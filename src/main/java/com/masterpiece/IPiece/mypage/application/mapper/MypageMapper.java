package com.masterpiece.IPiece.mypage.application.mapper;

import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.account.VirtualAccountJournal;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.dividends.domain.DividendPayouts;
import com.masterpiece.IPiece.dividends.infra.DividendPayoutsRepository;
import com.masterpiece.IPiece.market.domain.TradeExecution;
import com.masterpiece.IPiece.market.infra.jpa.TradeExecutionRepository;
import com.masterpiece.IPiece.mypage.api.dto.AccountHistoryItemDto;
import com.masterpiece.IPiece.mypage.api.dto.AccountJournalItemDto;
import com.masterpiece.IPiece.mypage.api.dto.AssetDto;
import com.masterpiece.IPiece.mypage.api.dto.FavoriteItemDto;
import com.masterpiece.IPiece.mypage.api.dto.PortfolioRatioDto;
import com.masterpiece.IPiece.mypage.api.dto.response.MyhomeResponse;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import com.masterpiece.IPiece.favorite.domain.FavoriteList;
import com.masterpiece.IPiece.offering.domain.ProductOfferingInfo;
import com.masterpiece.IPiece.offering.infra.ProductOfferingInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor  // ✅ 생성자 주입을 위해 추가
public class MypageMapper {

    private final ProductOfferingInfoRepository offeringInfoRepository;
    private final TradeExecutionRepository tradeExecutionRepository;
    private final DividendPayoutsRepository dividendPayoutsRepository;



    /**
     * Holdings → AssetDto 변환 (단일)
     */
    public AssetDto toAssetDto(Holdings holding) {
        Long currentPrice = holding.getProduct().getCurrentPrice();
        Long quantity = holding.getQuantity();
        Long avgBuyPrice = holding.getAvgBuyPrice();

        Long totalBuyPrice = avgBuyPrice * quantity;
        Long currentValue = currentPrice * quantity;
        Long profit = currentValue - totalBuyPrice;
        Double profitRate = totalBuyPrice > 0
                ? (double) profit / totalBuyPrice
                : 0.0;

        return AssetDto.builder()
                .productId(holding.getProduct().getProductId())
                .productName(holding.getProduct().getProductName())
                .tokenName(holding.getProduct().getTokenName())
                .thumbnailImg(holding.getProduct().getThumbnailImg())
                .quantity(quantity)
                .avgBuyPrice(avgBuyPrice)
                .totalBuyPrice(totalBuyPrice)
                .currentPrice(currentPrice)
                .currentValue(currentValue)
                .profit(profit)
                .profitRate(profitRate)
                .build();
    }

    /**
     * Holdings 리스트 → 같은 product끼리 합쳐서 AssetDto로 변환
     */
    public List<AssetDto> toMergedAssetDtos(List<Holdings> holdings) {
        return holdings.stream()
                .collect(Collectors.groupingBy(h -> h.getProduct().getProductId()))
                .entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getKey();
                    List<Holdings> productHoldings = entry.getValue();

                    // 같은 상품의 첫 번째 holding에서 상품 정보 가져오기
                    Holdings firstHolding = productHoldings.get(0);
                    Product product = firstHolding.getProduct();
                    Long currentPrice = product.getCurrentPrice();

                    // 수량 합계
                    long totalQuantity = productHoldings.stream()
                            .mapToLong(Holdings::getQuantity)
                            .sum();

                    // 가중평균 매수가 계산
                    long totalBuyAmount = productHoldings.stream()
                            .mapToLong(h -> h.getAvgBuyPrice() * h.getQuantity())
                            .sum();
                    long avgBuyPrice = totalQuantity > 0 ? totalBuyAmount / totalQuantity : 0;

                    // 평가금액 및 손익 계산
                    long currentValue = currentPrice * totalQuantity;
                    long profit = currentValue - totalBuyAmount;
                    double profitRate = totalBuyAmount > 0 ? (double) profit / totalBuyAmount : 0.0;

                    return AssetDto.builder()
                            .productId(productId)
                            .productName(product.getProductName())
                            .tokenName(product.getTokenName())
                            .thumbnailImg(product.getThumbnailImg())
                            .quantity(totalQuantity)
                            .avgBuyPrice(avgBuyPrice)
                            .totalBuyPrice(totalBuyAmount)
                            .currentPrice(currentPrice)
                            .currentValue(currentValue)
                            .profit(profit)
                            .profitRate(profitRate)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Holdings 리스트 → 포트폴리오 비중 계산
     */
    public List<PortfolioRatioDto> toPortfolioRatioDtos(List<Holdings> holdings) {
        // 전체 평가금액 계산
        long totalValue = holdings.stream()
                .mapToLong(h -> h.getProduct().getCurrentPrice() * h.getQuantity())
                .sum();

        if (totalValue == 0) {
            return List.of();
        }

        // 상품별 비중 계산
        return holdings.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getProduct().getProductName(),
                        Collectors.summingLong(h -> h.getProduct().getCurrentPrice() * h.getQuantity())
                ))
                .entrySet().stream()
                .map(entry -> PortfolioRatioDto.builder()
                        .productName(entry.getKey())
                        .ratio((double) entry.getValue() / totalValue * 100)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * VirtualAccountJournal → AccountJournalItemDto 변환
     */
    public AccountJournalItemDto toAccountJournalItemDto(
            VirtualAccountJournal journal
    ) {
        return AccountJournalItemDto.builder()
                .journalId(journal.getJournalId())
                .txType(journal.getTxType())
                .description(journal.getDescription())
                .amountKrw(journal.getAmountKrw())
                .balanceAfter(journal.getBalanceAfter())
                .createdAt(journal.getCreateAt()) // BaseEntity의 createAt
                .build();
    }

    /**
     * 전체 데이터 → MyhomeResponse 변환
     */
    public MyhomeResponse toMyhomeResponse(
            Long userId,
            VirtualAccount account,
            List<Holdings> holdings,
            List<AssetDto> allAssets,
            List<AssetDto> pagedAssets
    ) {
        // 1. 총 매수금액 (평균매수가 × 보유수량의 합)
        long totalBuyAmount = allAssets.stream()
                .mapToLong(AssetDto::getTotalBuyPrice)
                .sum();

        // 2. 총 평가금액 (현재주가 × 보유수량의 합)
        long totalEvaluation = allAssets.stream()
                .mapToLong(AssetDto::getCurrentValue)
                .sum();

        // 3. 보유 KRW (현금)
        long totalKrw = account.getBalanceKrw();

        // 4. 총 보유자산 = 보유 KRW + 총 평가
        long totalAssets = totalKrw + totalEvaluation;

        // 5. 총평가손익 = 총 평가 - 총 매수
        long totalProfit = totalEvaluation - totalBuyAmount;

        // 6. 총평가손익률 = (총 평가 - 총 매수) ÷ 총 매수 × 100
        double totalProfitRate = totalBuyAmount > 0
                ? (double) totalProfit / totalBuyAmount * 100
                : 0.0;

        // 7. 포트폴리오 비중
        List<PortfolioRatioDto> portfolioRatio = toPortfolioRatioDtos(holdings);

        // 8. 보유 상품 개수
        int holdingCount = allAssets.size();

        // 9. 주문가능 금액 (보유 KRW와 동일)
        long availableKrw = totalKrw;

        return MyhomeResponse.builder()
                .userId(userId)
                .totalKrw(totalKrw)                    // 보유 KRW
                .totalAssets(totalAssets)              // 총 보유자산
                .totalBuyAmount(totalBuyAmount)        // 총 매수
                .totalEvaluation(totalEvaluation)      // 총 평가
                .totalProfit(totalProfit)              // 총평가손익
                .totalProfitRate(totalProfitRate)      // 총평가손익률
                .availableKrw(availableKrw)            // 주문가능 금액
                .holdingCount(holdingCount)            // 보유 IP 개수
                .portfolioRatio(portfolioRatio)        // 포트폴리오 비중
                .assetList(pagedAssets)                  // 자산 목록
                .build();
    }

    /**
     * FavoriteList → FavoriteItemDto 변환
     */
    public FavoriteItemDto toFavoriteItemDto(FavoriteList favorite) {
        Product product = favorite.getProduct();

        // 상품 상태 판단 ("거래" or "공모")
        String status = determineProductStatus(product);

        // 가격 변동률 계산 (거래 상태일 때만)
        Double priceChangeRate = "2차 거래".equals(status)
                ? calculatePriceChangeRate(product)
                : null;

        return FavoriteItemDto.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .status(status)
                .thumbnail(product.getThumbnailImg())
                .currentPrice(product.getCurrentPrice())
                .priceChangeRate(priceChangeRate)
                .isFavorite(true)
                .build();
    }
    /**
     * 계좌 기준 체결 내역 → 거래내역 DTO 리스트로 변환
     */
    public List<AccountHistoryItemDto> toAccountTradeHistory(
            VirtualAccount account,
            LocalDateTime from,
            LocalDateTime to
    ) {
        List<TradeExecution> executions =
                tradeExecutionRepository.findByAccountAndMatchTimeBetween(account, from, to);

        return executions.stream()
                .map(exec -> mapExecutionToHistoryItem(exec, account))
                // DB에서 이미 account 조건을 걸었으므로 null 필터링 불필요
                .collect(Collectors.toList());
    }

    /**
     * 계좌 기준 배당금 지급 내역 → 거래내역 DTO 리스트로 변환
     */
    public List<AccountHistoryItemDto> toAccountDividendHistory(
            VirtualAccount account,
            LocalDateTime from,
            LocalDateTime to
    ) {
        // 완료된 배당만 보여준다고 가정 (status 값은 네가 INSERT한 값에 맞춰서 바꿔도 됨)
        List<DividendPayouts> payouts =
                dividendPayoutsRepository.findByVirtualAccountAndPayoutDateBetweenAndPayoutStatus(
                        account,
                        from,
                        to,
                        "PAID"
                );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return payouts.stream()
                .map(payout -> {
                    Product product = payout.getDividends().getProduct();

                    String createdAt = payout.getPayoutDate().format(formatter);

                    String type = "배당금";
                    String description = product.getProductName() + " 배당금";

                    Long price = payout.getPayoutAmount();   // 배당금은 현금 + (API에서 + 금액)

                    return AccountHistoryItemDto.builder()
                            .createdAt(createdAt)
                            .type(type)
                            .description(description)
                            .tokenAmount(null)   // 배당은 토큰 수량 변화 없음
                            .price(price)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 단일 체결내역 → 이 계좌 기준 history 아이템으로 변환
     */
    private AccountHistoryItemDto mapExecutionToHistoryItem(TradeExecution exec, VirtualAccount account) {
        Product product = exec.getProduct();
        OffsetDateTime time = exec.getMatchTime();

        boolean isBuyAccount = exec.getBuyOrder().getVirtualAccount().equals(account);
        boolean isSellAccount = exec.getSellOrder().getVirtualAccount().equals(account);

        // 이 계좌와 관련없는 체결이면 무시
        if (!isBuyAccount && !isSellAccount) {
            return null;
        }

        String type;
        Long tokenAmount = exec.getTradeQuantity();
        Long tradeAmount = exec.getTradeQuantity() * exec.getTradePrice();

        if (isBuyAccount) {
            // 구매 : 토큰 +, 현금 -
            type = "구매";
            tokenAmount = +tokenAmount;
            tradeAmount = -tradeAmount;
        } else {
            // 판매 : 토큰 -, 현금 +
            type = "판매";
            tokenAmount = -tokenAmount;
            tradeAmount = +tradeAmount;
        }

        String description = product.getProductName() + " 토큰 " + type;

        String createdAt = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        return AccountHistoryItemDto.builder()
                .createdAt(createdAt)
                .type(type)
                .description(description)
                .tokenAmount(tokenAmount)
                .price(tradeAmount)
                .build();
    }

    /**
     * 상품 상태 판단: "거래" or "공모"
     */
    private String determineProductStatus(Product product) {
        // ProductOfferingInfo 조회
        Optional<ProductOfferingInfo> offeringInfo =
                offeringInfoRepository.findById(product.getProductId());

        // ProductOfferingInfo가 없으면 "거래"
        if (offeringInfo.isEmpty()) {
            return "2차 거래";
        }

        // 공모 기간 확인
        OffsetDateTime now = OffsetDateTime.now();
        ProductOfferingInfo info = offeringInfo.get();

        boolean isOfferingPeriod =
                !now.isBefore(info.getOfferingStartDate()) &&
                        !now.isAfter(info.getOfferingEndDate());

        return isOfferingPeriod ? "공모" : "2차 거래";
    }

    /**
     * 가격 변동률 계산 (전일 대비)
     * 거래 상태일 때만 계산, 공모일 때는 null
     */
    private Double calculatePriceChangeRate(Product product) {
        Long lastPrice = product.getLastPrice();
        Long currentPrice = product.getCurrentPrice();

        if (lastPrice == null || lastPrice == 0) {
            return null;
        }

        // 등락률 = (현재가 - 전일가) / 전일가 * 100
        long priceDiff = currentPrice - lastPrice;
        return Math.round((double) priceDiff / lastPrice * 10000) / 100.0;  // 소수점 2자리
    }
}
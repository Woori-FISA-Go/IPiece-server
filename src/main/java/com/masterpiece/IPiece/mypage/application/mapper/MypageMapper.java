package com.masterpiece.IPiece.mypage.application.mapper;

import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.mypage.api.dto.AssetDto;
import com.masterpiece.IPiece.mypage.api.dto.PortfolioRatioDto;
import com.masterpiece.IPiece.mypage.api.dto.response.MyhomeResponse;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MypageMapper {

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
                .presentImg(holding.getProduct().getPresentImg())
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
                            .presentImg(product.getPresentImg())
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
}
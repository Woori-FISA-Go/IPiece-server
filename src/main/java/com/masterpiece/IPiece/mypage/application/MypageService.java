package com.masterpiece.IPiece.mypage.application;

import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountRepository;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.favorite.domain.FavoriteList;
import com.masterpiece.IPiece.favorite.infra.FavoriteListRepository;
import com.masterpiece.IPiece.mypage.api.dto.AccountHistoryItemDto;
import com.masterpiece.IPiece.mypage.api.dto.AssetDto;
import com.masterpiece.IPiece.mypage.api.dto.FavoriteItemDto;
import com.masterpiece.IPiece.mypage.api.dto.response.AccountHistoryResponse;
import com.masterpiece.IPiece.mypage.api.dto.response.FavoriteListResponse;
import com.masterpiece.IPiece.mypage.api.dto.response.MyhomeResponse;
import com.masterpiece.IPiece.mypage.application.mapper.MypageMapper;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import com.masterpiece.IPiece.mypage.infra.HoldingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageService {

    private final VirtualAccountRepository virtualAccountRepository;
    private final HoldingsRepository holdingsRepository;
    private final FavoriteListRepository favoriteListRepository;
    private final MypageMapper mypageMapper;

    private static final int PAGE_SIZE = 10;

    /**
     * 마이홈 조회 (보유자산 페이징)
     */
    public MyhomeResponse getMyHome(Long userId, int page) {
        // 1. 가상계좌 조회
        VirtualAccount account = virtualAccountRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));


        // 2. 전체 보유자산 조회
        List<Holdings> allHoldings = holdingsRepository.findAllByVirtualAccount(account);


        // 3. Holdings → AssetDto 변환
        List<AssetDto> allAssets = mypageMapper.toMergedAssetDtos(allHoldings);

        // 4. 페이징 처리 (1-based → 0-based 변환)
        int pageIndex = page - 1;
        int start = pageIndex * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allAssets.size());

        List<AssetDto> pagedAssets = (start < allAssets.size())
                ? allAssets.subList(start, end)
                : List.of();

        // 5. MyhomeResponse 생성
        return mypageMapper.toMyhomeResponse(userId, account, allHoldings, allAssets, pagedAssets);
    }

    public FavoriteListResponse getFavorites(Long userId) {
        // 1. 사용자의 가상계좌 조회 (User 정보 획득용)
        VirtualAccount account = virtualAccountRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 2. 관심목록 조회
        List<FavoriteList> favorites = favoriteListRepository.findAllByUser(account.getUser());

        // 3. FavoriteList → FavoriteItemDto 변환
        List<FavoriteItemDto> items = favorites.stream()
                .map(mypageMapper::toFavoriteItemDto)
                .collect(Collectors.toList());

        // 4. Response 생성
        return FavoriteListResponse.builder()
                .totalCount(items.size())
                .items(items)
                .build();
    }

    public AccountHistoryResponse getAccountHistory(Long userId, String dateFrom, String dateTo) {
        // 0. 날짜 파싱
        LocalDate from = LocalDate.parse(dateFrom);
        LocalDate to = LocalDate.parse(dateTo);

        // 서버 기본 타임존 기준으로 OffsetDateTime 만들기
        ZoneId zone = ZoneId.of("Asia/Seoul");

        // 시작 시각 = 00:00:00
        OffsetDateTime fromDateTime = from
                .atStartOfDay(zone)
                .toOffsetDateTime();

        // 끝 시각 = 23:59:59.999...
        OffsetDateTime toDateTime = to
                .atTime(LocalTime.MAX)
                .atZone(zone)
                .toOffsetDateTime();


        // 1. 가상계좌 조회
        Optional<VirtualAccount> optionalAccount = virtualAccountRepository.findByUser_UserId(userId);

        // 1-1. 가상계좌가 없는 경우: total_balance/pending_price 0, history는 빈 리스트
        if (optionalAccount.isEmpty()) {
            return AccountHistoryResponse.builder()
                    .totalBalance(0L)
                    .pendingPrice(0L)
                    .history(List.of())
                    .build();
        }
        VirtualAccount account = optionalAccount.get();

        // 2. 거래내역(구매/판매) 조회 및 매핑
        List<AccountHistoryItemDto> tradeHistory =
                mypageMapper.toAccountTradeHistory(account, fromDateTime, toDateTime);

        List<AccountHistoryItemDto> dividendHistory =
                mypageMapper.toAccountDividendHistory(account, fromDateTime, toDateTime);

        List<AccountHistoryItemDto> history = Stream.concat(
                        tradeHistory.stream(),
                        dividendHistory.stream()
                )
                .sorted(Comparator.comparing(AccountHistoryItemDto::getCreatedAt).reversed())
                .collect(Collectors.toList());

        // 4. 합계 값
        long totalBalance = account.getBalanceKrw();
        long pendingPrice = account.getPendingPrice() != null ? account.getPendingPrice() : 0L;

        return AccountHistoryResponse.builder()
                .totalBalance(totalBalance)
                .pendingPrice(pendingPrice)
                .history(history)
                .build();
    }
}
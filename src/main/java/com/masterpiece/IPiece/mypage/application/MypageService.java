package com.masterpiece.IPiece.mypage.application;

import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.account.VirtualAccountJournal;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountRepository;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountJournalRepository;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.favorite.domain.FavoriteList;
import com.masterpiece.IPiece.favorite.infra.FavoriteListRepository;
import com.masterpiece.IPiece.mypage.api.dto.AccountHistoryItemDto;
import com.masterpiece.IPiece.mypage.api.dto.AccountJournalItemDto;
import com.masterpiece.IPiece.mypage.api.dto.AssetDto;
import com.masterpiece.IPiece.mypage.api.dto.FavoriteItemDto;
import com.masterpiece.IPiece.mypage.api.dto.response.AccountHistoryResponse;
import com.masterpiece.IPiece.mypage.api.dto.response.AccountJournalResponse;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final VirtualAccountJournalRepository virtualAccountJournalRepository;

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
        LocalDate from = LocalDate.parse(dateFrom);   // 예: "2025-09-23"
        LocalDate to = LocalDate.parse(dateTo);       // 예: "2025-10-23"

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

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

    /**
     * 가상계좌 분개장(입출금/배당/거래) 전체 조회 (날짜 필터 없음, 최신순)
     */
    public AccountJournalResponse getAccountJournals(Long userId) {

        // 1. 가상계좌 조회
        VirtualAccount account = virtualAccountRepository
                .findByUser_UserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 2. 해당 계좌의 모든 저널을 최신순으로 조회
        List<VirtualAccountJournal> journals =
                virtualAccountJournalRepository
                        .findByVirtualAccountOrderByCreateAtDesc(account);

        // 3. DTO 변환
        List<AccountJournalItemDto> items = journals.stream()
                .map(mypageMapper::toAccountJournalItemDto)
                .collect(Collectors.toList());

        // 4. 상단 요약 정보
        long totalBalance = account.getBalanceKrw();
        long pendingPrice = account.getPendingPrice() != null ? account.getPendingPrice() : 0L;

        return AccountJournalResponse.builder()
                .totalBalance(totalBalance)
                .pendingPrice(pendingPrice)
                .items(items)
                .build();
    }
}
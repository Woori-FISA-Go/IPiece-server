package com.masterpiece.IPiece.mypage.api;

import com.masterpiece.IPiece.mypage.api.dto.response.AccountHistoryResponse;
import com.masterpiece.IPiece.mypage.api.dto.response.AccountJournalResponse;
import com.masterpiece.IPiece.mypage.api.dto.response.FavoriteListResponse;
import com.masterpiece.IPiece.mypage.api.dto.response.MyhomeResponse;
import com.masterpiece.IPiece.mypage.application.MypageService;
import com.masterpiece.IPiece.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/mypage")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class MypageController {

    private final MypageService mypageService;

    /**
     * 마이홈 조회
     * GET /v1/mypage/myhome?page=1
     */
    @GetMapping("/myhome")
    public ResponseEntity<MyhomeResponse> getMyHome(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") int page
    ) {
        MyhomeResponse response = mypageService.getMyHome(userId, page);
        return ResponseEntity.ok(response);
    }

    /**
     * 관심목록 조회
     * GET /v1/mypage/favorites
     */
    @GetMapping("/favorites")
    public ResponseEntity<FavoriteListResponse> getFavorites(
            @AuthenticationPrincipal Long userId
    ) {
        FavoriteListResponse response = mypageService.getFavorites(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 계좌 거래내역 조회
     * GET /v1/mypage/account?date_from=2025-09-23&date_to=2025-10-23
     */
    @GetMapping("/account")
    public ResponseEntity<AccountHistoryResponse> getAccountHistory(
            @AuthenticationPrincipal Long userId,
            @RequestParam("date_from") String dateFrom,
            @RequestParam("date_to") String dateTo
    ) {
        AccountHistoryResponse response = mypageService.getAccountHistory(userId, dateFrom, dateTo);
        return ResponseEntity.ok(response);
    }
    /**
     * 가상계좌 분개장(입출금/배당/거래) 내역 조회
     * 날짜 필터 없이 전체를 최신순으로 반환
     * GET /v1/mypage/account/journals
     */
    @GetMapping("/account/journals")
    public ResponseEntity<AccountJournalResponse> getAccountJournals(
            @AuthenticationPrincipal Long userId
    ) {
        AccountJournalResponse response = mypageService.getAccountJournals(userId);
        return ResponseEntity.ok(response);
    }
}
package com.masterpiece.IPiece.admin.dividend.api;

import com.masterpiece.IPiece.admin.dividend.api.dto.request.AdminUpsertDividendRequest;
import com.masterpiece.IPiece.admin.dividend.api.dto.response.AdminDividendListResponse;
import com.masterpiece.IPiece.admin.dividend.api.dto.response.AdminDividendPayoutsResponse;
import com.masterpiece.IPiece.admin.dividend.api.dto.response.AdminDividendResponse;
import com.masterpiece.IPiece.admin.dividend.application.AdminDividendService;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.infra.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AdminDividendController {

    private final AdminDividendService adminDividendService;
    private final UserRepository userRepository;

    /**
     * 공통: 관리자 인증 체크 (user_made_id == "admin")
     */
    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        Long userId;
        try {
            userId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);
        return user != null && "admin".equals(user.getUserMadeId());
    }
    /**
     * 1) 배당 선언 생성/수정
     * POST /v1/admin/dividends
     */
    @PostMapping("/v1/admin/dividends")
    public ResponseEntity<AdminDividendResponse> upsertDividend(
            Authentication authentication,
            @Valid @RequestBody AdminUpsertDividendRequest request
    ) {
        if (!isAdmin(authentication)) {
            // 인증 실패나 관리자 아님 → 403
            return ResponseEntity.status(403).build();
        }

        AdminDividendResponse response = adminDividendService.upsertDividend(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 2) 배당 선언 목록 조회 (페이징 없음)
     * GET /v1/admin/dividends?productId=&status=
     */
    @GetMapping("/v1/admin/dividends")
    public ResponseEntity<AdminDividendListResponse> listDividends(
            Authentication authentication,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "status", required = false) String status
    ) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).build();
        }

        AdminDividendListResponse response = adminDividendService.listDividends(productId, status);
        return ResponseEntity.ok(response);
    }

    /**
     * 3) 배당 집행 결과 조회 (path variable 버전)
     * GET /v1/admin/dividends/{dividendId}/payouts?status=
     */
    @GetMapping("/v1/admin/dividends/{dividendId}/payouts")
    public ResponseEntity<AdminDividendPayoutsResponse> getDividendPayouts(
            Authentication authentication,
            @PathVariable Long dividendId,
            @RequestParam(value = "status", required = false) String status
    ) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).build();
        }

        AdminDividendPayoutsResponse response =
                adminDividendService.getDividendPayouts(dividendId, status);

        return ResponseEntity.ok(response);
    }

    /**
     * 3-1) 배당 집행 결과 조회 (query parameter 버전)
     * GET /v1/admin/dividends/payouts?dividendId=&status=
     *
     * - 기존 프론트가 /v1/admin/dividends/payouts 로 호출하고 있어서
     *   NoResourceFoundException 이 발생하던 부분을 위한 호환용 엔드포인트.
     * - 내부적으로는 위의 getDividendPayouts(dividendId, status) 를 그대로 사용한다.
     */
    @GetMapping("/v1/admin/dividends/payouts")
    public ResponseEntity<AdminDividendPayoutsResponse> getDividendPayoutsByQuery(
            Authentication authentication,
            @RequestParam("dividendId") Long dividendId,
            @RequestParam(value = "status", required = false) String status
    ) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).build();
        }

        AdminDividendPayoutsResponse response =
                adminDividendService.getDividendPayouts(dividendId, status);

        return ResponseEntity.ok(response);
    }
}
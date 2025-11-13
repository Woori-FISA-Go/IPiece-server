package com.masterpiece.IPiece.mypage.api;

import com.masterpiece.IPiece.mypage.api.dto.response.MyhomeResponse;
import com.masterpiece.IPiece.mypage.application.MypageService;
import com.masterpiece.IPiece.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    /**
     * 마이홈 조회
     * GET /v1/mypage/myhome?page=1
     */
    @GetMapping("/myhome")
    public ResponseEntity<MyhomeResponse> getMyHome(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "1") int page
    ) {
        MyhomeResponse response = mypageService.getMyHome(user.getUserId(), page);
        return ResponseEntity.ok(response);
    }

    // 테스트용이라 실제 운영 시 삭제
    @GetMapping("/myhome/test")
    public ResponseEntity<MyhomeResponse> getMyHomeTest(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page
    ) {
        MyhomeResponse response = mypageService.getMyHome(userId, page);
        return ResponseEntity.ok(response);
    }
}
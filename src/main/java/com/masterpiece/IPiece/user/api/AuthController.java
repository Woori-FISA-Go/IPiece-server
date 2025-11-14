package com.masterpiece.IPiece.user.api;

import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.common.util.JwtTokenProvider;
import com.masterpiece.IPiece.common.web.Responses;
import com.masterpiece.IPiece.user.api.dto.request.LoginRequest;
import com.masterpiece.IPiece.user.application.AuthService;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.infra.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.masterpiece.IPiece.auth.application.TokenBlacklistService;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth/token")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){

        //JWT 토큰 반환
        Map<String, String> tokens = authService.login(request.getId(), request.getPassword());
        return Responses.ok(
            Map.of(
                    "message", "로그인 성공",
                    "accessToken", tokens.get("accessToken"),
                    "refreshToken", tokens.get("refreshToken")
            )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");

        // 1. 유효성 검사
        ErrorCode error = jwtTokenProvider.validateToken(refreshToken);
        if (error != null) {
            throw new BusinessException(error);
        }

        // 2. user 찾기
        String userId = jwtTokenProvider.getSubject(refreshToken);
        User user = userRepository.findByUserMadeId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN_ID));

        // 3. DB의 refresh_token과 일치하는지 확인
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 4. 새 접근 토큰 발급
        String newAccessToken = jwtTokenProvider.generateToken(userId);

        return Responses.ok(Map.of(
                "accessToken", newAccessToken
        ));
    }


    // 로그아웃 테스트시 로그인해서 accessToken 저장해놓고, 로그아웃 API 호출시

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        // 1Authorization 헤더에서 토큰 꺼내기
        String authHeader = request.getHeader("Authorization");

        // 헤더 없거나 Bearer 아닌 경우 → 잘못된 요청으로 간주
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);  // 또는 별도 에러코드 생성 가능
        }

        // "Bearer " 이후 실제 토큰 부분만 추출
        String accessToken = authHeader.substring(7);

        // 토큰 유효성 검증 (만료/위조 여부 확인)
        var error = jwtTokenProvider.validateToken(accessToken);
        if (error != null) {
            // 이미 만료된 토큰이라면 굳이 블랙리스트에 넣지 않고 바로 에러 던져도 됨
            throw new BusinessException(error);
        }

        // 토큰 남은 만료 시간(초) 계산
        long ttlSeconds = jwtTokenProvider.getRemainingValiditySeconds(accessToken);

        // 안전하게 보려면 0 이하일 때는 그냥 블랙리스트 처리 안 해도 됨
        if (ttlSeconds > 0) {
            // Redis 블랙리스트에 등록
            tokenBlacklistService.blacklist(accessToken, ttlSeconds);
        }

        // 5️응답 반환 (굳이 body 안 줘도 됨)
        return Responses.noContent();  // 204 No Content
    }

}

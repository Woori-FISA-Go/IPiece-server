package com.masterpiece.IPiece.user.api;

import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.common.util.JwtTokenProvider;
import com.masterpiece.IPiece.common.web.Responses;
import com.masterpiece.IPiece.user.api.dto.request.LoginRequest;
import com.masterpiece.IPiece.user.application.AuthService;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth/token")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

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


}

package com.masterpiece.IPiece.user.application;

import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.common.util.JwtTokenProvider;
import com.masterpiece.IPiece.common.util.PasswordHasher;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtTokenProvider jwtTokenProvider;

    /** 로그인 API */
    public Map<String, String> login(String loginId, String password) {

        User user = userRepository.findByUserMadeId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN_ID));

        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // Access Token 생성
        String accessToken = jwtTokenProvider.generateToken(user.getUserMadeId());

        // Refresh Token 생성
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserMadeId());

        // Refresh Token DB 저장
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);

        // 두 토큰 모두 리턴
        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }
}

package com.masterpiece.IPiece.user.application;

import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.common.util.JwtTokenProvider;
import com.masterpiece.IPiece.common.util.PasswordHasher;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.infra.UserRepository;
import jakarta.transaction.Transactional;
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
    @Transactional
    public Map<String, String> login(String loginId, String password) {

        User user = userRepository.findByUserMadeId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN_ID));

        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // Access Token 생성
        String accessToken = jwtTokenProvider.generateToken(String.valueOf(user.getUserId()));

        // Refresh Token 생성
        String refreshToken = jwtTokenProvider.generateRefreshToken(String.valueOf(user.getUserId()));

        // Refresh Token DB 저장
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);

        // 두 토큰 모두 리턴
        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }

    /* 로그아웃 API */
    @Transactional
    public void logout(String accessToken) {
        // JWT 유효성 검사
        ErrorCode error = jwtTokenProvider.validateToken(accessToken);
        if (error != null) {    //에러가 있으면(토큰이 유효하지 않거나..)
            throw new BusinessException(error);
        }

        // userId 추출
        String userId = jwtTokenProvider.getSubject(accessToken);

        // 사용자 조회
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN_ID));

        // refreshToken 삭제
        user.updateRefreshToken(null);
        userRepository.save(user);

        /*왜 Access Token을 삭제하지 않는가?
        * Access Token은 어차피 금방 만료되고 재발급도 하지 못함(Refresh Token이 없으면)
        * -> 사실상 있어도 쓸 수 없음
        * */
    }

}

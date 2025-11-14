package com.masterpiece.IPiece.common.util;

import com.masterpiece.IPiece.common.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final byte[] secret;
    private final long expirationMs;          // Access Token 유효기간
    private final long refreshExpirationMs;   // Refresh Token 유효기간

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-ms:241920000}") long expirationMs,     // 28일
            @Value("${jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs // 7일
    ) {
        this.secret = secretKey.getBytes(StandardCharsets.UTF_8);
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    /* Access Token 생성 */
    public String generateToken(String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(Keys.hmacShaKeyFor(secret), SignatureAlgorithm.HS256)
                .compact();
    }

    /* Refresh Token 생성 */
    public String generateRefreshToken(String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshExpirationMs))
                .signWith(Keys.hmacShaKeyFor(secret), SignatureAlgorithm.HS256)
                .compact();
    }

    /** subject 추출 */
    public String getSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * JWT 검증
     * 정상 = null
     * 만료 = EXPIRED_TOKEN
     * 기타 에러 = INVALID_TOKEN
     */
    public ErrorCode validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token);
            return null;
        } catch (ExpiredJwtException e) {
            return ErrorCode.EXPIRED_TOKEN;
        } catch (JwtException | IllegalArgumentException e) {
            return ErrorCode.INVALID_TOKEN;
        }
    }

    // JwtTokenProvider 안에 추가

    /**
     * ⏱ JWT 남은 만료 시간(초)을 계산해서 반환
     * - 로그아웃 시, 이 값을 Redis TTL로 사용하면 됨
     */
    public long getRemainingValiditySeconds(String token) {
        // 토큰에서 클레임(Claims) 꺼내기
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // 만료 시각(Expiration)을 밀리초 기준으로 가져옴
        Date expiration = claims.getExpiration();
        long now = System.currentTimeMillis();

        // 이미 만료된 토큰이면 0 이하가 나올 수 있으니 max(0, …) 처리
        long diffMillis = Math.max(0, expiration.getTime() - now);

        // 초 단위로 반환
        return diffMillis / 1000;
    }



}

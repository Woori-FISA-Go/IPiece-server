package com.masterpiece.IPiece.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * AccessToken 블랙리스트 관리용 서비스
 *
 * - 로그아웃 된 토큰 / 강제 만료 시킨 토큰을 Redis에 저장해두고
 *   이후 요청에서 "이 토큰은 이미 사용 불가"라는 것을 판단하는 용도.
 *
 * - 구조 예시:
 *   key   : "blacklist:{accessToken}"
 *   value : "1" (아무 값이나 상관 없음, 존재 여부만 체크)
 *   TTL   : 토큰 남은 만료 시간(초 단위) → 시간이 지나면 Redis에서 자동 삭제
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    // Spring에서 제공하는 Redis 문자열 전용 템플릿
    private final StringRedisTemplate redisTemplate;

    // 블랙리스트 키에 공통으로 붙일 prefix (충돌 방지용)
    private static final String BLACKLIST_PREFIX = "blacklist:";

    /**
     * 토큰을 블랙리스트에 등록
     *
     * @param accessToken   사용자가 가지고 있던 액세스 토큰(JWT 문자열)
     * @param ttlSeconds    이 토큰이 더 이상 유효하지 않아야 하는 시간(초 단위)
     *                      보통 "JWT 남은 만료시간"을 그대로 넣어줌
     */
    public void blacklist(String accessToken, long ttlSeconds) {
        // Redis에 저장할 key 생성: "blacklist:토큰내용"
        String key = BLACKLIST_PREFIX + accessToken;

        // value는 크게 의미 없어서 "1" 같은 더미 값으로 저장
        // Duration.ofSeconds(ttlSeconds) 만큼 지나면 자동 삭제됨.
        redisTemplate.opsForValue().set(
                key,
                "1",
                Duration.ofSeconds(ttlSeconds)
        );
    }

    /**
     * 해당 토큰이 블랙리스트에 있는지 확인
     *
     * @param accessToken   클라이언트가 헤더에 담아 보낸 액세스 토큰
     * @return              true  → 블랙리스트에 있음(= 이미 로그아웃 처리된 토큰)
     *                      false → 블랙리스트에 없음(= 사용 가능 후보, 추가 검증 필요)
     */
    public boolean isBlacklisted(String accessToken) {
        String key = BLACKLIST_PREFIX + accessToken;

        // hasKey()는 key가 존재하면 true, 없으면 false 반환
        Boolean exists = redisTemplate.hasKey(key);

        // null 안전 처리 (NullPointer 방지)
        return exists != null && exists;
    }
}

package com.masterpiece.IPiece.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.common.util.JwtTokenProvider;
import com.masterpiece.IPiece.user.application.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * JWT 인증 필터
 * - Authorization 헤더에서 토큰 추출 및 검증
 * - ErrorCode 기반 JSON problem 응답 반환
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // 회원가입 multipart 요청은 JWT 검사 제외
        if (path.startsWith("/v1/signup")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // 토큰이 없으면: 그냥 통과 (401 던지지 않음) -> SecurityConfig에서 토큰이 필요 없는걸 걸러주니 여기선 토큰이 없으면 그냥 통과시킴
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // 먼저 블랙리스트(로그아웃된 토큰)인지 확인
        if (tokenBlacklistService.isBlacklisted(token)) {
            // 블랙리스트면 더 볼 것도 없이 바로 401 응답
            sendErrorResponse(response, request, ErrorCode.INVALID_TOKEN);
            return;
        }

        ErrorCode error = jwtTokenProvider.validateToken(token);
        if (error != null) {
            sendErrorResponse(response, request, error);
            return;
        }

        String userIdStr = jwtTokenProvider.getSubject(token);
        try {
            Long userId = Long.valueOf(userIdStr); // userIdStr을 Long으로 변환

            // UserDetailsService를 통해 UserDetails 객체를 로드하여 권한 정보를 가져옵니다.
            UserDetails userDetails = userDetailsService.loadUserByUsername(userIdStr);
            // UserDetails에서 추출한 권한 정보를 사용합니다.
            var authorities = userDetails.getAuthorities();

            // Principal은 Long 타입 userId를 유지하고, 권한 정보만 추가합니다.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ex) { // UsernameNotFoundException, JwtException 등 모든 예외 처리
            sendErrorResponse(response, request, ErrorCode.INVALID_TOKEN);
            return;
        }

        filterChain.doFilter(request, response);
    }


    /**
     * 공통 problem+json 에러 응답
     * JWT 인증 예외는 핸들러(@RestControllerAdvice)가 도달하기 전(Security Filter 단계)에서 발생하지만,
     * GlobalExceptionHandler가 반환하는 problem+json 형식과 동일한 구조로 직접 응답을 만들어주면,
     * 결과적으로 프론트엔드에서는 "모든 예외가 같은 형식으로 내려오는 것처럼 보이게 된다.
     */
    private void sendErrorResponse(HttpServletResponse response, HttpServletRequest request, ErrorCode code) throws IOException {
        response.setStatus(code.getStatus().value());
        response.setContentType("application/problem+json; charset=UTF-8");

        Map<String, Object> body = Map.of(
                "type", code.name(),
                "title", code.getMessage(),
                "status", code.getStatus().value(),
                "detail", code.getMessage(),
                "instance", request.getRequestURI()
        );

        objectMapper.writeValue(response.getWriter(), body);
    }
}
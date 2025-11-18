package com.masterpiece.IPiece.config;

import com.masterpiece.IPiece.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 로그인 없이 접근 허용 경로를 설정
                        .requestMatchers(
                                "/v1/auth/**",
                                "/v1/otp/**",
                                "/v1/signup/info",
                                "/v1/market/products",
                                "/v1/market/*/details",
                                "/v1/market/*/chart",
                                "/v1/market/*/orders",
                                "/v1/market/*/detail",
                                "/v1/main/home",
                                "/v1/offerings",
                                "/v1/offerings/detail/{productId}",
                                "/error").permitAll()
                        .anyRequest().authenticated()   // 허용 경로 말고는 jwt토큰 달고 들어와야함
                )

                // 인증이 필요한데 토큰이 없거나 잘못된 경우 (401)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // ErrorCode.AUTH_REQUIRED 사용
                            ErrorCode code = ErrorCode.AUTH_REQUIRED;

                            response.setStatus(code.getStatus().value());
                            response.setContentType("application/problem+json; charset=UTF-8");

                            new com.fasterxml.jackson.databind.ObjectMapper()
                                    .writeValue(response.getWriter(), Map.of(
                                            "type", code.name(),
                                            "title", code.getMessage(),
                                            "status", code.getStatus().value(),
                                            "detail", code.getMessage(),
                                            "instance", request.getRequestURI()
                                    ));
                        })
                        // 권한이 없을 때 (403)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            ErrorCode code = ErrorCode.PERMISSION_DENIED;

                            response.setStatus(code.getStatus().value());
                            response.setContentType("application/problem+json; charset=UTF-8");

                            new com.fasterxml.jackson.databind.ObjectMapper()
                                    .writeValue(response.getWriter(), Map.of(
                                            "type", code.name(),
                                            "title", code.getMessage(),
                                            "status", code.getStatus().value(),
                                            "detail", code.getMessage(),
                                            "instance", request.getRequestURI()
                                    ));
                        })
                )
                // 기본 필터 전에 직접 만든 JWT 필터를 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 직접 AuthenticationManager를 쓰지 않지만, Spring Security가 내부적으로 필요하기 때문에 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

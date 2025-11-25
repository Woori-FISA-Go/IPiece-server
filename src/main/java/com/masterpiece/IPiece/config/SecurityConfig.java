package com.masterpiece.IPiece.config;

import com.masterpiece.IPiece.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.masterpiece.IPiece.config.JwtAuthenticationFilter;

import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOriginPattern(allowedOrigins);  // 프론트 URL 허용
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("Idempotency-Key");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // 🔥 반드시 필요
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/actuator/health",
                                "/v1/auth/**",
                                "/v1/otp/**",
                                "/v1/signup/info",
                                "/v1/auth/otp/**",
                                "/v1/auth/token/login",
                                "/v1/auth/token/refresh",
                                "/v1/blockchain/tokens/{address}",
                                "/v1/signup/**",
                                "/v1/market/products",
                                "/v1/market/*/details",
                                "/v1/market/*/chart",
                                "/v1/market/*/orders",
                                "/v1/market/*/detail",
                                "/v1/main/home",
                                "/v1/offerings",
                                "/v1/offerings/*/detail",
                                "/error",
                                "/images/**",
                                "/ws/**"
                        ).permitAll()
                        .requestMatchers("/v1/blockchain/admin/**").hasRole("ADMIN") // 이 줄 추가
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
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
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    // 직접 AuthenticationManager를 쓰지 않지만, Spring Security가 내부적으로 필요하기 때문에 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

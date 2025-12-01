package com.masterpiece.IPiece.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.config.JwtAuthenticationFilter;
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

import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * 🔥 CORS Config
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOriginPattern(allowedOrigins);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    /**
     * 🔥 Spring Boot 3.x
     * HTTP Metrics는 WebMvcObservationFilter가 자동 등록되므로
     * WebMvcMetricsFilter 같은 Bean 생성이 필요 없음.
     * 중요한 것은 JWT 필터가 "뒤"에 있어야 metrics 관찰이 가능.
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // Prometheus + Actuator
                        .requestMatchers(
                                "/actuator/**",
                                "/actuator/prometheus",
                                "/actuator/health",
                                "/healthz",
                                "/api/healthz"
                        ).permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**"
                        ).permitAll()

                        // 비회원 허용 API
                        .requestMatchers(
                                "/v1/auth/**",
                                "/v1/otp/**",
                                "/v1/signup/**",
                                "/v1/auth/token/**",
                                "/v1/market/**",
                                "/v1/main/home",
                                "/v1/offerings",
                                "/v1/offerings/*/detail",
                                "/images/**",
                                "/ws/**",
                                "/error"
                        ).permitAll()

                        // ADMIN 권한
                        .requestMatchers("/v1/blockchain/admin/**").hasRole("ADMIN")

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            ErrorCode code = ErrorCode.AUTH_REQUIRED;
                            response.setStatus(code.getStatus().value());
                            response.setContentType("application/problem+json; charset=UTF-8");
                            new ObjectMapper().writeValue(response.getWriter(), Map.of(
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
                            new ObjectMapper().writeValue(response.getWriter(), Map.of(
                                    "type", code.name(),
                                    "title", code.getMessage(),
                                    "status", code.getStatus().value(),
                                    "detail", code.getMessage(),
                                    "instance", request.getRequestURI()
                            ));
                        })
                )

                /**
                 * 🔥 핵심: JWT 필터는 UsernamePasswordAuthenticationFilter "뒤"에 둬야 함
                 * 그래야 ObservationFilter(WebMvcObservationFilter) → Security → JWT 순으로 넘어가며
                 * HTTP 요청 메트릭이 생성됨.
                 */
                .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AuthenticationManager Bean
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}

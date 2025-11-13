package com.masterpiece.IPiece.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Profile("local")
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/v1/_debug/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/_debug/**", "/error", "/v1/mypage/myhome/test").permitAll() // /v1/mypage/myhome/test는 테스트용이라 추후 삭제
                        .anyRequest().authenticated()
                );
        return http.build();
    }

}
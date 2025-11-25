package com.masterpiece.IPiece.config;

import com.masterpiece.IPiece.common.util.JwtTokenProvider;
import com.masterpiece.IPiece.integration.besu.BesuClient;
import com.masterpiece.IPiece.user.application.TokenBlacklistService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@EnableMethodSecurity
public class TestConfig {

    // 블록체인 관련
    @Bean @Primary
    public BesuClient besuClient() {
        return mock(BesuClient.class);
    }

    @Bean @Primary
    public Web3j web3j() {
        return mock(Web3j.class);
    }

    @Bean @Primary
    public Credentials credentials() {
        return mock(Credentials.class);
    }

    // JWT 관련
    @Bean @Primary
    public JwtTokenProvider jwtTokenProvider() {
        JwtTokenProvider mock = mock(JwtTokenProvider.class);
        when(mock.validateToken(anyString())).thenReturn(null);
        when(mock.getSubject(anyString())).thenReturn("1");
        return mock;
    }

    // 토큰 블랙리스트
    @Bean @Primary
    public TokenBlacklistService tokenBlacklistService() {
        TokenBlacklistService mock = mock(TokenBlacklistService.class);
        when(mock.isBlacklisted(anyString())).thenReturn(false);
        return mock;
    }

    // 유저 인증
    @Bean @Primary
    public UserDetailsService userDetailsService() {
        // USER 계정
        UserDetails testUser = User.builder()
            .username("1")
            .password("test")
            .authorities("ROLE_USER")
            .build();
        
        // ADMIN 계정
        UserDetails adminUser = User.builder()
            .username("2")
            .password("admin")
            .authorities("ROLE_ADMIN", "ROLE_USER")
            .build();
        
        UserDetailsService mock = mock(UserDetailsService.class);
        when(mock.loadUserByUsername("1")).thenReturn(testUser);
        when(mock.loadUserByUsername("2")).thenReturn(adminUser);
        return mock;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

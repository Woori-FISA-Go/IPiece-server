package com.masterpiece.IPiece.user.application;

import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // JWT의 subject에 저장된 userId를 사용합니다.
        Long userId;
        try {
            userId = Long.parseLong(username);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Invalid user id in JWT subject: " + username, e);
        }

        // 1. DB에서 우리 시스템의 User 엔티티를 조회합니다.
        User ourUser = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        // 2. 조회한 User 정보를 Spring Security가 사용하는 UserDetails 객체로 변환합니다.
        return new org.springframework.security.core.userdetails.User(
                ourUser.getUserId().toString(),
                ourUser.getPasswordHash(), // 비밀번호는 JWT 인증에서는 사용되지 않지만 형식상 넣어줍니다.
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // 사용자의 권한(Role)을 지정합니다.
        );
    }
}

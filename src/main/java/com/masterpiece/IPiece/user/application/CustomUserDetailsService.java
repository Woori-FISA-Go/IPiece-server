package com.masterpiece.IPiece.user.application;

import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private static final String ADMIN_USER_ID = "admin"; // Added constant

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
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // 모든 사용자는 기본적으로 USER 권한을 가집니다.

        // user_made_id가 "admin"이면 ADMIN 권한을 추가로 부여합니다.
        // NPE 방지를 위해 ourUser.getUserMadeId()가 null이 아닌지 먼저 확인
        if (ourUser.getUserMadeId() != null && ADMIN_USER_ID.equalsIgnoreCase(ourUser.getUserMadeId())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return new org.springframework.security.core.userdetails.User(
                ourUser.getUserId().toString(),
                ourUser.getPasswordHash(), // 비밀번호는 JWT 인증에서는 사용되지 않지만 형식상 넣어줍니다.
                authorities // 사용자의 권한(Role)을 지정합니다.
        );
    }
}

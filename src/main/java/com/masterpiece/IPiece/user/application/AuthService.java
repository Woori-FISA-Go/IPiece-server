package com.masterpiece.IPiece.user.application;

import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.common.util.JwtTokenProvider;
import com.masterpiece.IPiece.common.util.PasswordHasher;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtTokenProvider jwtTokenProvider;

    public String login(String loginId, String password){
        User user = userRepository.findByUserMadeId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN_ID));

        if(!passwordHasher.matches(password, user.getPasswordHash())){
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        return jwtTokenProvider.generateToken(user.getUserMadeId());
    }


}

package com.masterpiece.IPiece.user.application;

import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.common.util.PasswordHasher;
import com.masterpiece.IPiece.user.api.dto.request.SignUpRequest;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.domain.UserPrivate;
import com.masterpiece.IPiece.user.infra.StorageService;
import com.masterpiece.IPiece.user.infra.UserPrivateRepository;
import com.masterpiece.IPiece.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static java.time.LocalTime.now;

@Service
@RequiredArgsConstructor
public class UserService {
    private final StorageService storageService; // ← 저장 위치(S3 or Local) 전략을 주입받음
    private final UserRepository userRepository;
    private final UserPrivateRepository userPrivateRepository;
    private final PasswordHasher passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    public void signUp(SignUpRequest request, MultipartFile idCardFile) {
        // 1. 본인인증 여부 체크
        if (!request.isVerified()) {
            throw new BusinessException(ErrorCode.UNVERIFIED_USER);
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. 운영 DB에 저장할 사용자 정보 생성
        User user = User.builder()
                        .userMadeId(request.getId())
                        .passwordHash(encodedPassword)
                        .isVerified(true)
                        .joinDate(OffsetDateTime.now())
                        .build();

        // DB에 user 저장하고 해당 user정보 저장(이 때 userId 자동 생성됨)
        User savedUser = userRepository.save(user);

        // 신분증 이미지 저장하고 파일 경로 받기
        String idCardPath = storageService.saveIdCard(idCardFile, request.getId());


        /** 5) UserPrivate 생성 (민감정보 DB) */
        UserPrivate privateInfo = UserPrivate.builder()
                .user(savedUser)                       // @MapsId 관계로 user_id 공유됨
                .name(request.getName())
                .birthDate(LocalDate.parse(request.getBirth())) // "19980214" 형식이면 포맷 별도처리 필요
                .phoneNumber(request.getPhone())
                .address(request.getAddress())
                .idCardImg(idCardPath)                 // 로컬경로나 S3 key
                .build();

        userPrivateRepository.save(privateInfo);
    }
}

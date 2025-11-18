package com.masterpiece.IPiece.user.domain;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users") //DB테이블명 명시(PostgreSQL은 user가 예약어기 때문에 "" 명시)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //외부 직접 생성 방지
@AllArgsConstructor
@Builder
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto_increment
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_made_id", nullable = false,  unique = true)
    private String userMadeId;  //사용자 ID

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;    //해시값으로 들어온 비밀번호

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified; //본인인증 여부

    @Column(name = "join_date", nullable = false)
    private OffsetDateTime joinDate; //가입일

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserPrivate userPrivate;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private VirtualAccount virtualAccount;

    @Column(name = "refresh_token")
    private String refreshToken;

    public void updateRefreshToken(String token) {
        this.refreshToken = token;
    }

}

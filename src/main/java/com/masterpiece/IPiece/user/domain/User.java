package com.masterpiece.IPiece.user.domain;

import com.masterpiece.IPiece.domain.account.VirtualAccount;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users") //DB테이블명 명시(PostgreSQL은 user가 예약어기 때문에 "" 명시)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //외부 직접 생성 방지
@AllArgsConstructor
@Builder
public class User {

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
    private LocalDateTime joinDate; //가입일

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt; //데이터 생성시간

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //마지막 수정시간

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserPrivate userPrivate;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private VirtualAccount virtualAccount;
}

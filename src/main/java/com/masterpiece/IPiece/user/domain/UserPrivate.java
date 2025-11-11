package com.masterpiece.IPiece.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Table(name = "user_private")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
public class UserPrivate {

    @Id
    @Column(name = "user_id")
    private Long userId;    //user_id를 PK로 사용

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;  //User의 user_id가 FK이다

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "id_card_img", nullable = false)
    private String idCardImg;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;
}
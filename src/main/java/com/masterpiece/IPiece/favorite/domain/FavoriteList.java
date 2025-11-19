package com.masterpiece.IPiece.favorite.domain;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "favorite_list", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class FavoriteList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long favoriteId;

    // 어떤 유저의 찜 목록인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

}

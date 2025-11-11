package com.masterpiece.IPiece.favorite.domain;

import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorite_list")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FavoriteList {

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

    @CreationTimestamp
    @Column(name = "create_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "update_at", columnDefinition = "timestamptz")
    private LocalDateTime updatedAt;


}

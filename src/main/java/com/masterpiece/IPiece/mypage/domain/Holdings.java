package com.masterpiece.IPiece.mypage.domain;



import com.masterpiece.IPiece.domain.account.VirtualAccount;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "holdings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Holdings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id")
    private Long holdingId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private VirtualAccount virtualAccount;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "avg_price", nullable = false)
    private Long avgBuyPrice;

    /*
    ******************product 연결부분******************
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    */

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;



}

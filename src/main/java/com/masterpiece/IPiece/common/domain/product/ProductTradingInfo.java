package com.masterpiece.IPiece.common.domain.product;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "product_trading_info")
public class ProductTradingInfo extends BaseEntity {

    @Id
    @Column(name = "product_id")
    private Long productId; // Product PK 공유

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "product_page_img", length = 255)
    private String productPageImg;

    @Column(name = "ip_description", length = 255)
    private String ipDescription;

}

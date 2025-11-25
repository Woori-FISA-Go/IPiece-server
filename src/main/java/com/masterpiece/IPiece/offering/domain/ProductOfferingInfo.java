package com.masterpiece.IPiece.offering.domain;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import com.masterpiece.IPiece.common.domain.product.Product;
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
@Table(name = "product_offering_info")
public class ProductOfferingInfo extends BaseEntity {

    @Id
    @Column(name = "product_id")
    private Long productId; // product 테이블과 PK 공유

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId // 공유 PK 매핑 (FK=PK)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "progress_rate")
    private Integer progressRate;

    @Column(name = "detail_img", length = 255, nullable = false, columnDefinition = "text")
    private String detailImg;

    @Column(name = "offering_start_date", columnDefinition = "timestamp", nullable = false)
    private OffsetDateTime offeringStartDate;

    @Column(name = "offering_end_date", columnDefinition = "timestamp", nullable = false)
    private OffsetDateTime offeringEndDate;

    @Column(name = "offering_amount", nullable = false)
    private Long offeringAmount;

    @Column(name = "offering_price", nullable = false)
    private Long offeringPrice;


    public void updateProgressRate(int rate) {
        this.progressRate = rate;
    }
}




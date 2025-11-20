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
@Table(name = "disclosure")
public class Disclosure extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL
    @Column(name = "disclosure_id")
    private Long disclosureId;

    @Column(name = "disclosure_date", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime disclosureDate;

    @Column(name = "disclosure_title", length = 100, nullable = false)
    private String disclosureTitle;

    @Column(name = "disclosure_url", length = 255, nullable = false)
    private String disclosureUrl;

    // DDL 그대로: disclosure.product_id → product_trading_info.product_id (N:1)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    private ProductTradingInfo productTradingInfo;

}

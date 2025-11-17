package com.masterpiece.IPiece.common.domain.product;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DDL과 1:1 매핑된 Product 엔티티
 * - 테이블: "product"
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;

    @Column(name = "owner", length = 100, nullable = false)
    private String owner;

    @Column(name = "current_price", nullable = false)
    private Long currentPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ProductStatus status;

    @Column(name = "present_img", length = 255, nullable = false)
    private String presentImg;

    @Column(name = "thumbnail_img", length = 255, nullable = false)
    private String thumbnailImg;

    @Column(name = "project_name", length = 100, nullable = false)
    private String projectName;

    @Column(name = "issue_date", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime issueDate;

    @Column(name = "issue_amount", nullable = false)
    private Long issueAmount;

    @Column(name = "token_standard", length = 20, nullable = false)
    private String tokenStandard;

    @Column(name = "exchange_listing", length = 100, nullable = false)
    private String exchangeListing;

    @Column(name = "last_price", nullable = false)
    private Long lastPrice;

    @Column(name = "token_quantity", nullable = false)
    private Long tokenQuantity;

    @Column(name = "dividend_ratio", precision = 4, scale = 1, nullable = false)
    private BigDecimal dividendRatio;

    @Column(name = "token_name", length = 100, nullable = false)
    private String tokenName;

    @Column(name = "token_contract_address", length = 42)
    private String tokenContractAddress;

    @Column(name = "dividend_contract_address", length = 42)
    private String dividendContractAddress;

    @Column(name = "token_symbol", length = 10)
    private String tokenSymbol;

    @Column(name = "deployment_tx_hash", length = 66)
    private String deploymentTxHash;

    @Column(name = "deployed_at")
    private LocalDateTime deployedAt;



}

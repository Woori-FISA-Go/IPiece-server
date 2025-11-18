package com.masterpiece.IPiece.common.domain.product;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "token_name", nullable = false, length = 50)
    private String tokenName;

    @Column(name = "total_token_quantity", nullable = false)
    private Long totalTokenQuantity;

    @Column(name = "current_price", nullable = false)
    private Long currentPrice;

    @Column(name = "last_price")
    private Long lastPrice;

    @Column(name = "thumbnail_img", length = 255)
    private String thumbnailImg;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status;

    @Column(name = "dividend_contract_address", length = 255)
    private String dividendContractAddress;

    @Column(name = "owner", length = 100)
    private String owner;

    @Column(name = "issue_date")
    private OffsetDateTime issueDate;

    @Column(name = "token_standard", length = 50)
    private String tokenStandard;

    @Column(name = "exchange_listing", length = 255)
    private String exchangeListing;

    @Column(name = "present_img", length = 255)
    private String presentImg;

    @Column(name = "deployed_at")
    private OffsetDateTime deployedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Holdings> holdings;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderBook> orderBooks;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TradeExecution> tradeExecutions;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dividends> dividends;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavoriteList> favoriteLists;

    //== 비즈니스 로직 ==//
    public void updateCurrentPrice(Long newPrice) {
        this.lastPrice = this.currentPrice;
        this.currentPrice = newPrice;
    }

    public String getProjectName() {
        return this.productName;
    }

    public Long getTokenQuantity() {
        return this.totalTokenQuantity;
    }
}
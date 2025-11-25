package com.masterpiece.IPiece.common.domain.product;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.dividends.domain.Dividends;
import com.masterpiece.IPiece.favorite.domain.FavoriteList;
import com.masterpiece.IPiece.market.domain.OrderBook;
import com.masterpiece.IPiece.market.domain.TradeExecution;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "token_name", nullable = false, length = 50)
    private String tokenName;

    @Column(name = "issue_amount")
    private Long issueAmount;

    @Column(name = "total_token_quantity", nullable = false)
    private Long totalTokenQuantity;

    @Column(name = "current_price", nullable = false)
    private Long currentPrice;

    @Column(name = "last_price")
    private Long lastPrice;

    @Column(name = "thumbnail_img", columnDefinition = "text")
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

    @Column(name = "present_img", columnDefinition = "text")
    private String presentImg;

    @Column(name = "deployed_at")
    private OffsetDateTime deployedAt;

    @Column(name = "token_contract_address", length = 42)
    private String tokenContractAddress;

    @Column(name = "token_symbol", length = 10)
    private String tokenSymbol;

    @Column(name = "dividend_ratio")
    private Double dividendRatio;

    @Column(name = "project_name", length = 100)
    private String projectName;

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
        return this.projectName;
    }

    public Long getTokenQuantity() {
        return this.totalTokenQuantity;
    }

    public void enableSecondaryTrading() {
        if (this.status == ProductStatus.TRADE) {
            // 이미 2차거래 상태
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_IN_TRADE_STATUS);
        }

        if (this.status != ProductStatus.OFFERING) {
            // 공모(OFFERING) 상태가 아닌 경우 → 상태 충돌
            throw new BusinessException(ErrorCode.PRODUCT_STATUS_NOT_OFFERING);
        }

        this.status = ProductStatus.TRADE;
    }
}
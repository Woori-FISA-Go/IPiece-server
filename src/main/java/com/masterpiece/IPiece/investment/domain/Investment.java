package com.masterpiece.IPiece.investment.domain;

import com.masterpiece.IPiece.investment.domain.InvestmentStatus;
import com.masterpiece.IPiece.investment.domain.InvestmentStepStatus;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "investment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Investment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "investment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "token_amount", nullable = false)
    private Long tokenAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvestmentStatus status;

    @Column(name = "whitelist_tx_hash", length = 66)
    private String whitelistTxHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "whitelist_status", nullable = false, length = 20)
    private InvestmentStepStatus whitelistStatus;

    @Column(name = "whitelist_completed_at")
    private OffsetDateTime whitelistCompletedAt;

    @Column(name = "transfer_tx_hash", length = 66)
    private String transferTxHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_status", nullable = false, length = 20)
    private InvestmentStepStatus transferStatus;

    @Column(name = "transfer_completed_at")
    private OffsetDateTime transferCompletedAt;

    public void recordWhitelistTx(String txHash) {
        this.whitelistTxHash = txHash;
        this.status = InvestmentStatus.PROCESSING;
        this.whitelistStatus = InvestmentStepStatus.PROCESSING;
        this.whitelistCompletedAt = OffsetDateTime.now();
    }

    public void recordTransferTx(String txHash) {
        this.transferTxHash = txHash;
        this.status = InvestmentStatus.COMPLETED;
        this.transferStatus = InvestmentStepStatus.COMPLETED;
        this.transferCompletedAt = OffsetDateTime.now();
    }

    public void markFailed() {
        this.status = InvestmentStatus.FAILED;
        if (this.whitelistStatus == InvestmentStepStatus.PROCESSING) {
            this.whitelistStatus = InvestmentStepStatus.FAILED;
        }
        if (this.transferStatus == InvestmentStepStatus.PROCESSING) {
            this.transferStatus = InvestmentStepStatus.FAILED;
        }
    }
}

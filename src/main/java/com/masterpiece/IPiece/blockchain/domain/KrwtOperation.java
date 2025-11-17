package com.masterpiece.IPiece.blockchain.domain;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "krwt_operations")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KrwtOperation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_id")
    private Long operationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 10)
    private OperationType operationType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "before_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal beforeBalance;

    @Column(name = "after_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal afterBalance;

    @Column(name = "tx_hash", length = 66)
    private String txHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "bank_transaction_id", length = 100)
    private String bankTransactionId;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private VirtualAccount virtualAccount;

    //== 비즈니스 로직 ==//
    public void complete(String txHash) {
        this.txHash = txHash;
        this.status = TransactionStatus.SUCCESS;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = TransactionStatus.FAILED;
        this.memo = errorMessage;
    }

    public void validateBalance() {
        if (operationType == OperationType.MINT) {
            if (afterBalance.compareTo(beforeBalance.add(amount)) != 0) {
                throw new IllegalStateException("입금 후 잔액이 일치하지 않습니다");
            }
        } else if (operationType == OperationType.BURN) {
            if (afterBalance.compareTo(beforeBalance.subtract(amount)) != 0) {
                throw new IllegalStateException("출금 후 잔액이 일치하지 않습니다");
            }
        }
    }
}

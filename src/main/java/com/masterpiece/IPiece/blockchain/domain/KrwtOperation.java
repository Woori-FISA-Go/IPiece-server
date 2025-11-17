package com.masterpiece.IPiece.blockchain.domain;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "krwt_operations")
@Getter
@Setter
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

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "before_balance", nullable = false)
    private Long beforeBalance;

    @Column(name = "after_balance", nullable = false)
    private Long afterBalance;

    @Column(name = "tx_hash", nullable = false, length = 66)
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
}

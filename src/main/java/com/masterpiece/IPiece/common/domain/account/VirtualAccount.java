package com.masterpiece.IPiece.common.domain.account;


import com.masterpiece.IPiece.common.domain.BaseEntity;
import com.masterpiece.IPiece.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "virtual_account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VirtualAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "account_no", nullable = false, unique = true, length = 30)
    private String accountNo;

    @Column(name = "balance_krw", nullable = false)
    private Long balanceKrw;

    @Column(name = "wallet_address", nullable = false, unique = true, length = 255)
    private String walletAddress;

    @Column(name = "pending_price")
    private Long pendingPrice;

    public void increaseBalanceKrw(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount to increase must be positive");
        }
        this.balanceKrw += amount;
    }

    public void decreaseBalanceKrw(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount to decrease must be positive");
        }
        if (this.balanceKrw < amount) {
            throw new IllegalStateException("Insufficient balance");
        }
        this.balanceKrw -= amount;
    }

    public void increasePendingPrice(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount to increase must be positive");
        }
        this.pendingPrice = (this.pendingPrice == null ? 0L : this.pendingPrice) + amount;
    }

    public void decreasePendingPrice(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount to decrease must be positive");
        }
        if (this.pendingPrice == null || this.pendingPrice < amount) {
            throw new IllegalStateException("Insufficient pending amount");
        }
        this.pendingPrice -= amount;
    }
}

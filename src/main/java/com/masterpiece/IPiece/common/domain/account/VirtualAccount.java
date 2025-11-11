package com.masterpiece.IPiece.common.domain.account;


import com.masterpiece.IPiece.dividends.domain.DividendPayouts;
import com.masterpiece.IPiece.market.domain.OrderBook;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import com.masterpiece.IPiece.offering.domain.OfferingSubscriptions;
import com.masterpiece.IPiece.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "virtual_account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VirtualAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "account_no", nullable = false, unique = true, length = 30)
    private String accountNo;

    @Column(name = "balance_krw", nullable = false)
    private Long balanceKrw;

    @Column(name = "wallet_address", nullable = false, unique = true, length = 255)
    private String walletAddress;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private LocalDateTime updateAt;

    @CreationTimestamp
    @Column(name = "create_at", columnDefinition = "timestamptz", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @Column(name = "pending_price")
    private Long pendingPrice;



}

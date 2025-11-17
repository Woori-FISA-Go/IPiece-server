package com.masterpiece.IPiece.offering.domain;


import com.masterpiece.IPiece.common.domain.BaseEntity;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.OffsetDateTime;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    @Entity
    @Table(name = "offering_subscriptions")
    public class OfferingSubscriptions extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "subscription_id")
        private Long subscriptionId;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "account_id", nullable = false)
        private VirtualAccount virtualAccount;

        @Column(name = "applied_quantity", nullable = false)
        private Long appliedQuantity;

        @Column(name = "applied_amount_krw", nullable = false)
        private Long appliedAmountKrw;

        @Enumerated(EnumType.STRING) // ← status를 Enum으로 매핑
        @Column(name = "status", length = 16, nullable = false)
        private OfferingStatus status;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "product_id", nullable = false)
        private ProductOfferingInfo productOfferingInfo;
    }


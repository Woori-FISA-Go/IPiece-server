package com.masterpiece.IPiece.offering.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    @Entity
    @Table(name = "offering_subscriptions")
    public class OfferingSubscriptions {

        @Id
        @Column(name = "subscription_id")
        private Long subscriptionId;

        @Column(name = "applied_quantity", nullable = false)
        private Long appliedQuantity;

        @Column(name = "applied_amount_krw", nullable = false)
        private Long appliedAmountKrw;

        @Enumerated(EnumType.STRING) // ← status를 Enum으로 매핑
        @Column(name = "status", length = 16, nullable = false)
        private OfferingStatus status;

        @Column(name = "created_at", columnDefinition = "timestamptz", nullable = false)
        private OffsetDateTime createdAt;

        @Column(name = "account_id", length = 20, nullable = false)
        private String accountId;

        @Column(name = "product_id", nullable = false)
        private Long productId;

        @Column(name = "create_at", columnDefinition = "timestamptz", nullable = false)
        private OffsetDateTime createAt;

        // DDL 상 updated_at 이 VARCHAR(255) 이므로 그대로 매핑 (추후 TIMESTAMPTZ 권장)
        @Column(name = "updated_at", columnDefinition = "timestamptz")
        private OffsetDateTime updatedAt;
    }


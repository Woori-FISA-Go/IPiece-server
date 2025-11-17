package com.masterpiece.IPiece.blockchain.domain;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import com.masterpiece.IPiece.common.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blockchain_tokens")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockchainToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    @Column(name = "contract_address", nullable = false, unique = true, length = 42)
    private String contractAddress;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "symbol", nullable = false, length = 10)
    private String symbol;

    @Column(name = "total_supply", nullable = false)
    private Long totalSupply;

    @Column(name = "decimals", nullable = false)
    @Builder.Default
    private Integer decimals = 0;

    @Column(name = "owner_address", nullable = false, length = 42)
    private String ownerAddress;

    @Column(name = "deployed_at", nullable = false)
    private LocalDateTime deployedAt;

    @Column(name = "deployment_tx_hash", length = 66)
    private String deploymentTxHash;

    // 연관관계
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}

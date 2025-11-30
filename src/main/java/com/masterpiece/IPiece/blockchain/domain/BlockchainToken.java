package com.masterpiece.IPiece.blockchain.domain;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "blockchain_token")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BlockchainToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    @Column(name = "contract_address", nullable = false, unique = true, length = 42)
    private String contractAddress;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "symbol", nullable = false, unique = true)
    private String symbol;

    @Column(name = "total_supply", nullable = false)
    private Long totalSupply;

    @Column(name = "face_value", nullable = false)
    private Long faceValue;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "transaction_hash", nullable = false, unique = true, length = 66)
    private String transactionHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TokenStatus status; // Enum for token deployment status

    public void updateStatus(TokenStatus status) {
        this.status = status;
    }
}

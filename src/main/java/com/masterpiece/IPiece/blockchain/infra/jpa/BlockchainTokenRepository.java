package com.masterpiece.IPiece.blockchain.infra.jpa;

import com.masterpiece.IPiece.blockchain.domain.BlockchainToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockchainTokenRepository extends JpaRepository<BlockchainToken, Long> {
    Optional<BlockchainToken> findByContractAddress(String contractAddress);
    Optional<BlockchainToken> findByProductProductId(Long productId);
}

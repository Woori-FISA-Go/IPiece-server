package com.masterpiece.IPiece.blockchain.infra.jpa;

import com.masterpiece.IPiece.blockchain.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserId(Long userId);
    Optional<Wallet> findByAddress(String address);
}

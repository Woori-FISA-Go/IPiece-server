package com.masterpiece.IPiece.common.domain.infra;

import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Long> {

    // 특정 유저의 가상계좌 조회
    Optional<VirtualAccount> findByUser_UserId(Long userId);

    // 지갑 주소로 조회
    Optional<VirtualAccount> findByWalletAddress(String walletAddress);

}

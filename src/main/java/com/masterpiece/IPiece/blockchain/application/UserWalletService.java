package com.masterpiece.IPiece.blockchain.application;

import com.masterpiece.IPiece.blockchain.domain.Wallet;
import com.masterpiece.IPiece.blockchain.infra.jpa.WalletRepository;
import com.masterpiece.IPiece.common.exception.BlockchainException;
import com.masterpiece.IPiece.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

/**
 * 유저별 온체인 지갑 주소 관리 서비스.
 *
 * - 현재 단계에서는 "유효한 EVM EOA 주소"만 생성해서 DB에 저장한다.
 * - 생성 시 사용된 private key는 이 서비스 안에서만 일시적으로 사용하고,
 *   별도로 보관하지 않는다.
 *   (유저가 직접 온체인 송금을 하지 않는 구조이므로,
 *    지금은 주소만 있으면 충분하다는 전제)
 *
 * ⚠️ 나중에 "유저가 온체인에서 직접 송금/승인해야 하는" 요구가 생기면,
 *     여기서 생성한 private key를 안전하게 암호화/저장/복구하는
 *     별도 모듈을 도입해야 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserWalletService {

    private final WalletRepository walletRepository;

    /**
     * 해당 유저의 온체인 지갑을 반환.
     * - 이미 있으면 그대로 반환
     * - 없으면 새 address를 생성해서 저장 후 반환
     */
    public Wallet getOrCreateWallet(User user) {
        return walletRepository.findByUserId(user.getUserId())
                .orElseGet(() -> createNewWallet(user));
    }

    private Wallet createNewWallet(User user) {
        try {
            // 1) 새 EOA 키쌍 생성
            ECKeyPair keyPair = Keys.createEcKeyPair();
            String addressHex = Keys.getAddress(keyPair.getPublicKey()); // 40자리 hex (0x 제외)
            String address = "0x" + addressHex;

            // 2) Wallet 엔티티에 address 저장
            Wallet wallet = Wallet.builder()
                    .userId(user.getUserId())
                    .address(address)
                    .build();

            Wallet saved = walletRepository.save(wallet);

            log.info("[UserWalletService] Created on-chain wallet for user {}: {}",
                    user.getUserId(), address);

            // ⚠️ 현재는 keyPair.getPrivateKey() 를 어디에도 저장하지 않는다.
            //    토큰 전송은 admin 지갑 → user address 방향으로만 발생하며,
            //    출금은 오프체인으로 처리하기 때문.
            //    나중에 유저 outbound 트랜잭션이 필요해지면 이 부분을 확장해야 한다.

            return saved;
        } catch (Exception e) {
            log.error("[UserWalletService] Failed to create wallet for user {}: {}",
                    user.getUserId(), e.getMessage(), e);
            throw new BlockchainException("Failed to create on-chain wallet for user " + user.getUserId(), e);
        }
    }
}
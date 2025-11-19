package com.masterpiece.IPiece.blockchain.application;

import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtBalanceResponse;
import com.masterpiece.IPiece.blockchain.domain.Wallet;
import com.masterpiece.IPiece.blockchain.infra.jpa.WalletRepository;
import com.masterpiece.IPiece.common.exception.BlockchainException;
import com.masterpiece.IPiece.common.exception.WalletNotFoundException;
import com.masterpiece.IPiece.integration.besu.BesuClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockchainServiceTest {

    @Mock
    private BesuClient besuClient;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private BlockchainService blockchainService;

    private Long userId;
    private String walletAddress;
    private Wallet mockWallet;

    @BeforeEach
    void setUp() {
        userId = 1L;
        walletAddress = "0x1234567890abcdef";
        mockWallet = Wallet.builder()
                .userId(userId)
                .address(walletAddress)
                .build();
    }

    @Test
    @DisplayName("KRWT 잔고를 성공적으로 조회한다")
    void getKrwtBalance_Success() {
        // Given
        BigDecimal expectedBalance = new BigDecimal("12345.67");
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(mockWallet));
        when(besuClient.getKrwtBalance(walletAddress)).thenReturn(expectedBalance);

        // When
        KrwtBalanceResponse response = blockchainService.getKrwtBalance(userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getBalance()).isEqualTo(expectedBalance);
    }

    @Test
    @DisplayName("사용자에게 지갑이 없을 경우 WalletNotFoundException을 던진다")
    void getKrwtBalance_WalletNotFound() {
        // Given
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(WalletNotFoundException.class, () -> {
            blockchainService.getKrwtBalance(userId);
        });
    }

    @Test
    @DisplayName("BesuClient에서 예외가 발생할 경우 BlockchainException을 던진다")
    void getKrwtBalance_BlockchainError() {
        // Given
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(mockWallet));
        when(besuClient.getKrwtBalance(walletAddress)).thenThrow(new RuntimeException("Connection failed"));

        // When & Then
        assertThrows(BlockchainException.class, () -> {
            blockchainService.getKrwtBalance(userId);
        });
    }
}

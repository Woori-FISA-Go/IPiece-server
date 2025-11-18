package com.masterpiece.IPiece.blockchain.application;

import com.masterpiece.IPiece.blockchain.api.dto.response.MyWalletResponse;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountRepository;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import com.masterpiece.IPiece.mypage.infra.HoldingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private VirtualAccountRepository virtualAccountRepository;

    @Mock
    private HoldingsRepository holdingsRepository;

    @InjectMocks
    private WalletService walletService;

    private Long userId;
    private VirtualAccount mockVirtualAccount;

    @BeforeEach
    void setUp() {
        userId = 1L;
        mockVirtualAccount = VirtualAccount.builder()
                .user(null) // User entity is not relevant for this test, can be null
                .walletAddress("0x123abc")
                .balanceKrw(100000L)
                .build();
    }

    @Test
    @DisplayName("사용자 ID로 지갑 정보를 성공적으로 조회한다 (보유 토큰 없음)")
    void getMyWallet_Success_NoHoldings() {
        // Given
        when(virtualAccountRepository.findByUser_UserId(userId)).thenReturn(Optional.of(mockVirtualAccount));
        when(holdingsRepository.findAllByVirtualAccount(any(VirtualAccount.class))).thenReturn(Collections.emptyList());

        // When
        MyWalletResponse response = walletService.getMyWallet(userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getWalletAddress()).isEqualTo(mockVirtualAccount.getWalletAddress());
        assertThat(response.getBalanceKrw()).isEqualTo(mockVirtualAccount.getBalanceKrw());
        assertThat(response.getCreatedAt()).isEqualTo(mockVirtualAccount.getCreatedAt());
        assertThat(response.getTokens()).isEmpty();
        assertThat(response.getTotalValueKrw()).isEqualTo(mockVirtualAccount.getBalanceKrw());
    }

    @Test
    @DisplayName("사용자 ID로 지갑 정보를 성공적으로 조회한다 (보유 토큰 있음)")
    void getMyWallet_Success_WithHoldings() {
        // Given
        Product mockProduct = Product.builder()
                .productId(1L)
                .productName("Test Product")
                .tokenName("TEST")
                .totalTokenQuantity(1000L)
                .currentPrice(500L)
                .build();

        Holdings mockHolding = Holdings.builder()
                .quantity(10L)
                .product(mockProduct)
                .build();

        when(virtualAccountRepository.findByUser_UserId(userId)).thenReturn(Optional.of(mockVirtualAccount));
        when(holdingsRepository.findAllByVirtualAccount(any(VirtualAccount.class))).thenReturn(List.of(mockHolding));

        // When
        MyWalletResponse response = walletService.getMyWallet(userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTokens()).hasSize(1);
        MyWalletResponse.TokenInfo tokenInfo = response.getTokens().get(0);
        assertThat(tokenInfo.getProjectName()).isEqualTo("Test Product");
        assertThat(tokenInfo.getBalance()).isEqualTo(10L);
        assertThat(tokenInfo.getSharePercentage()).isEqualTo("1.0000%");

        long expectedTotalValue = (10L * 500L) + 100000L;
        assertThat(response.getTotalValueKrw()).isEqualTo(expectedTotalValue);
    }

    @Test
    @DisplayName("사용자 ID에 해당하는 지갑이 없을 경우 예외를 발생시킨다")
    void getMyWallet_WalletNotFound() {
        // Given
        when(virtualAccountRepository.findByUser_UserId(anyLong())).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> walletService.getMyWallet(userId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(exception.getMessage()).contains("has no virtual account");
    }
}

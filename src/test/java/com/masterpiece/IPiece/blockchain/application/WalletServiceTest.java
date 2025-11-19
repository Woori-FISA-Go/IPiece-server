package com.masterpiece.IPiece.blockchain.application;

import com.masterpiece.IPiece.blockchain.api.dto.request.KrwtBurnRequest;
import com.masterpiece.IPiece.blockchain.api.dto.request.KrwtMintRequest;
import com.masterpiece.IPiece.blockchain.api.dto.request.TransactionQueryRequest;
import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtBurnResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtMintResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.MyWalletResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.TransactionListResponse;
import com.masterpiece.IPiece.blockchain.domain.KrwtOperation;
import com.masterpiece.IPiece.blockchain.domain.OperationType;
import com.masterpiece.IPiece.blockchain.domain.TransactionStatus;
import com.masterpiece.IPiece.blockchain.infra.jpa.KrwtOperationRepository;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private VirtualAccountRepository virtualAccountRepository;

    @Mock
    private HoldingsRepository holdingsRepository;

    @Mock
    private KrwtOperationRepository krwtOperationRepository;

    @InjectMocks
    private WalletService walletService;

    private Long userId;
    private Long adminUserId;
    private VirtualAccount mockVirtualAccount;
    private User mockUser;
    private static final long MAX_MINT_AMOUNT = 100_000_000L; // 1억원

    @BeforeEach
    void setUp() {
        userId = 1L;
        adminUserId = 99L; // Assuming an admin user ID
        mockUser = User.builder().userId(userId).build();
        mockVirtualAccount = VirtualAccount.builder()
                .user(mockUser)
                .walletAddress("0x123abc")
                .balanceKrw(100000L)
                .createdAt(OffsetDateTime.now())
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
                .virtualAccount(mockVirtualAccount) // Ensure virtualAccount is set
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

    @Test
    @DisplayName("거래 내역을 성공적으로 조회한다 (ALL 타입)")
    void getTransactions_Success_AllType() {
        // Given
        TransactionQueryRequest request = TransactionQueryRequest.builder().page(0).size(10).type("ALL").build();
        List<KrwtOperation> operations = List.of(
                KrwtOperation.builder().operationId(1L).operationType(OperationType.MINT).amount(BigDecimal.valueOf(1000L)).beforeBalance(BigDecimal.ZERO).afterBalance(BigDecimal.valueOf(1000L)).txHash("0x1").createdAt(OffsetDateTime.now()).memo("deposit").user(mockUser).virtualAccount(mockVirtualAccount).build(),
                KrwtOperation.builder().operationId(2L).operationType(OperationType.BURN).amount(BigDecimal.valueOf(500L)).beforeBalance(BigDecimal.valueOf(1000L)).afterBalance(BigDecimal.valueOf(500L)).txHash("0x2").createdAt(OffsetDateTime.now()).memo("withdrawal").user(mockUser).virtualAccount(mockVirtualAccount).build()
        );
        Page<KrwtOperation> page = new PageImpl<>(operations, PageRequest.of(0, 10), 2);

        when(virtualAccountRepository.findByUser_UserId(userId)).thenReturn(Optional.of(mockVirtualAccount));
        when(krwtOperationRepository.findByUserUserId(anyLong(), any(Pageable.class))).thenReturn(page);

        // When
        TransactionListResponse response = walletService.getTransactions(userId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTransactions()).hasSize(2);
        assertThat(response.getTransactions().get(0).getType()).isEqualTo(OperationType.MINT.name());
        verify(krwtOperationRepository, times(1)).findByUserUserId(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("거래 내역을 성공적으로 조회한다 (MINT 타입)")
    void getTransactions_Success_MintType() {
        // Given
        TransactionQueryRequest request = TransactionQueryRequest.builder().page(0).size(10).type("MINT").build();
        List<KrwtOperation> operations = List.of(
                KrwtOperation.builder().operationId(1L).operationType(OperationType.MINT).amount(BigDecimal.valueOf(1000L)).beforeBalance(BigDecimal.ZERO).afterBalance(BigDecimal.valueOf(1000L)).txHash("0x1").createdAt(OffsetDateTime.now()).memo("deposit").user(mockUser).virtualAccount(mockVirtualAccount).build()
        );
        Page<KrwtOperation> page = new PageImpl<>(operations, PageRequest.of(0, 10), 1);

        when(virtualAccountRepository.findByUser_UserId(userId)).thenReturn(Optional.of(mockVirtualAccount));
        when(krwtOperationRepository.findByUserUserIdAndOperationType(anyLong(), any(OperationType.class), any(Pageable.class))).thenReturn(page);

        // When
        TransactionListResponse response = walletService.getTransactions(userId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTransactions()).hasSize(1);
        assertThat(response.getTransactions().get(0).getType()).isEqualTo(OperationType.MINT.name());
        verify(krwtOperationRepository, times(1)).findByUserUserIdAndOperationType(anyLong(), any(OperationType.class), any(Pageable.class));
    }

    @Test
    @DisplayName("KRWT를 성공적으로 발행(입금)한다")
    @Transactional // Ensure transaction for save operations
    void mintKrwt_Success() {
        // Given
        KrwtMintRequest request = KrwtMintRequest.builder().userId(userId).amount(50000L).memo("Initial mint").build();
        long initialBalance = mockVirtualAccount.getBalanceKrw();

        when(virtualAccountRepository.findByUser_UserId(userId)).thenReturn(Optional.of(mockVirtualAccount));
        when(virtualAccountRepository.save(any(VirtualAccount.class))).thenReturn(mockVirtualAccount);
        when(krwtOperationRepository.save(any(KrwtOperation.class))).thenAnswer(invocation -> {
            KrwtOperation op = invocation.getArgument(0);
            // op.setOperationId(1L); // Simulate ID generation - removed as ID is generated by DB
            return op;
        });

        // When
        KrwtMintResponse response = walletService.mintKrwt(adminUserId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getPreviousBalance()).isEqualTo(initialBalance);
        assertThat(response.getMintAmount()).isEqualTo(request.getAmount());
        assertThat(response.getNewBalance()).isEqualTo(initialBalance + request.getAmount());
        assertThat(mockVirtualAccount.getBalanceKrw()).isEqualTo(initialBalance + request.getAmount()); // Verify balance updated
        verify(virtualAccountRepository, times(1)).save(mockVirtualAccount);
        verify(krwtOperationRepository, times(1)).save(any(KrwtOperation.class));
    }

    @Test
    @DisplayName("KRWT 발행 시 금액이 0 이하이면 예외를 던진다")
    void mintKrwt_InvalidAmount_Zero() {
        // Given
        KrwtMintRequest request = KrwtMintRequest.builder().userId(userId).amount(0L).memo("Invalid mint").build();

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> walletService.mintKrwt(adminUserId, request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_AMOUNT);
    }

    @Test
    @DisplayName("KRWT 발행 시 금액이 최대 발행 금액을 초과하면 예외를 던진다")
    void mintKrwt_AmountExceedsLimit() {
        // Given
        KrwtMintRequest request = KrwtMintRequest.builder().userId(userId).amount(MAX_MINT_AMOUNT + 1).memo("Exceed limit").build();

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> walletService.mintKrwt(adminUserId, request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_AMOUNT);
    }

    @Test
    @DisplayName("KRWT 발행 시 입금 받을 사용자를 찾을 수 없으면 예외를 던진다")
    void mintKrwt_UserNotFound() {
        // Given
        KrwtMintRequest request = KrwtMintRequest.builder().userId(999L).amount(1000L).memo("Mint to non-existent user").build();
        when(virtualAccountRepository.findByUser_UserId(request.getUserId())).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> walletService.mintKrwt(adminUserId, request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(exception.getMessage()).contains("입금 받을 사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("KRWT를 성공적으로 소각(출금)한다")
    @Transactional // Ensure transaction for save operations
    void burnKrwt_Success() {
        // Given
        KrwtBurnRequest request = KrwtBurnRequest.builder().amount(50000L).memo("Withdrawal").build();
        long initialBalance = mockVirtualAccount.getBalanceKrw();

        when(virtualAccountRepository.findByUser_UserId(userId)).thenReturn(Optional.of(mockVirtualAccount));
        when(virtualAccountRepository.save(any(VirtualAccount.class))).thenReturn(mockVirtualAccount);
        when(krwtOperationRepository.save(any(KrwtOperation.class))).thenAnswer(invocation -> {
            KrwtOperation op = invocation.getArgument(0);
            // op.setOperationId(2L); // Simulate ID generation - removed as ID is generated by DB
            return op;
        });

        // When
        KrwtBurnResponse response = walletService.burnKrwt(userId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getPreviousBalance()).isEqualTo(initialBalance);
        assertThat(response.getBurnAmount()).isEqualTo(request.getAmount());
        assertThat(response.getNewBalance()).isEqualTo(initialBalance - request.getAmount());
        assertThat(mockVirtualAccount.getBalanceKrw()).isEqualTo(initialBalance - request.getAmount()); // Verify balance updated
        verify(virtualAccountRepository, times(1)).save(mockVirtualAccount);
        verify(krwtOperationRepository, times(1)).save(any(KrwtOperation.class));
    }

    @Test
    @DisplayName("KRWT 소각 시 금액이 0 이하이면 예외를 던진다")
    void burnKrwt_InvalidAmount_Zero() {
        // Given
        KrwtBurnRequest request = KrwtBurnRequest.builder().amount(0L).memo("Invalid burn").build();

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> walletService.burnKrwt(userId, request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_AMOUNT);
    }

    @Test
    @DisplayName("KRWT 소각 시 잔고가 부족하면 예외를 던진다")
    void burnKrwt_InsufficientFunds() {
        // Given
        KrwtBurnRequest request = KrwtBurnRequest.builder().amount(200000L).memo("Insufficient funds").build(); // More than initial 100000L
        when(virtualAccountRepository.findByUser_UserId(userId)).thenReturn(Optional.of(mockVirtualAccount));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> walletService.burnKrwt(userId, request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_BALANCE);
    }

    @Test
    @DisplayName("KRWT 소각 시 사용자 지갑을 찾을 수 없으면 예외를 던진다")
    void burnKrwt_UserNotFound() {
        // Given
        KrwtBurnRequest request = KrwtBurnRequest.builder().amount(1000L).memo("Burn from non-existent user").build();
        when(virtualAccountRepository.findByUser_UserId(userId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> walletService.burnKrwt(userId, request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(exception.getMessage()).contains("사용자 지갑을 찾을 수 없습니다.");
    }
}
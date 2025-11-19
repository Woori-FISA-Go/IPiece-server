package com.masterpiece.IPiece.blockchain.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterpiece.IPiece.blockchain.api.dto.request.KrwtBurnRequest;
import com.masterpiece.IPiece.blockchain.api.dto.request.KrwtMintRequest;
import com.masterpiece.IPiece.blockchain.api.dto.request.TransactionQueryRequest;
import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtBurnResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtMintResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.MyWalletResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.TransactionListResponse;
import com.masterpiece.IPiece.blockchain.application.WalletService;
import com.masterpiece.IPiece.blockchain.domain.OperationType;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.user.application.CustomUserDetailsService;
import com.masterpiece.IPiece.common.util.JwtTokenProvider;
import com.masterpiece.IPiece.config.JwtAuthenticationFilter;
import com.masterpiece.IPiece.config.WebConfig;
import com.masterpiece.IPiece.integration.besu.BesuClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("TODO: 컨트롤러 테스트 환경의 근본적인 문제 해결 후 활성화 (#??)")
@WebMvcTest(WalletController.class)
@Import(WebConfig.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WalletService walletService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    // For SecurityConfig to load
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private BesuClient besuClient; // Added for BesuClient dependency in WalletService

    @Test
    @WithMockUser(username = "1", roles = "USER")
    void 자신의_지갑_정보를_성공적으로_조회한다() throws Exception {
        // given
        Long userId = 1L;
        MyWalletResponse response = MyWalletResponse.builder()
            .walletAddress("0x1234567890abcdef")
            .balanceKrw(1000000L)
            .tokens(Collections.emptyList())
            .totalValueKrw(1000000L)
            .createdAt(OffsetDateTime.now())
            .build();

        when(walletService.getMyWallet(userId)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/v1/blockchain/wallet/my"))
            .andDo(print())  // ← 응답 출력
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.walletAddress").value("0x1234567890abcdef"))
            .andExpect(jsonPath("$.balanceKrw").value(1000000))
            .andExpect(jsonPath("$.totalValueKrw").value(1000000));

        // verify
        verify(walletService).getMyWallet(userId);
    }

    @Test
    @WithMockUser(username = "999", roles = "USER")
    void 지갑_정보가_없는_경우_404_Not_Found_응답을_받는다() throws Exception {
        // given
        Long userId = 999L;
        when(walletService.getMyWallet(userId))
            .thenThrow(new BusinessException(ErrorCode.NOT_FOUND));

        // when & then
        mockMvc.perform(get("/v1/blockchain/wallet/my"))
            .andDo(print())
            .andExpect(status().isNotFound());
        
        // verify
        verify(walletService).getMyWallet(userId);
    }

    @Test
    void 인증되지_않은_사용자는_지갑_정보를_조회할_수_없다() throws Exception {
        mockMvc.perform(get("/v1/blockchain/wallet/my"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    void 거래_내역을_성공적으로_조회한다() throws Exception {
        // Given
        Long userId = 1L;
        TransactionListResponse.TransactionItem item = TransactionListResponse.TransactionItem.builder()
                .journalId(1L)
                .type(OperationType.MINT.name())
                .amount(1000L)
                .balanceAfter(1000L)
                .memo("Test Deposit")
                .transactionHash("0xabc")
                .createdAt(OffsetDateTime.now())
                .build();
        TransactionListResponse mockResponse = TransactionListResponse.builder()
                .totalElements(1L)
                .totalPages(1)
                .currentPage(0)
                .pageSize(10)
                .transactions(List.of(item))
                .build();

        when(walletService.getTransactions(eq(userId), any(TransactionQueryRequest.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/v1/blockchain/wallet/transactions")
                        .param("page", "0")
                        .param("size", "10")
                        .param("type", "ALL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1L))
                .andExpect(jsonPath("$.transactions[0].type").value(OperationType.MINT.name()));

        // Verify
        verify(walletService).getTransactions(eq(userId), any(TransactionQueryRequest.class));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void KRWT를_성공적으로_발행한다() throws Exception {
        // Given
        Long adminUserId = 1L;
        KrwtMintRequest request = KrwtMintRequest.builder().userId(2L).amount(10000L).memo("Admin Mint").build();
        KrwtMintResponse mockResponse = KrwtMintResponse.builder()
                .transactionId(1L)
                .userId(2L)
                .previousBalance(0L)
                .mintAmount(10000L)
                .newBalance(10000L)
                .transactionHash("0xmintTxHash")
                .completedAt(OffsetDateTime.now())
                .build();

        when(walletService.mintKrwt(eq(adminUserId), any(KrwtMintRequest.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/v1/blockchain/wallet/krwt/mint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mintAmount").value(10000L));

        // Verify
        verify(walletService).mintKrwt(eq(adminUserId), any(KrwtMintRequest.class));
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    void KRWT_발행은_ADMIN_권한이_없으면_실패한다() throws Exception {
        // Given
        KrwtMintRequest request = KrwtMintRequest.builder().userId(2L).amount(10000L).memo("Admin Mint").build();

        // When & Then
        mockMvc.perform(post("/v1/blockchain/wallet/krwt/mint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden()); // 403 Forbidden
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    void KRWT를_성공적으로_소각한다() throws Exception {
        // Given
        Long userId = 1L;
        KrwtBurnRequest request = KrwtBurnRequest.builder().amount(5000L).memo("User Burn").build();
        KrwtBurnResponse mockResponse = KrwtBurnResponse.builder()
                .transactionId(2L)
                .userId(1L)
                .previousBalance(10000L)
                .burnAmount(5000L)
                .newBalance(5000L)
                .transactionHash("0xburnTxHash")
                .completedAt(OffsetDateTime.now())
                .build();

        when(walletService.burnKrwt(eq(userId), any(KrwtBurnRequest.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/v1/blockchain/wallet/krwt/burn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.burnAmount").value(5000L));

        // Verify
        verify(walletService).burnKrwt(eq(userId), any(KrwtBurnRequest.class));
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    void KRWT_소각_시_금액이_유효하지_않으면_실패한다() throws Exception {
        // Given
        KrwtBurnRequest request = KrwtBurnRequest.builder().amount(0L).memo("Invalid Burn").build();

        // When & Then
        mockMvc.perform(post("/v1/blockchain/wallet/krwt/burn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest()); // 400 Bad Request
    }
}
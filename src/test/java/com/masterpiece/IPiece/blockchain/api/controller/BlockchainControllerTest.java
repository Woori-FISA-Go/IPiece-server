package com.masterpiece.IPiece.blockchain.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterpiece.IPiece.blockchain.api.BlockchainController;
import com.masterpiece.IPiece.blockchain.api.dto.request.TokenTransferRequest;
import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtBalanceResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.TokenTransferResponse;
import com.masterpiece.IPiece.blockchain.application.BlockchainService;
import com.masterpiece.IPiece.config.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BlockchainController.class)
@Import(TestConfig.class)
@TestPropertySource(properties = "blockchain.enabled=true")
class BlockchainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BlockchainService blockchainService;

    @Test
    @DisplayName("인증된 사용자는 자신의 KRWT 잔고를 조회할 수 있다")
    @WithMockUser(username = "1", authorities = "ROLE_USER")
    void getKrwtBalance_Success() throws Exception {
        // Given
        BigDecimal balance = new BigDecimal("12345.67");
        KrwtBalanceResponse mockResponse = KrwtBalanceResponse.builder()
            .balance(balance)
            .build();
        
        when(blockchainService.getKrwtBalance(anyLong())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/v1/blockchain/wallet/krwt")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(balance.doubleValue()));
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 KRWT 잔고를 조회할 수 없다")
    void getKrwtBalance_Unauthorized() throws Exception {
        mockMvc.perform(get("/v1/blockchain/wallet/krwt")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ADMIN은 토큰을 다른 주소로 전송할 수 있다")
    @WithMockUser(username = "2", authorities = {"ROLE_ADMIN", "ROLE_USER"}) // "admin" -> "2"
    void transferToken_Success() throws Exception {
        // Given
        String contractAddress = "0x1234567890123456789012345678901234567890";
        TokenTransferRequest request = TokenTransferRequest.builder()
                .toAddress("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd")
                .amount(100)
                .investmentId("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
                .build();
        TokenTransferResponse mockResponse = TokenTransferResponse.builder()
                .fromAddress("0xAdminWalletAddress")
                .toAddress(request.getToAddress())
                .amount(request.getAmount())
                .transactionHash("0xmockTransactionHash123")
                .transferredAt(OffsetDateTime.now())
                .build();

        when(blockchainService.transferToken(anyString(), any(TokenTransferRequest.class), anyLong()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/v1/blockchain/tokens/{address}/transfer", contractAddress)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromAddress").value(mockResponse.getFromAddress()))
                .andExpect(jsonPath("$.toAddress").value(mockResponse.getToAddress()))
                .andExpect(jsonPath("$.amount").value(mockResponse.getAmount()))
                .andExpect(jsonPath("$.transactionHash").value(mockResponse.getTransactionHash()));

        verify(blockchainService).transferToken(anyString(), any(TokenTransferRequest.class), anyLong());
    }

    @Test
    @DisplayName("USER는 토큰을 전송할 수 없다 (권한 없음)")
    @WithMockUser(username = "1", authorities = "ROLE_USER")
    void transferToken_Forbidden() throws Exception {
        // Given
        String contractAddress = "0x1234567890123456789012345678901234567890";
        TokenTransferRequest request = TokenTransferRequest.builder()
                .toAddress("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd")
                .amount(100)
                .build();

        // When & Then
        mockMvc.perform(post("/v1/blockchain/tokens/{address}/transfer", contractAddress)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 토큰을 전송할 수 없다")
    void transferToken_Unauthorized() throws Exception {
        // Given
        String contractAddress = "0x1234567890123456789012345678901234567890";
        TokenTransferRequest request = TokenTransferRequest.builder()
                .toAddress("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd")
                .amount(100)
                .build();

        // When & Then
        mockMvc.perform(post("/v1/blockchain/tokens/{address}/transfer", contractAddress)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}

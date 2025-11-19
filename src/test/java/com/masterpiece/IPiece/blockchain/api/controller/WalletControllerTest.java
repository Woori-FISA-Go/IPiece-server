package com.masterpiece.IPiece.blockchain.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterpiece.IPiece.blockchain.api.dto.response.MyWalletResponse;
import com.masterpiece.IPiece.blockchain.application.WalletService;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WalletService walletService;
    
    @Test
    @DisplayName("자신의 지갑 정보를 성공적으로 조회한다")
    @WithMockUser(username = "1", roles = "USER")    void getMyWalletInfo_Success() throws Exception {
        // Given
        MyWalletResponse mockResponse = MyWalletResponse.builder()
                .walletAddress("0x123abc")
                .balanceKrw(100000L)
                .createdAt(OffsetDateTime.now())
                .tokens(Collections.emptyList())
                .totalValueKrw(100000L)
                .build();

        when(walletService.getMyWallet(anyLong())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/v1/blockchain/wallet/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletAddress").value(mockResponse.getWalletAddress()))
                .andExpect(jsonPath("$.balanceKrw").value(mockResponse.getBalanceKrw()))
                .andExpect(jsonPath("$.totalValueKrw").value(mockResponse.getTotalValueKrw()))
                .andExpect(jsonPath("$.tokens").isEmpty());
    }

    @Test
    @DisplayName("지갑 정보가 없는 경우 404 Not Found 응답을 받는다")
    @WithMockUser(username = "2", roles = "USER")
    void getMyWalletInfo_WalletNotFound_NotFound() throws Exception {
        // Given
        when(walletService.getMyWallet(anyLong()))
                .thenThrow(new BusinessException(ErrorCode.NOT_FOUND, "User has no virtual account"));

        // When & Then
        mockMvc.perform(get("/v1/blockchain/wallet/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value(ErrorCode.NOT_FOUND.name()));
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 지갑 정보를 조회할 수 없다 (401 Unauthorized)")
    void getMyWalletInfo_UnauthenticatedUser_Unauthorized() throws Exception {
        mockMvc.perform(get("/v1/blockchain/wallet/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}


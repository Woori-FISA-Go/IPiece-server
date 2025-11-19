package com.masterpiece.IPiece.blockchain.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterpiece.IPiece.blockchain.api.BlockchainController;
import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtBalanceResponse;
import com.masterpiece.IPiece.blockchain.application.BlockchainService;
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

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("TODO: 컨트롤러 테스트 환경의 근본적인 문제 해결 후 활성화 (#??)")
@WebMvcTest(BlockchainController.class)
@Import(WebConfig.class)
class BlockchainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BlockchainService blockchainService;

    @MockBean
    private BesuClient besuClient;

    // For SecurityConfig to load
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    @Test
    @DisplayName("인증된 사용자는 자신의 KRWT 잔고를 조회할 수 있다")
    @WithMockUser(username = "1", roles = "USER")
    void getKrwtBalance_Success() throws Exception {
        // Given
        Long userId = 1L;
        BigDecimal balance = new BigDecimal("12345.67");
        KrwtBalanceResponse mockResponse = KrwtBalanceResponse.builder().balance(balance).build();
        when(blockchainService.getKrwtBalance(userId)).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/v1/blockchain/wallet/krwt")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(balance.doubleValue()));

        // verify
        verify(blockchainService).getKrwtBalance(userId);
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 KRWT 잔고를 조회할 수 없다")
    void getKrwtBalance_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/blockchain/wallet/krwt")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}

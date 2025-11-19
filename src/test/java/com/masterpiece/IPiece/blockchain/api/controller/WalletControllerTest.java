package com.masterpiece.IPiece.blockchain.api.controller;

import com.masterpiece.IPiece.blockchain.api.dto.response.MyWalletResponse;
import com.masterpiece.IPiece.blockchain.application.WalletService;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.user.application.CustomUserDetailsService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@WebMvcTest(WalletController.class)
@Import(WebConfig.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "1", roles = "USER")
    void 자신의_지갑_정보를_성공적으로_조회한다() throws Exception {
        // given
        MyWalletResponse response = MyWalletResponse.builder()
            .walletAddress("0x1234567890abcdef")
            .balanceKrw(1000000L)
            .tokens(Collections.emptyList())
            .totalValueKrw(1000000L)
            .createdAt(OffsetDateTime.now())
            .build();

        when(walletService.getMyWallet(anyLong())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/v1/blockchain/wallet/my"))
            .andDo(print())  // ← 응답 출력
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.walletAddress").value("0x1234567890abcdef"))
            .andExpect(jsonPath("$.balanceKrw").value(1000000))
            .andExpect(jsonPath("$.totalValueKrw").value(1000000));
    }

    @Test
    @WithMockUser(username = "999", roles = "USER")
    void 지갑_정보가_없는_경우_404_Not_Found_응답을_받는다() throws Exception {
        // given
        when(walletService.getMyWallet(anyLong()))
            .thenThrow(new BusinessException(ErrorCode.NOT_FOUND));

        // when & then
        mockMvc.perform(get("/v1/blockchain/wallet/my"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void 인증되지_않은_사용자는_지갑_정보를_조회할_수_없다() throws Exception {
        mockMvc.perform(get("/v1/blockchain/wallet/my"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
}
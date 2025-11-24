package com.masterpiece.IPiece.blockchain.api.controller;

import com.masterpiece.IPiece.blockchain.api.dto.response.ContractInfoResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.TokenListResponse;
import com.masterpiece.IPiece.blockchain.application.BlockchainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/blockchain")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true", matchIfMissing = true)
public class AdminBlockchainController {

    private final BlockchainService blockchainService;

    @GetMapping("/tokens")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "블록체인 토큰 목록 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<TokenListResponse> getTokenList(
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        TokenListResponse response = blockchainService.getTokenList(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contracts")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "컨트랙트 정보 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<ContractInfoResponse> getContractInfo() {
        ContractInfoResponse response = blockchainService.getContractInfo();
        return ResponseEntity.ok(response);
    }
}

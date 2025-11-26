package com.masterpiece.IPiece.investment.api.controller;

import com.masterpiece.IPiece.common.web.annotation.CurrentUser;
import com.masterpiece.IPiece.investment.api.dto.request.InvestmentRequest;
import com.masterpiece.IPiece.investment.api.dto.response.InvestmentResponse;
import com.masterpiece.IPiece.investment.api.dto.response.InvestmentStatusResponse;
import com.masterpiece.IPiece.investment.application.InvestmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/blockchain/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "블록체인 투자 실행", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<InvestmentResponse> executeInvestment(
            @CurrentUser Long userId,
            @Valid @RequestBody InvestmentRequest request) {
        InvestmentResponse response = investmentService.executeInvestment(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "투자 상태 확인", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<InvestmentStatusResponse> getInvestmentStatus(
            @CurrentUser Long userId,
            @PathVariable("id") Long investmentId) {
        InvestmentStatusResponse response = investmentService.getInvestmentStatus(userId, investmentId);
        return ResponseEntity.ok(response);
    }
}

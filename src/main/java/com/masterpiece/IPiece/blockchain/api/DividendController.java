package com.masterpiece.IPiece.blockchain.api;

import com.masterpiece.IPiece.blockchain.api.dto.request.DividendExecuteRequest;
import com.masterpiece.IPiece.blockchain.api.dto.request.DividendSimulateRequest;
import com.masterpiece.IPiece.blockchain.api.dto.response.DividendExecuteResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.DividendSimulateResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.MyDividendsResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.ProjectDividendsResponse;
import com.masterpiece.IPiece.blockchain.application.DividendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/blockchain/dividends")
@RequiredArgsConstructor
public class DividendController {

    private final DividendService dividendService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DividendExecuteResponse> executeDividend(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody DividendExecuteRequest request
    ) {
        DividendExecuteResponse response = dividendService.executeDividend(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/simulate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DividendSimulateResponse> simulateDividend(
        @Valid @RequestBody DividendSimulateRequest request
    ) {
        DividendSimulateResponse response = dividendService.simulateDividend(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MyDividendsResponse> getMyDividends(
        @AuthenticationPrincipal Long userId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        MyDividendsResponse response = dividendService.getMyDividends(userId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProjectDividendsResponse> getProjectDividends(
        @PathVariable Long projectId
    ) {
        ProjectDividendsResponse response = dividendService.getProjectDividends(projectId);
        return ResponseEntity.ok(response);
    }
}

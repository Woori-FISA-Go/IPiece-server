package com.masterpiece.IPiece.market.api;

import com.masterpiece.IPiece.market.api.dto.request.OrderRequest;
import com.masterpiece.IPiece.market.api.dto.request.PendingOrderRequest;
import com.masterpiece.IPiece.market.api.dto.response.*;
import com.masterpiece.IPiece.market.application.MarketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    @GetMapping("/products")
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @AuthenticationPrincipal Long userId
    ) {
        Pageable  pageable = PageRequest.of(page-1, 10, Sort.by("issueDate").descending());
        return ResponseEntity.ok(marketService.getProducts(pageable, userId));
    }

    @GetMapping("/{product_id}/details")
    public ResponseEntity<ProductDetailsResponse> getDetails(
            @PathVariable("product_id") Long productId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(marketService.getDetails(productId, userId));
    }

    @GetMapping("/{product_id}/chart")
    public ResponseEntity<ChartResponse> getChart(
            @PathVariable("product_id") Long productId,
            @RequestParam(value = "interval", defaultValue = "1d") String interval,
            @RequestParam(value = "cursor", required = false) String cursor
    ) {
        ChartResponse response = marketService.getChart(productId, interval, cursor);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{product_id}/buy")
    public ResponseEntity<OrderResponse> buy(
            @PathVariable("product_id") Long productId,
            @AuthenticationPrincipal Long userId,
            @RequestHeader(name = "Idempotency-Key", required = true) String idempotencyKey,
            @Valid @RequestBody OrderRequest request
    ) {
        if (userId == null)
            throw new IllegalStateException("User not authenticated");

        OrderResponse response =
                marketService.buy(productId, userId, idempotencyKey, request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{product_id}/sell")
    public ResponseEntity<OrderResponse> sell(
            @PathVariable("product_id") Long productId,
            @AuthenticationPrincipal Long userId,
            @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
            @Valid @RequestBody OrderRequest request
    ) {
        if (userId == null)
            throw new IllegalStateException("User not authenticated");

        OrderResponse response = marketService.sell(productId, userId, idempotencyKey, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{product_id}/orders/pending")
    public ResponseEntity<PendingOrderListResponse> pendingOrders(
            @PathVariable("product_id") Long productId,
            @AuthenticationPrincipal Long userId,
            @Valid @ModelAttribute PendingOrderRequest request
    ) {

        if (userId == null)
            throw new IllegalStateException("User not authenticated");

        var response = marketService.getPendingOrders(
                userId,
                productId,
                request.getPage()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{product_id}/asset")
    public ResponseEntity<HoldingAssetResponse> getHoldingAsset(
            @PathVariable("product_id") Long productId,
            @AuthenticationPrincipal Long userId
    ) {
        var response = marketService.getHoldingAsset(userId, productId);
        return ResponseEntity.ok(response);
    }
}

package com.masterpiece.IPiece.market.api;

import com.masterpiece.IPiece.market.api.dto.request.BuyOrderRequest;
import com.masterpiece.IPiece.market.api.dto.response.BuyOrderResponse;
import com.masterpiece.IPiece.market.api.dto.response.ProductDetailsResponse;
import com.masterpiece.IPiece.market.api.dto.response.ProductListResponse;
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
            @RequestParam(defaultValue = "1") @Min(1) int page
            // TODO: 추후 인증(JWT) 구현 후 활성화 예정
            // @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        Pageable  pageable = PageRequest.of(page-1, 10, Sort.by("issueDate").descending());
        // MEMO: 현재 인증 미구현 상태이므로 userId는 임시 null로 테스트
        Long userId = null;

        return ResponseEntity.ok(marketService.getProducts(pageable, userId));
    }

    @GetMapping("/{product_id}/details")
    public ResponseEntity<ProductDetailsResponse> getDetails(
            @PathVariable("product_id") Long productId
            // TODO: 추후 인증(JWT) 구현 후 활성화 예정
            // @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        Long userId = null; // MEMO: 로그인 미구현이므로 null
        return ResponseEntity.ok(marketService.getDetails(productId, userId));
    }

    @PostMapping("/{product_id}/buy")
    public ResponseEntity<BuyOrderResponse> buy(
            @PathVariable("product_id") Long productId,
            @Valid @RequestBody BuyOrderRequest request,
            @AuthenticationPrincipal Long userId,
//            @RequestHeader(value = "X-USER-ID", required = false) Long userId,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        System.out.println("***********************"+userId);
        if (userId == null)
            userId = 1L;
//            throw new IllegalStateException("User not authenticated");

        BuyOrderResponse response =
                marketService.buy(productId, userId, request, idempotencyKey);

        return ResponseEntity.ok(response);
    }
}

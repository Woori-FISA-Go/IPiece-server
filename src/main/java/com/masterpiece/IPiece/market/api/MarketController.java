package com.masterpiece.IPiece.market.api;

import com.masterpiece.IPiece.market.api.dto.response.ProductListResponse;
import com.masterpiece.IPiece.market.application.MarketService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}

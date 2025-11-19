package com.masterpiece.IPiece.offering.api;

import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.common.util.JwtTokenProvider;
import com.masterpiece.IPiece.common.web.Responses;
import com.masterpiece.IPiece.offering.application.OfferingPurchaseService;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/offerings")
@RequiredArgsConstructor
public class OfferingPurchaseController {

    private final OfferingPurchaseService purchaseService;
    private final JwtTokenProvider jwtTokenProvider;


    // 구매 사전 검증
    @GetMapping("/{product_id}/purchase/validate")
    @Operation(security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<?> validatePurchase(
            @PathVariable("product_id") Long productId,
            @RequestParam("quantity") Long quantity,
            HttpServletRequest request
    ){
        Long userId = extractUserIdFromToken(request);

        purchaseService.validatePurchase(productId, userId, quantity);

        return Responses.ok(Map.of(
           "valid", true,
           "productId", productId,
           "quantity", quantity
        ));
    }

    // 구매 API
    @PostMapping("/{product_id}/purchase")
    @Operation(security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<?> purchase(
            @PathVariable("product_id") Long productId,
            @RequestParam("quantity") Long quantity,
            HttpServletRequest request
    ){
        Long userId = extractUserIdFromToken(request);

        if(quantity <= 0){
            throw new BusinessException(ErrorCode.INVALID_QUANTITY);
        }

        purchaseService.purchase(productId, userId, quantity);

        return Responses.ok(Map.of(
                "success", true,
                "productId",productId,
                "quantity", quantity
        ));

    }


    private Long extractUserIdFromToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if(header == null || !header.startsWith("Bearer ")){
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        String token = header.substring(7);
        ErrorCode error = jwtTokenProvider.validateToken(token);


        if(error != null){
            throw new BusinessException(error);
        }

        return Long.valueOf(jwtTokenProvider.getSubject(token));
    }

}

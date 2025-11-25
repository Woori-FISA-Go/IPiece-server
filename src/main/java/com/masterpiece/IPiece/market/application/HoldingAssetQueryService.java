package com.masterpiece.IPiece.market.application;

import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountRepository;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.market.api.dto.response.HoldingAssetResponse;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import com.masterpiece.IPiece.mypage.infra.HoldingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HoldingAssetQueryService {

    private final VirtualAccountRepository virtualAccountRepository;
    private final ProductRepository productRepository;
    private final HoldingsRepository holdingsRepository;

    public HoldingAssetResponse getHoldingAsset(Long userId, Long productId) {

        VirtualAccount account = virtualAccountRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Virtual account not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Holdings holding = holdingsRepository.findByVirtualAccountAndProduct(account, product)
                .orElseThrow(() -> new IllegalArgumentException("No holdings for this product"));

        long quantity = holding.getQuantity();
        long avgBuyPrice = holding.getAvgBuyPrice();
        long currentPrice = product.getCurrentPrice();

        long totalAmount = quantity * currentPrice;
        long profitAmount = (currentPrice - avgBuyPrice) * quantity;

        double profitRate = (avgBuyPrice > 0)
                ? Math.abs((profitAmount * 100.0) / (avgBuyPrice * quantity))
                : 0.0;

        profitRate = Math.round(profitRate * 10) / 10.0;

        return HoldingAssetResponse.builder()
                .product_name(product.getProductName())
                .quantity(quantity)
                .avg_buy_price(avgBuyPrice)
                .total_amount(totalAmount)
                .total_profit_amount(profitAmount)
                .total_profit_rate(profitRate)
                .build();
    }
}

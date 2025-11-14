package com.masterpiece.IPiece.market.application.mapper;

import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.market.api.dto.response.ProductListResponse;
import com.masterpiece.IPiece.common.domain.product.policy.PriceChangePolicy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ProductMapper {

    public ProductListResponse.ProductListItem toProductListItem(
            Product p,
            Map<Long, Long> prevCloseMap,
            Set<Long> favorites
    ) {
        long current = p.getCurrentPrice();
        long prevClose = prevCloseMap.getOrDefault(p.getProductId(), 0L);
        double changeRate = PriceChangePolicy.changeRate(current, prevClose);

        return ProductListResponse.ProductListItem.builder()
                .productId(p.getProductId())
                .productName(p.getProductName())
                .thumbnailImg(p.getThumbnailImg())
                .currentPrice(current)
                .changeRate(changeRate)
                .owner(p.getOwner())
                .startAt(p.getIssueDate().toString())
                .isFavorited(favorites.contains(p.getProductId()))
                .build();
    }
}
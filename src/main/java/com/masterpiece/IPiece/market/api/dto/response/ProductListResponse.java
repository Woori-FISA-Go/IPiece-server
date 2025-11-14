package com.masterpiece.IPiece.market.api.dto.response;

import com.masterpiece.IPiece.common.domain.product.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class ProductListResponse {
    private List<ProductListItem> products;
    private int totalCount;
    private int page;

    public static ProductListResponse of(
            List<Product> productList,
            List<Long> favoritedIds,
            int totalCount,
            int page
    ) {
        Set<Long> favoritedSet = favoritedIds != null ? new HashSet<>(favoritedIds) : Collections.emptySet();
        List<ProductListItem> items = productList.stream()
                .map(p -> ProductListItem.of(p, favoritedSet.contains(p.getProductId())))
                .collect(Collectors.toList());
        return ProductListResponse.builder()
                .products(items)
                .totalCount(totalCount)
                .page(page)
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProductListItem {
        private Long productId;
        private String thumbnailImg;
        private String productName;
        private String owner;
        private Long currentPrice;
        private Double changeRate;
        private Boolean isFavorited;
        private String startAt;

        public static ProductListItem of(Product p, boolean favorited) {
            return ProductListItem.builder()
                    .productId(p.getProductId())
                    .productName(p.getProductName())
                    .thumbnailImg(p.getThumbnailImg())
                    .currentPrice(p.getCurrentPrice())
                    .changeRate(0.0)
                    .owner(p.getOwner())
                    .startAt(p.getIssueDate().toString())
                    .isFavorited(favorited)
                    .build();
        }
    }
}

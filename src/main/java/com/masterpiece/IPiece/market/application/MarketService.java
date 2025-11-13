package com.masterpiece.IPiece.market.application;

import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.domain.product.policy.PriceChangePolicy;
import com.masterpiece.IPiece.market.api.dto.response.ProductListResponse;
import com.masterpiece.IPiece.market.application.mapper.ProductMapper;
import com.masterpiece.IPiece.market.application.port.FavoriteQueryPort;
import com.masterpiece.IPiece.market.application.port.PrevCloseQueryPort;
import com.masterpiece.IPiece.market.application.port.ProductQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarketService {

    private final ProductQueryPort productQueryPort;
    private final FavoriteQueryPort favoriteQueryPort;
    private final PrevCloseQueryPort prevCloseQueryPort;
    private final ProductMapper productMapper;

    public ProductListResponse getProducts(Pageable pageable, Long userId) {
        Page<com.masterpiece.IPiece.common.domain.product.Product> page =
                productQueryPort.findActiveProducts(pageable);

        Set<Long> favorites = (userId != null)
                ? favoriteQueryPort.findProductIdsByUserId(userId)
                : Set.of();

        List<Long> productIds = page.getContent().stream()
                .map(p -> p.getProductId())
                .collect(Collectors.toList());

        Map<Long, Long> prevCloseMap =
                prevCloseQueryPort.findYesterdaysPrevClose(productIds, ZoneId.of("Asia/Seoul"));

        var items = page.getContent().stream()
                .map(p -> productMapper.toProductListItem(p, prevCloseMap, favorites))
                .toList();

        return new ProductListResponse(items, (int) page.getTotalElements(), page.getNumber()+1);
    }
}

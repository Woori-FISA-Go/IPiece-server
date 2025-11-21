package com.masterpiece.IPiece.market.application;

import com.masterpiece.IPiece.market.api.dto.response.PendingOrderItem;
import com.masterpiece.IPiece.market.api.dto.response.PendingOrderListResponse;
import com.masterpiece.IPiece.market.infra.jpa.OrderBookRepository;
import com.masterpiece.IPiece.market.infra.messaging.RealtimePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PendingOrderPushService {

    private final OrderBookRepository orderBookRepository;
    private final RealtimePublisher realtimePublisher;

    public void pushPendingOrders(Long userId, Long productId) {
        var pageable = PageRequest.of(0, 10);
        var result = orderBookRepository.findPendingOrders(userId, productId, pageable);

        var items = result.getContent().stream().map(ob -> {
            long remain = ob.getRemainQuantity() == null ? 0L : ob.getRemainQuantity();
            long filled = ob.getOrderQuantity() - remain;
            return PendingOrderItem.builder()
                    .order_id(String.valueOf(ob.getOrderId()))
                    .product_id(productId)
                    .product_name(ob.getProduct().getProductName())
                    .order_type(ob.getOrderType().name())
                    .price(ob.getOrderPrice())
                    .quantity(ob.getOrderQuantity())
                    .filled_quantity(filled)
                    .remaining_quantity(remain)
                    .amount(ob.getOrderPrice() * ob.getOrderQuantity())
                    .placed_at(ob.getClientTime().toString())
                    .build();
        }).collect(Collectors.toList());

        PendingOrderListResponse response = PendingOrderListResponse.builder()
                .items(items)
                .page(1)
                .total(result.getTotalElements())
                .has_next(result.hasNext())
                .build();

        realtimePublisher.publishPendingOrders(userId, productId, response);
    }
}

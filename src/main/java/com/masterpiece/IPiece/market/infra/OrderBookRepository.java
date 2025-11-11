package com.masterpiece.IPiece.market.infra;

import com.masterpiece.IPiece.market.domain.OrderBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderBookRepository extends JpaRepository<OrderBook, Long> {
}
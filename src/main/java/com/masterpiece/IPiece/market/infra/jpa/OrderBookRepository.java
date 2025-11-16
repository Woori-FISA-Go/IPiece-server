package com.masterpiece.IPiece.market.infra.jpa;

import com.masterpiece.IPiece.market.domain.OrderBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderBookRepository extends JpaRepository<OrderBook, Long> {
}
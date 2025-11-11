package com.masterpiece.IPiece.market.infra;


import com.masterpiece.IPiece.market.domain.TradeExecution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeExecutionRepository extends JpaRepository<TradeExecution, Long> {
}

package com.masterpiece.IPiece.dividends.infra;

import com.masterpiece.IPiece.dividends.domain.Dividends;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DividendsRepository extends JpaRepository<Dividends, Long> {
    Dividends findByProduct_ProductId(Long productId);
}
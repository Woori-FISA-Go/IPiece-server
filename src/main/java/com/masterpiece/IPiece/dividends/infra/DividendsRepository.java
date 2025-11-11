package com.masterpiece.IPiece.dividends.infra;

import com.masterpiece.IPiece.dividends.domain.Dividends;
import com.masterpiece.IPiece.offering.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DividendsRepository extends JpaRepository<Dividends, Long> {
    /*
    product 연결 후 주석 해제
    List<Dividends> findByProductId(Long productId);
     */
}
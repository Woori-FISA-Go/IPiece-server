package com.masterpiece.IPiece.dividends.infra;

import com.masterpiece.IPiece.dividends.domain.DividendStatus;
import com.masterpiece.IPiece.dividends.domain.Dividends;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.OffsetDateTime;

public interface DividendsRepository extends JpaRepository<Dividends, Long> {
    Dividends findByProduct_ProductId(Long productId);
    List<Dividends> findAllByProduct_ProductId(Long productId); // 251120 타입불일치로 수정(이기현)
    List<Dividends> findByStatus(DividendStatus status);
    List<Dividends> findByProduct_ProductIdAndStatus(Long productId, DividendStatus status);
    List<Dividends> findByStatusAndPayoutDateBefore(DividendStatus status, OffsetDateTime now);

}
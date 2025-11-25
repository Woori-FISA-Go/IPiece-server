package com.masterpiece.IPiece.investment.infra.jpa;

import com.masterpiece.IPiece.investment.domain.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Long> {
}

package com.masterpiece.IPiece.domain.infra;

import com.masterpiece.IPiece.domain.product.Disclosure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisclosureRepository extends JpaRepository<Disclosure, Long> {
}

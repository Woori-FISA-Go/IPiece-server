package com.masterpiece.IPiece.dividends.infra;


import com.masterpiece.IPiece.dividends.domain.DividendPayouts;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DividendPayoutsRepository extends JpaRepository<DividendPayouts, Long> {
    List<DividendPayouts> findByVirtualAccount(VirtualAccount account);
}

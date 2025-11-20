package com.masterpiece.IPiece.dividends.infra;

import com.masterpiece.IPiece.dividends.domain.Dividends;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.dividends.domain.DividendPayouts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface DividendPayoutsRepository extends JpaRepository<DividendPayouts, Long> {
    List<DividendPayouts> findByDividends_Product_ProductIdAndPayoutStatusOrderByPayoutDateDesc(
            Long productId,
            String payoutStatus
    );

    // 특정 계좌에 대해, 기간 내 완료된 배당금 지급 내역 조회 -> mypage에서 거래내역 조회용
    List<DividendPayouts> findByVirtualAccountAndPayoutDateBetweenAndPayoutStatus(
            VirtualAccount virtualAccount,
            OffsetDateTime from,
            OffsetDateTime to,
            String payoutStatus
    );

    List<DividendPayouts> findByDividends(Dividends dividends);

    List<DividendPayouts> findByDividendsAndPayoutStatus(Dividends dividends, String payoutStatus);
}

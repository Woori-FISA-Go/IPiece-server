package com.masterpiece.IPiece.mypage.infra;

import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoldingsRepository extends JpaRepository<Holdings, Long> {

    //특정 가상계좌 보유 자산 조회
    List<Holdings> findAllByVirtualAccount(VirtualAccount virtualAccount);

}

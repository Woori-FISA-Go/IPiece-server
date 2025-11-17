package com.masterpiece.IPiece.common.domain.infra;

import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.account.VirtualAccountJournal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VirtualAccountJournalRepository
        extends JpaRepository<VirtualAccountJournal, Long> {

    // 해당 계좌의 모든 저널을 created_at 기준 최신순으로 반환
    List<VirtualAccountJournal> findByVirtualAccountOrderByCreatedAtDesc(
            VirtualAccount account
    );
}
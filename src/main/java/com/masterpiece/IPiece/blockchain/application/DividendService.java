package com.masterpiece.IPiece.blockchain.application;

import com.masterpiece.IPiece.blockchain.api.dto.request.DividendExecuteRequest;
import com.masterpiece.IPiece.blockchain.api.dto.request.DividendSimulateRequest;
import com.masterpiece.IPiece.blockchain.api.dto.response.DividendExecuteResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.DividendSimulateResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.MyDividendsResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.ProjectDividendsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

@Service
@RequiredArgsConstructor
@Transactional
public class DividendService {

    private final Web3j web3j;
    private final Credentials adminCredentials;

    public DividendExecuteResponse executeDividend(DividendExecuteRequest request) {
        // 1. 블록체인 실행
        // 2. DB 저장
        // 3. Response 반환
        throw new UnsupportedOperationException("배당 실행 기능이 아직 구현되지 않았습니다");
    }

    public DividendSimulateResponse simulateDividend(DividendSimulateRequest request) {
        // 1. 시뮬레이션 로직
        throw new UnsupportedOperationException("배당 시뮬레이션 기능이 아직 구현되지 않았습니다");
    }

    @Transactional(readOnly = true)
    public MyDividendsResponse getMyDividends(Long userId, int page, int size) {
        // 1. 내 배당 내역 조회 로직
        throw new UnsupportedOperationException("내 배당 조회 기능이 아직 구현되지 않았습니다");
    }

    @Transactional(readOnly = true)
    public ProjectDividendsResponse getProjectDividends(Long projectId) {
        // 1. 프로젝트별 배당 내역 조회 로직
        throw new UnsupportedOperationException("프로젝트 배당 조회 기능이 아직 구현되지 않았습니다");
    }
}

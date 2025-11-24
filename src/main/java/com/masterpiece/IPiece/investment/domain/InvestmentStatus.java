package com.masterpiece.IPiece.investment.domain;

public enum InvestmentStatus {
    PENDING,    // 대기
    PROCESSING, // 처리중 (화이트리스트 완료)
    COMPLETED,  // 완료 (토큰 전송까지 완료)
    FAILED      // 실패
}

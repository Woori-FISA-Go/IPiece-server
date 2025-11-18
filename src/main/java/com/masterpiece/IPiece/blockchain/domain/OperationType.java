package com.masterpiece.IPiece.blockchain.domain;

public enum OperationType {
    MINT,    // 발행 (입금)
    BURN,    // 소각 (출금)
    TRANSFER, // 전송
    OTHER    // 기타
}
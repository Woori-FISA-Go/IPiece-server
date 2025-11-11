package com.masterpiece.IPiece.offering.domain;

/** 청약(Subscription) 상태 전용 Enum */
public enum OfferingStatus {
    PENDING,          // 대기
    PAID,             // 입금완료
    REFUNDED,         // 환불
}

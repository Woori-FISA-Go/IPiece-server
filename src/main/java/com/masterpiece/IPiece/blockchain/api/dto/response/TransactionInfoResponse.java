package com.masterpiece.IPiece.blockchain.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionInfoResponse {
    private String hash;
    private String status; // success/failed
    private Long blockNumber;
    private String blockHash;
    private String from;
    private String to;
    private String value; // 전송 값 (KRWT는 0일 수 있음)
    private String gasUsed;
    private String gasPrice;
    private OffsetDateTime timestamp;
    private List<Log> logs; // 이벤트 로그

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Log {
        private String event;
        private Map<String, Object> data; // 이벤트 데이터는 가변적이므로 Map으로 처리
    }
}

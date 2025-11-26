package com.masterpiece.IPiece.dividends.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 배당 배정/집행 스케줄러
 * - 매일 새벽 03:00 배정
 * - 매일 새벽 03:10 집행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DividendBatchJob {

    private final DividendService dividendService;

    /** 매일 08:00 배정 */
    @Scheduled(cron = "0 20 17 * * *")
    public void allocate() {
        log.info("[DividendBatchJob] allocate start");
        int count = dividendService.allocateDueDividends();
        log.info("[DividendBatchJob] allocate end - processed={} dividends", count);
    }

    /** 매일 08:10 집행 */
    @Scheduled(cron = "0 23 17 * * *")
    public void execute() {
        log.info("[DividendBatchJob] execute start");
        int count = dividendService.executeDueDividends();
        log.info("[DividendBatchJob] execute end - completed={} dividends", count);
    }
}
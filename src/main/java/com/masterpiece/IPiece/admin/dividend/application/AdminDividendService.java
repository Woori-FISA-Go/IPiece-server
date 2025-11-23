package com.masterpiece.IPiece.admin.dividend.application;

import com.masterpiece.IPiece.admin.dividend.api.dto.request.AdminUpsertDividendRequest;
import com.masterpiece.IPiece.admin.dividend.api.dto.response.AdminDividendListResponse;
import com.masterpiece.IPiece.admin.dividend.api.dto.response.AdminDividendPayoutsResponse;
import com.masterpiece.IPiece.admin.dividend.api.dto.response.AdminDividendPayoutsResponse.PayoutItem;
import com.masterpiece.IPiece.admin.dividend.api.dto.response.AdminDividendResponse;
import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.dividends.domain.DividendPayouts;
import com.masterpiece.IPiece.dividends.domain.DividendStatus;
import com.masterpiece.IPiece.dividends.domain.Dividends;
import com.masterpiece.IPiece.dividends.infra.DividendPayoutsRepository;
import com.masterpiece.IPiece.dividends.infra.DividendsRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDividendService {

    private final DividendsRepository dividendsRepository;
    private final ProductRepository productRepository;
    private final DividendPayoutsRepository dividendPayoutsRepository;

    // ==========================
    // 1) 배당 선언 생성/수정
    // ==========================
    @Transactional
    public AdminDividendResponse upsertDividend(AdminUpsertDividendRequest request) {
        // 1. 날짜 파싱
        OffsetDateTime recordDate;
        OffsetDateTime payoutDate;
        try {
            recordDate = OffsetDateTime.parse(request.getRecordDate());
            payoutDate = OffsetDateTime.parse(request.getPayoutDate());
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "record_date 또는 payout_date 형식이 올바르지 않습니다."
            );
        }

        if (!payoutDate.isAfter(recordDate)) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "payout_date는 record_date 이후여야 합니다."
            );
        }

        // 2. 상품 확인
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        Dividends dividends;

        // 3. 생성 vs 수정
        if (request.getDividendId() == null) {
            // === 생성 ===
            dividends = Dividends.builder()
                    .product(product)
                    .recordDate(recordDate)
                    .payoutDate(payoutDate)
                    .status(DividendStatus.SCHEDULED)
                    .totalAmount(request.getTotalAmount())
                    .distributedAmount(0L)
                    .remainderAmount(0L)
                    .recipientCount(0)
                    .build();

            dividendsRepository.save(dividends);
        } else {
            // === 수정 ===
            dividends = dividendsRepository.findById(request.getDividendId())
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.NOT_FOUND,
                            "배당 정보를 찾을 수 없습니다.")
                    );

            if (dividends.getStatus() != DividendStatus.SCHEDULED) {
                throw new BusinessException(
                        ErrorCode.UNPROCESSABLE_ENTITY,
                        "SCHEDULED 상태의 배당만 수정할 수 있습니다."
                );
            }

            // 필드 업데이트 (id 유지)
            dividends = Dividends.builder()
                    .dividendId(dividends.getDividendId())
                    .product(product)
                    .recordDate(recordDate)
                    .payoutDate(payoutDate)
                    .status(DividendStatus.SCHEDULED)
                    .totalAmount(request.getTotalAmount())
                    .distributedAmount(dividends.getDistributedAmount())
                    .remainderAmount(dividends.getRemainderAmount())
                    .recipientCount(dividends.getRecipientCount())
                    .transactionHash(dividends.getTransactionHash())
                    .blockNumber(dividends.getBlockNumber())
                    .dividendPayouts(dividends.getDividendPayouts())
                    .build();

            dividendsRepository.save(dividends);
        }

        return AdminDividendResponse.builder()
                .dividendId(dividends.getDividendId())
                .productId(dividends.getProduct().getProductId())
                .status(dividends.getStatus().name())
                .recordDate(dividends.getRecordDate())
                .payoutDate(dividends.getPayoutDate())
                .totalAmount(dividends.getTotalAmount())
                .build();
    }

    // ==========================
    // 2) 배당 선언 목록 조회 (페이징 없음)
    // ==========================
    @Transactional(readOnly = true)
    public AdminDividendListResponse listDividends(Long productId, String status) {
        List<Dividends> list;

        DividendStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = DividendStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new BusinessException(
                        ErrorCode.VALIDATION_ERROR,
                        "status 값이 올바르지 않습니다."
                );
            }
        }

        if (productId == null && statusEnum == null) {
            list = dividendsRepository.findAll();
        } else if (productId != null && statusEnum == null) {
            list = dividendsRepository.findAllByProduct_ProductId(productId);
        } else if (productId == null) {
            list = dividendsRepository.findByStatus(statusEnum);
        } else {
            list = dividendsRepository.findByProduct_ProductIdAndStatus(productId, statusEnum);
        }

        List<AdminDividendResponse> items = list.stream()
                .map(d -> AdminDividendResponse.builder()
                        .dividendId(d.getDividendId())
                        .productId(d.getProduct().getProductId())
                        .status(d.getStatus().name())
                        .recordDate(d.getRecordDate())
                        .payoutDate(d.getPayoutDate())
                        .totalAmount(d.getTotalAmount())
                        .build())
                .toList();

        return AdminDividendListResponse.builder()
                .items(items)
                .totalCount((long) items.size())
                .build();
    }

    // ==========================
    // 3) 배당 집행 결과 조회
    // ==========================
    @Transactional(readOnly = true)
    public AdminDividendPayoutsResponse getDividendPayouts(Long dividendId, String status) {
        // 1. 배당 선언 조회
        Dividends dividends = dividendsRepository.findById(dividendId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "배당 정보를 찾을 수 없습니다.")
                );

        // 2. payout 목록 조회
        List<DividendPayouts> payouts;
        if (status == null || status.isBlank()) {
            payouts = dividendPayoutsRepository.findByDividends(dividends);
        } else {
            payouts = dividendPayoutsRepository.findByDividendsAndPayoutStatus(dividends, status);
        }

        // 3. 요약 값 계산
        long recipientCount = payouts.size();

        long totalPaid = payouts.stream()
                .filter(p -> "PAID".equalsIgnoreCase(p.getPayoutStatus()))
                .mapToLong(DividendPayouts::getPayoutAmount)
                .sum();

        long failedCount = payouts.stream()
                .filter(p -> "FAILED".equalsIgnoreCase(p.getPayoutStatus()))
                .count();

        // 4. items 변환
        List<PayoutItem> items = payouts.stream()
                .map(p -> PayoutItem.builder()
                        .payoutId(p.getPayoutId())
                        .accountId(p.getVirtualAccount().getAccountId())
                        .payoutAmount(p.getPayoutAmount())
                        .payoutStatus(p.getPayoutStatus())
                        .payoutDate(p.getPayoutDate())
                        .build())
                .toList();

        // 5. 응답 DTO
        return AdminDividendPayoutsResponse.builder()
                .dividendId(dividends.getDividendId())
                .recipientCount(recipientCount)
                .totalPaid(totalPaid)
                .failedCount(failedCount)
                .items(items)
                .build();
    }
}
package com.masterpiece.IPiece.admin.offeringandtrade.application;

import com.masterpiece.IPiece.admin.offeringandtrade.api.dto.request.AdminCreateProductRequest;
import com.masterpiece.IPiece.admin.offeringandtrade.api.dto.request.AdminEnableSecondaryTradingRequest;
import com.masterpiece.IPiece.admin.offeringandtrade.api.dto.response.AdminEnableSecondaryTradingResponse;
import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.domain.product.ProductStatus;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.offering.domain.ProductOfferingInfo;
import com.masterpiece.IPiece.offering.infra.ProductOfferingInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductOfferingInfoRepository productOfferingInfoRepository;

    @Transactional
    public void createProductWithOffering(AdminCreateProductRequest request) {
        // 1. 상품명 중복 체크 (대소문자 무시)
        if (productRepository.existsByProductNameIgnoreCase(request.getProductName())) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "Product name already exists: " + request.getProductName()
            );
        }

        // 2. 날짜 파싱 (OffsetDateTime)
        OffsetDateTime issueDate = parseOffsetDateTime(request.getIssueDate());
        OffsetDateTime offeringStart = parseOffsetDateTime(request.getOffering().getOfferingStartDate());
        OffsetDateTime offeringEnd = parseOffsetDateTime(request.getOffering().getOfferingEndDate());

        // 3. Product 엔티티 생성
        Product product = Product.builder()
                .productName(request.getProductName())
                .projectName(request.getProjectName())
                .owner(request.getOwner())
                .issueAmount(request.getIssueAmount())
                .issueDate(issueDate)
                .tokenName(request.getTokenName())
                .tokenSymbol(request.getTokenSymbol())
                .totalTokenQuantity(request.getTokenQuantity())
                .dividendRatio(request.getDividendRatio())
                .exchangeListing("IPiece")      // 고정
                .tokenStandard("ERC-1400")      // 고정
                .presentImg(request.getPresentImg())
                .thumbnailImg(request.getThumbnailImg())
                .status(ProductStatus.OFFERING) // 신규는 공모 상태
                .currentPrice(request.getOffering().getOfferingPrice())
                .lastPrice(request.getOffering().getOfferingPrice())
                .build();

        productRepository.save(product);

        // 4. 공모 정보 엔티티 생성 (product_id 공유)
        ProductOfferingInfo offeringInfo = ProductOfferingInfo.builder()
                .product(product)
                .detailImg(request.getOffering().getDetailImg())
                .offeringAmount(request.getOffering().getOfferingAmount())
                .offeringPrice(request.getOffering().getOfferingPrice())
                .offeringStartDate(offeringStart)
                .offeringEndDate(offeringEnd)
                .progressRate(0) // 최초 0%
                .build();

        productOfferingInfoRepository.save(offeringInfo);
    }

    private OffsetDateTime parseOffsetDateTime(String value) {
        // 예: 2025-12-01T00:00:00+09:00 형식 가정
        return OffsetDateTime.parse(value);
    }

    @Transactional
    public AdminEnableSecondaryTradingResponse enableSecondaryTrading(
            Long productId,
            AdminEnableSecondaryTradingRequest request
    ) {
        // 1. confirm 검증
        if (Boolean.FALSE.equals(request.getConfirm())) {
            // confirm이 false면 잘못된 승인 요청
            throw new BusinessException(ErrorCode.INVALID_SECONDARY_TRADING_CONFIRM);
        }

        // 2. 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductStatus previousStatus = product.getStatus();

        // 3. 상태 전환 (비즈니스 로직은 Product 안에 캡슐화)
        product.enableSecondaryTrading(); // 여기서 이미 상태 검증 및 예외 처리

        // 4. 승인/적용 시각
        OffsetDateTime enabledAt = OffsetDateTime.now();

        // 5. 응답 DTO 생성
        return AdminEnableSecondaryTradingResponse.builder()
                .success(true)
                .productId(product.getProductId())
                .previousStatus(previousStatus.name())
                .newStatus(product.getStatus().name())
                .enabledAt(enabledAt)
                .build();
    }
}
package com.masterpiece.IPiece.admin.offeringandtrade.application;

import com.masterpiece.IPiece.admin.offeringandtrade.api.dto.request.AdminCreateProductRequest;
import com.masterpiece.IPiece.admin.offeringandtrade.api.dto.request.AdminEnableSecondaryTradingRequest;
import com.masterpiece.IPiece.admin.offeringandtrade.api.dto.response.AdminEnableSecondaryTradingResponse;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountRepository;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.domain.product.ProductStatus;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import com.masterpiece.IPiece.mypage.infra.HoldingsRepository;
import com.masterpiece.IPiece.offering.domain.ProductOfferingInfo;
import com.masterpiece.IPiece.offering.infra.OfferingSubscriptionsRepository;
import com.masterpiece.IPiece.offering.infra.ProductOfferingInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductOfferingInfoRepository productOfferingInfoRepository;
    private final OfferingSubscriptionsRepository subscriptionsRepository;
    private final HoldingsRepository holdingsRepository;
    private final VirtualAccountRepository virtualAccountRepository;

    private void debugProduct(Product product) {
        System.out.println("===== PRODUCT FIELD LENGTH CHECK =====");

        debug("productName", product.getProductName());
        debug("projectName", product.getProjectName());
        debug("owner", product.getOwner());
        debug("presentImg", product.getPresentImg());
        debug("thumbnailImg", product.getThumbnailImg());
        debug("tokenName", product.getTokenName());
        debug("tokenSymbol", product.getTokenSymbol());
        debug("tokenStandard", product.getTokenStandard());
        debug("exchangeListing", product.getExchangeListing());
        debug("tokenContractAddress", product.getTokenContractAddress());
        debug("dividendContractAddress", product.getDividendContractAddress());
        debug("description", product.getDescription());

        System.out.println("======================================");
    }

    private void debug(String field, String value) {
        if (value == null) {
            System.out.println(field + " = null");
        } else {
            System.out.println(field + " = (" + value.length() + ") " + value);
        }
    }


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

        debugProduct(product);

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
        if (Boolean.FALSE.equals(request.getConfirm())) {
            throw new BusinessException(ErrorCode.INVALID_SECONDARY_TRADING_CONFIRM);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductStatus previousStatus = product.getStatus();

        product.enableSecondaryTrading(); // 상태 OFFERING → TRADE

        // 1) 공모 신청 내역 불러오기
        List<Object[]> results =
                subscriptionsRepository.sumQuantityByProduct(productId);

        // 2) product_offering_info 조회 (avg price 계산용)
        ProductOfferingInfo offeringInfo = productOfferingInfoRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OFFERING_INFO_NOT_FOUND));

        Long offeringPrice = offeringInfo.getOfferingPrice();

        // 3) account_id별로 holdings로 이전
        for (Object[] row : results) {
            Long accountId = (Long) row[0];
            Long quantity   = (Long) row[1];

            VirtualAccount account = virtualAccountRepository.findById(accountId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.VIRTUAL_ACCOUNT_NOT_FOUND));

            Holdings holding = holdingsRepository
                    .findByVirtualAccountAndProduct(account, product)
                    .orElse(Holdings.builder()
                            .virtualAccount(account)
                            .product(product)
                            .avgBuyPrice(0L)
                            .quantity(0L)
                            .build());

            long existingQuantity = holding.getQuantity();
            long totalQuantity = existingQuantity + quantity;
            long newAvgPrice = ((holding.getAvgBuyPrice() * existingQuantity) + (offeringPrice * quantity)) / totalQuantity;

            holding.setAvgBuyPrice(newAvgPrice);
            holding.setQuantity(totalQuantity);

            holdingsRepository.save(holding);
        }

        // 4) offering_subscriptions 전체 삭제
        subscriptionsRepository.deleteAllByProductId(productId);


        OffsetDateTime enabledAt = OffsetDateTime.now();

        return AdminEnableSecondaryTradingResponse.builder()
                .success(true)
                .productId(product.getProductId())
                .previousStatus(previousStatus.name())
                .newStatus(product.getStatus().name())
                .enabledAt(enabledAt)
                .build();
    }

}
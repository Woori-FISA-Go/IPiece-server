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
import com.masterpiece.IPiece.user.infra.StorageService;
import java.time.OffsetDateTime;
import java.util.List;
import com.masterpiece.IPiece.investment.application.InvestmentService; // Inject InvestmentService
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Add @Slf4j
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;

@Slf4j // Add this annotation
@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductOfferingInfoRepository productOfferingInfoRepository;
    private final OfferingSubscriptionsRepository subscriptionsRepository;
    private final HoldingsRepository holdingsRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final InvestmentService investmentService;
    private final StorageService storageService;


    @Transactional
    public void createProductWithOffering(
            AdminCreateProductRequest request,
            MultipartFile presentImg,
            MultipartFile thumbnailImg,
            MultipartFile detailImg
    ) {
        // 1. 상품명 중복 체크 (대소문자 무시)
        if (productRepository.existsByProductNameIgnoreCase(request.getProductName())) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "Product name already exists: " + request.getProductName()
            );
        }

        // 2. 날짜 파싱
        OffsetDateTime issueDate = parseOffsetDateTime(request.getIssueDate());
        OffsetDateTime offeringStart = parseOffsetDateTime(request.getOffering().getOfferingStartDate());
        OffsetDateTime offeringEnd = parseOffsetDateTime(request.getOffering().getOfferingEndDate());

        // 3. 이미지 S3 업로드
        String productKeyBase = request.getProductName().replaceAll("\\s+", "_");

        String presentImgUrl = null;
        String thumbnailImgUrl = null;
        String detailImgUrl = null;

        if (presentImg != null && !presentImg.isEmpty()) {
            presentImgUrl = storageService.saveProductImage(
                    presentImg, "present", productKeyBase);
        }
        if (thumbnailImg != null && !thumbnailImg.isEmpty()) {
            thumbnailImgUrl = storageService.saveProductImage(
                    thumbnailImg, "thumbnail", productKeyBase);
        }
        if (detailImg != null && !detailImg.isEmpty()) {
            detailImgUrl = storageService.saveProductImage(
                    detailImg, "detail", productKeyBase);
        }

        // 4. Product 엔티티 생성
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
                .exchangeListing("IPiece")
                .tokenStandard("ERC-1400")
                .presentImg(presentImgUrl)
                .thumbnailImg(thumbnailImgUrl)
                .status(ProductStatus.OFFERING)
                .currentPrice(request.getOffering().getOfferingPrice())
                .lastPrice(request.getOffering().getOfferingPrice())
                .build();


        productRepository.save(product);

        ProductOfferingInfo offeringInfo = ProductOfferingInfo.builder()
                .product(product)
                .detailImg(detailImgUrl)
                .offeringAmount(request.getOffering().getOfferingAmount())
                .offeringPrice(request.getOffering().getOfferingPrice())
                .offeringStartDate(offeringStart)
                .offeringEndDate(offeringEnd)
                .progressRate(0)
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

        // Use lock to prevent race condition
        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // Ensure product is in OFFERING status before enabling secondary trading
        if (product.getStatus() != ProductStatus.OFFERING) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Product is not in OFFERING status.");
        }

        ProductStatus previousStatus = product.getStatus();

        // 1) 공모 신청 내역 불러오기
        List<Object[]> results = subscriptionsRepository.sumQuantityByProduct(productId);

        // 2) product_offering_info 조회
        ProductOfferingInfo offeringInfo = productOfferingInfoRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OFFERING_INFO_NOT_FOUND));

        Long offeringPrice = offeringInfo.getOfferingPrice();

        // 3) Blockchain Distribution for each subscriber
        for (Object[] row : results) {
            Long accountId = (Long) row[0];
            Long quantity = (Long) row[1];

            VirtualAccount account = virtualAccountRepository.findById(accountId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.VIRTUAL_ACCOUNT_NOT_FOUND));

            Long userId = account.getUser().getUserId();
            Long amount = quantity * offeringPrice;

            try {
                com.masterpiece.IPiece.investment.api.dto.request.InvestmentRequest investmentRequest =
                        com.masterpiece.IPiece.investment.api.dto.request.InvestmentRequest.builder()
                                .projectId(productId)
                                .amount(amount)
                                .tokenAmount(quantity)
                                .build();

                investmentService.executeInvestment(userId, investmentRequest);
            } catch (Exception e) {
                log.error("Failed to distribute tokens for userId {} (accountId {}) and productId {}: {}",
                        userId, accountId, productId, e.getMessage(), e);
                // Re-throw to abort the entire transaction and maintain consistency
                throw new BusinessException(ErrorCode.BLOCKCHAIN_TRANSACTION_FAILED,
                    "Token distribution failed for user " + userId, e);
            }
        }

        // 4) Update holdings for each subscriber
        for (Object[] row : results) {
            Long accountId = (Long) row[0];
            Long quantity = (Long) row[1];

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

        // 5) Delete all processed subscriptions
        subscriptionsRepository.deleteAllByProductId(productId);

        // 6) Update product status after all distributions are successful
        product.enableSecondaryTrading();
        productRepository.save(product);

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
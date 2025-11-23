package com.masterpiece.IPiece.offering.application;

import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.account.VirtualAccountJournal;
import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountJournalRepository;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountRepository;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.domain.product.ProductStatus;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.offering.domain.OfferingSubscriptions;
import com.masterpiece.IPiece.offering.domain.ProductOfferingInfo;
import com.masterpiece.IPiece.offering.infra.OfferingSubscriptionsRepository;
import com.masterpiece.IPiece.offering.infra.ProductOfferingInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfferingPurchaseService {

    private final ProductRepository productRepository;
    private final ProductOfferingInfoRepository offeringInfoRepository;
    private final OfferingSubscriptionsRepository subscriptionsRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final VirtualAccountJournalRepository virtualAccountJournalRepository;

    // 구매 전 사전 검증
    public void validatePurchase(Long productId, Long userId, long quantity) {
        if(quantity <= 0){
            throw new BusinessException(ErrorCode.INVALID_QUANTITY);
        }

        // 상품Id로 실제 상품 확인
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 공모 상태가 맞는지 확인
        if(product.getStatus() != ProductStatus.OFFERING){
            throw new BusinessException(ErrorCode.PRODUCT_NOT_OFFERING);
        }

        // 공모 정보 조회
        ProductOfferingInfo offeringInfo = offeringInfoRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OFFERING_INFO_NOT_FOUND));

        // 지금까지 신청된 수량
        Long appliedSum = subscriptionsRepository.sumAppliedQuantityByProductId((productId));

        // 총 발행수량
        long totalAmount = offeringInfo.getOfferingAmount();

        // 남은 수량
        long remaining = totalAmount - appliedSum;

        // 남은 수량이 주문수량보다 적다면 예외처리
        if(remaining < quantity){
            throw new BusinessException(ErrorCode.INSUFFICIENT_OFFERING_STOCK);
        }

        // 사용자 가상계좌 확인
        VirtualAccount account = virtualAccountRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIRTUAL_ACCOUNT_NOT_FOUND));

        // 필요한 돈만큼 잔액 있는지 확인
        long requiredPrice = offeringInfo.getOfferingPrice() * quantity;
        if(account.getBalanceKrw() < requiredPrice){
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }
    }


    // 실제 구매
    @Transactional
    public void purchase(Long productId, Long userId, long quantity) {

        validatePurchase(productId, userId, quantity);

        ProductOfferingInfo offeringInfo = offeringInfoRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OFFERING_INFO_NOT_FOUND));

        VirtualAccount account = virtualAccountRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIRTUAL_ACCOUNT_NOT_FOUND));

        long pricePerToken = offeringInfo.getOfferingPrice();
        long totalPrice = pricePerToken * quantity;

        //계좌 잔액에서 해당 금액 줄이기
        account.decreaseBalanceKrw(totalPrice);



        OfferingSubscriptions subscription = OfferingSubscriptions.builder()
                .virtualAccount(account)
                .productOfferingInfo(offeringInfo)
                .appliedQuantity(quantity)
                .appliedAmountKrw(totalPrice)
                .build();

        subscriptionsRepository.save(subscription);

        VirtualAccountJournal journal = VirtualAccountJournal.builder()
                .virtualAccount(account)
                .txType("OFFERING_BUY") //
                .amountKrw(-totalPrice) // 출금이므로 음수
                .balanceAfter(account.getBalanceKrw()) // 차감된 잔액
                .description("공모 구매: " + offeringInfo.getProduct().getProductName())
                .numberOfToken(quantity)
                .build();

        virtualAccountJournalRepository.save(journal);

        updateProgressRate(productId);
    }


    @Transactional
    public void updateProgressRate(Long productId) {
        ProductOfferingInfo offeringInfo = offeringInfoRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OFFERING_INFO_NOT_FOUND));

        Long appliedSum = subscriptionsRepository.sumAppliedQuantityByProductId(productId);
        long totalAmount = offeringInfo.getOfferingAmount();

        if(totalAmount <= 0){
            throw new BusinessException(ErrorCode.OFFERING_AMOUNT_INVALID);
        }

        int rate = (int) Math.floor((double) appliedSum * 100 / totalAmount);

        if(rate > 100) rate = 100;

        offeringInfo.updateProgressRate(rate);
    }
}

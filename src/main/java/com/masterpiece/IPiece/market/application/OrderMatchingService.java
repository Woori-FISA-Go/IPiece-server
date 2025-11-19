package com.masterpiece.IPiece.market.application;

import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountRepository;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import com.masterpiece.IPiece.market.domain.OrderBook;
import com.masterpiece.IPiece.market.domain.OrderType;
import com.masterpiece.IPiece.market.domain.TradeExecution;
import com.masterpiece.IPiece.mypage.infra.HoldingsRepository;
import com.masterpiece.IPiece.market.infra.jpa.OrderBookRepository;
import com.masterpiece.IPiece.market.infra.jpa.TradeExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import jakarta.persistence.OptimisticLockException;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderMatchingService {

    private final OrderBookRepository orderBookRepository;
    private final TradeExecutionRepository tradeExecutionRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final HoldingsRepository holdingsRepository;
    private final PlatformTransactionManager transactionManager;

    /**
     * /v1/market/{product_id}/buy, /sell 에서
     * order_book 에 주문이 저장된 직후 호출되는 진입점
     */
    public void match(OrderBook incomingOrder) {
        if (incomingOrder.getOrderType() == OrderType.BUY) {
            matchBuyOrder(incomingOrder);
        } else {
            matchSellOrder(incomingOrder);
        }
    }

    /**
     * 낙관적 락 충돌 시 재시도
    */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void matchWithRetry(Long orderId) {
        int attempts = 0;
        RuntimeException lastError = null;
        while (attempts < 3) {
            try {
                executeMatchInNewTransaction(orderId);
                return;
            } catch (OptimisticLockException | OptimisticLockingFailureException e) {
                attempts++;
                lastError = e;
            }
        }
        throw lastError != null ? lastError
                : new OptimisticLockingFailureException("Failed to match order after retries");
    }

    private void executeMatchInNewTransaction(Long orderId) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.executeWithoutResult(status -> {
            OrderBook incomingOrder = orderBookRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
            match(incomingOrder);
        });
    }

    // ==========================
    // 1) 매수 주문 매칭 (가격/시간 우선, 부분/다중 체결)
    // ==========================
    private void matchBuyOrder(OrderBook buyOrder) {

        List<OrderBook> sellOrders = orderBookRepository.findMatchableSellOrders(
                buyOrder.getProduct().getProductId(),
                buyOrder.getOrderPrice()
        );

        long remaining = buyOrder.getRemainQuantity();

        for (OrderBook sellOrder : sellOrders) {
            if (remaining <= 0) break;

            long available = sellOrder.getRemainQuantity();
            if (available <= 0) continue;

            long matchQty = Math.min(remaining, available);

            // 호가 방식: 매도자 가격(더 유리한 쪽)으로 체결
            long tradePrice = sellOrder.getOrderPrice();

            // 체결 저장 + 계좌/보유자산 갱신
            saveTrade(buyOrder, sellOrder, matchQty, tradePrice);

            // 매도 주문 잔량/상태 갱신
            long newSellRemain = available - matchQty;
            sellOrder.setRemainQuantity(newSellRemain);
            if (newSellRemain == 0) {
                sellOrder.setPendingStatus(false);
            }

            remaining -= matchQty;
        }

        buyOrder.setRemainQuantity(remaining);
        if (remaining == 0) {
            buyOrder.setPendingStatus(false);
        }
    }

    // ==========================
    // 2) 매도 주문 매칭 (가격/시간 우선, 부분/다중 체결)
    // ==========================
    private void matchSellOrder(OrderBook sellOrder) {

        List<OrderBook> buyOrders = orderBookRepository.findMatchableBuyOrders(
                sellOrder.getProduct().getProductId(),
                sellOrder.getOrderPrice()
        );

        long remaining = sellOrder.getRemainQuantity();

        for (OrderBook buyOrder : buyOrders) {
            if (remaining <= 0) break;

            long available = buyOrder.getRemainQuantity();
            if (available <= 0) continue;

            long matchQty = Math.min(remaining, available);

            // 호가 방식: 매수자 가격(더 유리한 쪽)으로 체결
            long tradePrice = buyOrder.getOrderPrice();

            saveTrade(buyOrder, sellOrder, matchQty, tradePrice);

            long newBuyRemain = available - matchQty;
            buyOrder.setRemainQuantity(newBuyRemain);
            if (newBuyRemain == 0) {
                buyOrder.setPendingStatus(false);
            }

            remaining -= matchQty;
        }

        sellOrder.setRemainQuantity(remaining);
        if (remaining == 0) {
            sellOrder.setPendingStatus(false);
        }
    }

    // ==========================
    // 3) 체결 기록 저장 (trade_execution)
    // ==========================
    private void saveTrade(OrderBook buyOrder,
                           OrderBook sellOrder,
                           long qty,
                           long price) {

        TradeExecution trade = TradeExecution.builder()
                .product(buyOrder.getProduct())
                .buyOrder(buyOrder)
                .sellOrder(sellOrder)
                .tradeQuantity(qty)
                .tradePrice(price)
                .matchTime(OffsetDateTime.now())
                .settleTime(OffsetDateTime.now())
                .tradeState(true)
                .build();

        tradeExecutionRepository.save(trade);

        // 체결을 기준으로 virtual_account / holdings 업데이트
        updateAccountsAndHoldings(buyOrder, sellOrder, qty, price);
    }

    // ==========================
    // 4) 체결 시점 잔액/보유자산 업데이트
    //  - 주문 시점에 pending 자금/수량은 이미 잠겨 있다고 전제
    // ==========================
    private void updateAccountsAndHoldings(OrderBook buyOrder,
                                           OrderBook sellOrder,
                                           long qty,
                                           long tradePrice) {

        VirtualAccount buyer = buyOrder.getVirtualAccount();
        VirtualAccount seller = sellOrder.getVirtualAccount();
        Product product = buyOrder.getProduct();

        long orderPrice = buyOrder.getOrderPrice();
        long totalTradeAmount = qty * tradePrice;

        // --- (1) 구매자: pending 자금 확정 + 가격 차이 환불 ---
        // 주문 시점: pending_price += orderPrice * totalQty
        // 체결 시점: 해당 체결 수량만큼 주문가 기준으로 pending 해제
        long pendingRelease = orderPrice * qty;
        buyer.decreasePendingPrice(pendingRelease);

        // 주문가 대비 실제 체결가가 더 낮으면 그 차액을 balance로 환불
        long refund = (orderPrice - tradePrice) * qty;
        if (refund > 0) {
            buyer.increaseBalanceKrw(refund);
        }

        // 보유자산(매수) 반영: 수량/평단 갱신
        updateHoldingOnBuy(buyer, product, qty, tradePrice);

        // --- (2) 판매자: 보유자산 감소 + 수익 입금 ---
        // 판매자 보유 수량 줄이기 (pending_quantity 컬럼이 있다면 거기서 빼는 로직으로 확장 가능)
        decreaseHoldingOnSell(seller, product, qty);

        // 판매 대금 입금
        seller.increaseBalanceKrw(totalTradeAmount);

        virtualAccountRepository.save(buyer);
        virtualAccountRepository.save(seller);

        printTradeLog(buyOrder, sellOrder, qty, tradePrice, refund);
    }

    // ==========================
    // 5) 보유자산 업데이트 — BUY (평단 가중 평균)
    // ==========================
    private void updateHoldingOnBuy(VirtualAccount buyer,
                                    Product product,
                                    long qty,
                                    long tradePrice) {

        Holdings holding = holdingsRepository.findByVirtualAccountAndProduct(buyer, product)
                .orElseGet(() -> Holdings.create(buyer, product));

        long oldQty = holding.getQuantity();
        long oldAvg = holding.getAvgBuyPrice();

        long newQty = oldQty + qty;
        long newAvg = (oldQty == 0)
                ? tradePrice
                : ((oldAvg * oldQty) + (tradePrice * qty)) / newQty;

        holding.setQuantity(newQty);
        holding.setAvgBuyPrice(newAvg);

        holdingsRepository.save(holding);
    }

    // ==========================
    // 6) 보유자산 업데이트 — SELL
    // ==========================
    private void decreaseHoldingOnSell(VirtualAccount seller,
                                       Product product,
                                       long qty) {

        Holdings holding = holdingsRepository.findByVirtualAccountAndProduct(seller, product)
                .orElseThrow(() -> new IllegalStateException("보유 수량 부족"));

        long remainPending = holding.getPendingQuantity() - qty;
        if (remainPending < 0) {
            throw new IllegalStateException("대기 수량 부족");
        }

        holding.setPendingQuantity(remainPending);
        holdingsRepository.save(holding);
    }

    private void printTradeLog(OrderBook buyOrder,
                               OrderBook sellOrder,
                               long qty,
                               long tradePrice,
                               long refund) {

        String productName = buyOrder.getProduct().getProductName();
        Long productId = buyOrder.getProduct().getProductId();

        long buyerRemain = buyOrder.getRemainQuantity();
        long sellerRemain = sellOrder.getRemainQuantity();

        String buyerType = (buyerRemain == 0) ? "전체체결" : "부분체결";
        String sellerType = (sellerRemain == 0) ? "전체체결" : "부분체결";

        long totalAmount = qty * tradePrice;

        Long buyerUserId = buyOrder.getVirtualAccount().getUser().getUserId();
        Long sellerUserId = sellOrder.getVirtualAccount().getUser().getUserId();

        System.out.println();
        System.out.println("┌───────────────────────────────────────────────┐");
        System.out.println("│  📈 TRADE EXECUTED                           │");
        System.out.println("└───────────────────────────────────────────────┘");

        System.out.printf("🧩 상품: %s (%d)%n", productName, productId);
        System.out.printf("💰 체결가: %,d원%n", tradePrice);
        System.out.printf("📦 체결 수량: %d개  |  총액: %,d원%n", qty, totalAmount);
        System.out.println();

        // BUYER 블록
        System.out.println("👤 [BUYER]");
        System.out.printf("   • 유저 ID      : %d%n", buyerUserId);
        System.out.printf("   • 주문 유형    : BUY%n");
        System.out.printf("   • 체결 수량    : %d개%n", qty);
        System.out.printf("   • 단가         : %,d원%n", tradePrice);
        System.out.printf("   • 체결 총액    : %,d원%n", totalAmount);
        System.out.printf("   • 체결 유형    : %s%n", buyerType);
        System.out.printf("   • 환불 금액    : %,d원%n", refund);
        System.out.println();

        // SELLER 블록
        System.out.println("👤 [SELLER]");
        System.out.printf("   • 유저 ID      : %d%n", sellerUserId);
        System.out.printf("   • 주문 유형    : SELL%n");
        System.out.printf("   • 체결 수량    : %d개%n", qty);
        System.out.printf("   • 단가         : %,d원%n", tradePrice);
        System.out.printf("   • 체결 총액    : %,d원%n", totalAmount);
        System.out.printf("   • 체결 유형    : %s%n", sellerType);
        System.out.println();

        // 잔량 정보도 같이 찍어주면 디버깅에 도움 됨
        System.out.println("🔎 [REMAIN]");
        System.out.printf("   • BUY  remain : %d개%n", buyerRemain);
        System.out.printf("   • SELL remain : %d개%n", sellerRemain);
        System.out.println("───────────────────────────────────────────────\n");
    }
}

package com.masterpiece.IPiece.market.application;

import com.masterpiece.IPiece.blockchain.application.BlockchainService;
import com.masterpiece.IPiece.blockchain.domain.BlockchainTransaction;
import com.masterpiece.IPiece.blockchain.domain.TransactionType;
import com.masterpiece.IPiece.blockchain.infra.jpa.BlockchainTransactionRepository;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountRepository;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.domain.product.ProductStatus;
import com.masterpiece.IPiece.integration.besu.BesuClient;
import com.masterpiece.IPiece.market.api.dto.request.OrderRequest;
import com.masterpiece.IPiece.market.domain.OrderBook;
import com.masterpiece.IPiece.market.domain.TradeExecution;
import com.masterpiece.IPiece.market.infra.jpa.OrderBookRepository;
import com.masterpiece.IPiece.market.infra.jpa.TradeExecutionRepository;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import com.masterpiece.IPiece.mypage.infra.HoldingsRepository;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.infra.UserRepository;
import com.masterpiece.IPiece.user.infra.LocalStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
    // PostgreSQL Database
    "spring.datasource.url=jdbc:postgresql://10.0.151.40:5432/ipiece_test",
    "spring.datasource.username=postgres",
    "spring.datasource.password=postgres",
    "spring.datasource.driver-class-name=org.postgresql.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
    
    // AWS S3
    "cloud.aws.region.static=ap-northeast-2",
    "cloud.aws.credentials.access-key=test",
    "cloud.aws.credentials.secret-key=test",
    "cloud.aws.s3.bucket=test",
    "cloud.aws.stack.auto=false",

    // Redis
    "REDIS_HOST=10.0.133.84",
    "REDIS_PORT=6379",

    // JWT
    "JWT_SECRET=GDrstwtmYicx9S4RSb80AQfnfSEhJXAH4wVRi+m43huDkt4WiPxfmDE5V3Crk2S/tgLYoC7B0cjv2COTgHq1VA==",

    // SOLAPI (SMS)
    "SOLAPI_API_KEY=NCSJ85DSPA6GYLJL",
    "SOLAPI_API_SECRET=O2HMYLVGQMJKD6QDISOTMEI4EYSXM98A",
    "SOLAPI_SENDER=01023650244"
})
class OrderMatchingServiceIntegrationTest {

    @MockBean
    private BesuClient besuClient;
    
    @MockBean
    private LocalStorageService localStorageService;

    @MockBean
    private BlockchainService blockchainService;  // ← 추가!

    @Autowired
    private MarketService marketService;

    @Autowired
    private OrderMatchingService orderMatchingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VirtualAccountRepository virtualAccountRepository;

    @Autowired
    private HoldingsRepository holdingsRepository;

    @Autowired
    private BlockchainTransactionRepository blockchainTransactionRepository;

    @Autowired
    private OrderBookRepository orderBookRepository;

    @Autowired
    private TradeExecutionRepository tradeExecutionRepository;

    private User seller;
    private User buyer;
    private Product product;
    private VirtualAccount sellerAccount;
    private VirtualAccount buyerAccount;

    @BeforeEach
    void setUp() {
        // Mocking BesuClient behavior
        when(besuClient.isWhitelisted(anyString(), anyString())).thenReturn(true); // Assume already whitelisted for simplicity
        when(besuClient.transferToken(anyString(), anyString(), any(Long.class))).thenReturn("0x" + UUID.randomUUID().toString().replace("-", ""));
        when(besuClient.transferKrwt(anyString(), any(Long.class)))
            .thenReturn("0x" + UUID.randomUUID().toString().replace("-", ""));

        // ✅ BlockchainService Mock - 정산 성공으로 처리
        doNothing().when(blockchainService).settleTradeOnChain(
            any(OrderBook.class),
            any(OrderBook.class),
            anyLong(),
            anyLong()
        );

        // 1. Setup Users and Wallets
        seller = userRepository.save(User.builder().userMadeId("seller").joinDate(OffsetDateTime.now()).passwordHash("test123").build());
        buyer = userRepository.save(User.builder().userMadeId("buyer").joinDate(OffsetDateTime.now()).passwordHash("test123").build());

        sellerAccount = virtualAccountRepository.save(VirtualAccount.builder().user(seller).balanceKrw(0L).walletAddress("0xSELLER").accountNo("1111111111").build());
        buyerAccount = virtualAccountRepository.save(VirtualAccount.builder().user(buyer).balanceKrw(100_000L).walletAddress("0xBUYER").accountNo("2222222222").build());

        // 2. Setup Product
        product = productRepository.save(Product.builder()
                .productName("Test Product")
                .tokenContractAddress("0xTOKEN_CONTRACT")
                .status(ProductStatus.TRADE)
                .currentPrice(1000L)
                .totalTokenQuantity(1000L)
                .tokenName("TEST_TOKEN")
                .build());

        // 3. Setup Initial Holdings for Seller
        holdingsRepository.save(Holdings.builder()
                .product(product)
                .virtualAccount(sellerAccount)
                .quantity(100L)
                .avgBuyPrice(1000L)
                .build());
    }

    @Test
    @DisplayName("매수/매도 주문이 체결되면, 온체인 정산 로직이 실행되고 각 사용자의 자산이 정상적으로 변경된다.")
    void shouldSettleTradeOnChainWhenOrdersMatch() throws Exception {
        // Given
        long sellPrice = 1000L;
        long sellQuantity = 10L;
        OrderRequest sellRequest = new OrderRequest(sellPrice, sellQuantity, OffsetDateTime.now().toString());
        marketService.sell(product.getProductId(), seller.getUserId(), UUID.randomUUID().toString(), sellRequest);

        // When
        long buyPrice = 1000L;
        long buyQuantity = 10L;
        OrderRequest buyRequest = new OrderRequest(buyPrice, buyQuantity, OffsetDateTime.now().toString());
        marketService.buy(product.getProductId(), buyer.getUserId(), UUID.randomUUID().toString(), buyRequest);

        Thread.sleep(1000);

        // Then - 잔액 및 보유량 확인
        Holdings sellerHoldings = holdingsRepository.findByVirtualAccountAndProduct(sellerAccount, product).get();
        assertThat(sellerHoldings.getQuantity()).isEqualTo(90L);

        VirtualAccount finalSellerAccount = virtualAccountRepository.findById(sellerAccount.getAccountId()).get();
        assertThat(finalSellerAccount.getBalanceKrw()).isEqualTo(10_000L);

        Holdings buyerHoldings = holdingsRepository.findByVirtualAccountAndProduct(buyerAccount, product).get();
        assertThat(buyerHoldings.getQuantity()).isEqualTo(10L);

        VirtualAccount finalBuyerAccount = virtualAccountRepository.findById(buyerAccount.getAccountId()).get();
        assertThat(finalBuyerAccount.getBalanceKrw()).isEqualTo(90_000L);

        // ✅ BlockchainService가 호출되었는지 확인
        verify(blockchainService, times(1)).settleTradeOnChain(
            any(OrderBook.class),
            any(OrderBook.class),
            eq(10L),
            eq(1000L)
        );
    }

    @Test
    @DisplayName("매도 수량이 매수 수량보다 많으면 부분 체결되고 나머지는 주문장에 남는다")
    void shouldPartiallyMatchWhenSellQuantityIsGreater() throws Exception {
        // Given - 매도 20개
        long sellPrice = 1000L;
        long sellQuantity = 20L;
        OrderRequest sellRequest = new OrderRequest(sellPrice, sellQuantity, OffsetDateTime.now().toString());
        marketService.sell(product.getProductId(), seller.getUserId(), UUID.randomUUID().toString(), sellRequest);

        // When - 매수 10개
        long buyPrice = 1000L;
        long buyQuantity = 10L;
        OrderRequest buyRequest = new OrderRequest(buyPrice, buyQuantity, OffsetDateTime.now().toString());
        marketService.buy(product.getProductId(), buyer.getUserId(), UUID.randomUUID().toString(), buyRequest);

        Thread.sleep(1000);

        // Then
        // 1. 10개 체결됨
        List<TradeExecution> trades = tradeExecutionRepository.findAll();
        assertThat(trades).hasSize(1);
        assertThat(trades.get(0).getTradeQuantity()).isEqualTo(10L);

        // 2. 매도 주문 10개 남음
        List<OrderBook> remainingOrders = orderBookRepository.findAll()
            .stream()
            .filter(OrderBook::getPendingStatus)
            .toList();
        assertThat(remainingOrders).hasSize(1);
        assertThat(remainingOrders.get(0).getRemainQuantity()).isEqualTo(10L);

        // 3. 자산 변경 확인
        VirtualAccount finalSellerAccount = virtualAccountRepository.findById(sellerAccount.getAccountId()).get();
        assertThat(finalSellerAccount.getBalanceKrw()).isEqualTo(10_000L); // 10개만 팔림

        Holdings sellerHoldings = holdingsRepository.findByVirtualAccountAndProduct(sellerAccount, product).get();
        assertThat(sellerHoldings.getQuantity() + sellerHoldings.getPendingQuantity()).isEqualTo(90L); // 80 (available) + 10 (pending)
    }

    @Test
    @DisplayName("매수 수량이 매도 수량보다 많으면 부분 체결되고 나머지는 주문장에 남는다")
    void shouldPartiallyMatchWhenBuyQuantityIsGreater() throws Exception {
        // Given - 매도 10개
        long sellPrice = 1000L;
        long sellQuantity = 10L;
        OrderRequest sellRequest = new OrderRequest(sellPrice, sellQuantity, OffsetDateTime.now().toString());
        marketService.sell(product.getProductId(), seller.getUserId(), UUID.randomUUID().toString(), sellRequest);

        // When - 매수 20개
        long buyPrice = 1000L;
        long buyQuantity = 20L;
        OrderRequest buyRequest = new OrderRequest(buyPrice, buyQuantity, OffsetDateTime.now().toString());
        marketService.buy(product.getProductId(), buyer.getUserId(), UUID.randomUUID().toString(), buyRequest);

        Thread.sleep(1000);

        // Then
        // 1. 10개만 체결
        List<TradeExecution> trades = tradeExecutionRepository.findAll();
        assertThat(trades).hasSize(1);
        assertThat(trades.get(0).getTradeQuantity()).isEqualTo(10L);

        // 2. 매수 주문 10개 남음
        List<OrderBook> remainingOrders = orderBookRepository.findAll()
            .stream()
            .filter(OrderBook::getPendingStatus)
            .toList();
        assertThat(remainingOrders).hasSize(1);
        assertThat(remainingOrders.get(0).getRemainQuantity()).isEqualTo(10L);

        // 3. 자산 변경 확인
        VirtualAccount finalBuyerAccount = virtualAccountRepository.findById(buyerAccount.getAccountId()).get();
        assertThat(finalBuyerAccount.getBalanceKrw()).isEqualTo(80_000L); // 20개 주문 금액(20,000원)이 모두 pending으로 잡힘

        Holdings buyerHoldings = holdingsRepository.findByVirtualAccountAndProduct(buyerAccount, product).get();
        assertThat(buyerHoldings.getQuantity()).isEqualTo(10L);
    }

    @Test
    @DisplayName("매수가가 매도가보다 낮으면 체결되지 않는다")
    void shouldNotMatchWhenBuyPriceIsLowerThanSellPrice() throws Exception {
        // Given - 매도 1000원
        long sellPrice = 1000L;
        long sellQuantity = 10L;
        OrderRequest sellRequest = new OrderRequest(sellPrice, sellQuantity, OffsetDateTime.now().toString());
        marketService.sell(product.getProductId(), seller.getUserId(), UUID.randomUUID().toString(), sellRequest);

        // When - 매수 900원
        long buyPrice = 900L;
        long buyQuantity = 10L;
        OrderRequest buyRequest = new OrderRequest(buyPrice, buyQuantity, OffsetDateTime.now().toString());
        marketService.buy(product.getProductId(), buyer.getUserId(), UUID.randomUUID().toString(), buyRequest);

        Thread.sleep(1000);

        // Then
        // 1. 체결 안 됨
        List<TradeExecution> trades = tradeExecutionRepository.findAll();
        assertThat(trades).isEmpty();

        // 2. 두 주문 모두 주문장에 남아있음
        List<OrderBook> orders = orderBookRepository.findAll()
            .stream()
            .filter(OrderBook::getPendingStatus)
            .toList();
        assertThat(orders).hasSize(2);

        // 3. 자산 변경 없음
        VirtualAccount finalSellerAccount = virtualAccountRepository.findById(sellerAccount.getAccountId()).get();
        assertThat(finalSellerAccount.getBalanceKrw()).isEqualTo(0L);

        VirtualAccount finalBuyerAccount = virtualAccountRepository.findById(buyerAccount.getAccountId()).get();
        // 잔액은 주문 시점에 pending으로 빠져나감
        long expectedBuyerBalance = 100_000L - (buyPrice * buyQuantity);
        assertThat(finalBuyerAccount.getBalanceKrw()).isEqualTo(expectedBuyerBalance);
    }

    @Test
    @DisplayName("하나의 매수 주문이 여러 매도 주문과 순차적으로 체결된다")
    void shouldMatchMultipleSellOrdersWithOneBuyOrder() throws Exception {
        // Given - 두 개의 매도 주문
        OrderRequest sellRequest1 = new OrderRequest(1000L, 5L, OffsetDateTime.now().toString());
        marketService.sell(product.getProductId(), seller.getUserId(), UUID.randomUUID().toString(), sellRequest1);

        Thread.sleep(100); // 시간차 보장

        // 다른 판매자 또는 같은 판매자로 설정 가능. 여기서는 설명을 위해 같은 판매자 사용.
        OrderRequest sellRequest2 = new OrderRequest(1000L, 5L, OffsetDateTime.now().toString());
        marketService.sell(product.getProductId(), seller.getUserId(), UUID.randomUUID().toString(), sellRequest2);

        // When - 하나의 매수 주문 10개
        OrderRequest buyRequest = new OrderRequest(1000L, 10L, OffsetDateTime.now().toString());
        marketService.buy(product.getProductId(), buyer.getUserId(), UUID.randomUUID().toString(), buyRequest);

        Thread.sleep(1000);

        // Then
        // 1. 2번의 거래 체결
        List<TradeExecution> trades = tradeExecutionRepository.findAll();
        assertThat(trades).hasSize(2);
        assertThat(trades).extracting(TradeExecution::getTradeQuantity)
            .containsExactlyInAnyOrder(5L, 5L);

        // 2. 모든 주문 체결 완료
        List<OrderBook> remainingOrders = orderBookRepository.findAll()
            .stream()
            .filter(OrderBook::getPendingStatus)
            .toList();
        assertThat(remainingOrders).isEmpty();

        // 3. 자산 확인
        VirtualAccount finalSellerAccount = virtualAccountRepository.findById(sellerAccount.getAccountId()).get();
        assertThat(finalSellerAccount.getBalanceKrw()).isEqualTo(10_000L);

        Holdings sellerHoldings = holdingsRepository.findByVirtualAccountAndProduct(sellerAccount, product).get();
        assertThat(sellerHoldings.getQuantity() + sellerHoldings.getPendingQuantity()).isEqualTo(90L);
    }

    @Test
    @DisplayName("동일한 idempotencyKey로 중복 주문을 시도하면 두 번째 주문은 무시된다")
    void shouldPreventDuplicateOrdersWithSameIdempotencyKey() {
        // Given
        String idempotencyKey = UUID.randomUUID().toString();
        OrderRequest buyRequest = new OrderRequest(1000L, 10L, OffsetDateTime.now().toString());

        // When & Then
        // 첫 번째 주문은 성공
        marketService.buy(product.getProductId(), buyer.getUserId(), idempotencyKey, buyRequest);
        orderBookRepository.flush(); // 영속성 컨텍스트를 DB에 반영

        // 두 번째 주문은 BusinessException (DUPLICATE_ORDER) 발생
        assertThatThrownBy(() ->
            marketService.buy(product.getProductId(), buyer.getUserId(), idempotencyKey, buyRequest)
        ).isInstanceOf(Exception.class);

        // Then - 주문 1개만 생성됨
        List<OrderBook> orders = orderBookRepository.findAll();
        assertThat(orders).hasSize(1);

        // 자산도 한 번만 차감
        VirtualAccount finalBuyerAccount = virtualAccountRepository.findById(buyerAccount.getAccountId()).get();
        long expectedBalance = 100_000L - (1000L * 10L); // 한 번만 차감
        assertThat(finalBuyerAccount.getBalanceKrw()).isEqualTo(expectedBalance);
    }

    @Test
    @DisplayName("구매자의 잔액이 부족하면 매수 주문이 실패한다")
    void shouldFailWhenBuyerHasInsufficientBalance() {
        // Given - 잔액 100,000원

        // When & Then - 200,000원 주문 시도
        OrderRequest buyRequest = new OrderRequest(20_000L, 10L, OffsetDateTime.now().toString());

        assertThatThrownBy(() ->
            marketService.buy(product.getProductId(), buyer.getUserId(), UUID.randomUUID().toString(), buyRequest)
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("잔액");
    }

    @Test
    @DisplayName("판매자의 보유량이 부족하면 매도 주문이 실패한다")
    void shouldFailWhenSellerHasInsufficientHoldings() {
        // When & Then - 보유량 100개인데 200개 팔려고 시도
        OrderRequest sellRequest = new OrderRequest(1000L, 200L, OffsetDateTime.now().toString());

        assertThatThrownBy(() ->
            marketService.sell(product.getProductId(), seller.getUserId(), UUID.randomUUID().toString(), sellRequest)
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("보유");
    }

    @Test
    @DisplayName("동일 가격의 주문들은 먼저 들어온 순서대로 체결된다 (FIFO)")
    void shouldMatchOrdersInFIFOOrder() throws Exception {
        // Given - 3개의 매도 주문 (각 3개씩)
        String seller1Id = UUID.randomUUID().toString();
        OrderRequest sellRequest1 = new OrderRequest(1000L, 3L, OffsetDateTime.now().toString());
        marketService.sell(product.getProductId(), seller.getUserId(), seller1Id, sellRequest1);

        Thread.sleep(100);

        String seller2Id = UUID.randomUUID().toString();
        OrderRequest sellRequest2 = new OrderRequest(1000L, 3L, OffsetDateTime.now().toString());
        marketService.sell(product.getProductId(), seller.getUserId(), seller2Id, sellRequest2);

        Thread.sleep(100);

        String seller3Id = UUID.randomUUID().toString();
        OrderRequest sellRequest3 = new OrderRequest(1000L, 3L, OffsetDateTime.now().toString());
        marketService.sell(product.getProductId(), seller.getUserId(), seller3Id, sellRequest3);

        // When - 5개 매수
        OrderRequest buyRequest = new OrderRequest(1000L, 5L, OffsetDateTime.now().toString());
        marketService.buy(product.getProductId(), buyer.getUserId(), UUID.randomUUID().toString(), buyRequest);

        Thread.sleep(1000);

        // Then - 첫 번째 주문 3개 + 두 번째 주문 2개 체결
        List<OrderBook> orders = orderBookRepository.findAll();

        // 첫 번째 주문: 완전 체결 (remainQuantity = 0)
        OrderBook firstOrder = orders.stream()
            .filter(o -> o.getIdempotencyKey().equals(seller1Id))
            .findFirst().get();
        assertThat(firstOrder.getRemainQuantity()).isEqualTo(0L);

        // 두 번째 주문: 부분 체결 (remainQuantity = 1)
        OrderBook secondOrder = orders.stream()
            .filter(o -> o.getIdempotencyKey().equals(seller2Id))
            .findFirst().get();
        assertThat(secondOrder.getRemainQuantity()).isEqualTo(1L);

        // 세 번째 주문: 체결 안 됨 (remainQuantity = 3)
        OrderBook thirdOrder = orders.stream()
            .filter(o -> o.getIdempotencyKey().equals(seller3Id))
            .findFirst().get();
        assertThat(thirdOrder.getRemainQuantity()).isEqualTo(3L);
    }
}

package com.masterpiece.IPiece.blockchain.application;

import com.masterpiece.IPiece.blockchain.api.dto.request.DividendExecuteRequest;
import com.masterpiece.IPiece.blockchain.api.dto.request.DividendSimulateRequest;
import com.masterpiece.IPiece.blockchain.api.dto.response.DividendExecuteResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.DividendSimulateResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.MyDividendsResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.ProjectDividendsResponse;
import com.masterpiece.IPiece.blockchain.contract.DividendDistributor;
import com.masterpiece.IPiece.blockchain.domain.BlockchainTransaction;
import com.masterpiece.IPiece.blockchain.domain.TransactionStatus;
import com.masterpiece.IPiece.blockchain.domain.TransactionType;
import com.masterpiece.IPiece.blockchain.infra.jpa.BlockchainTransactionRepository;
import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.dividends.domain.DividendStatus;
import com.masterpiece.IPiece.dividends.domain.Dividends;
import com.masterpiece.IPiece.dividends.infra.DividendsRepository;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.infra.UserRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class DividendService {

    private final Web3j web3j;
    private final Credentials adminCredentials;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BlockchainTransactionRepository blockchainTransactionRepository;
    private final DividendsRepository dividendsRepository;

    public DividendExecuteResponse executeDividend(Long userId, DividendExecuteRequest request) {
        // 1. Product 및 컨트랙트 정보 조회
        User adminUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "관리자 정보를 찾을 수 없습니다."));

        Product product = productRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (StringUtils.isEmpty(product.getDividendContractAddress())) {
            throw new BusinessException(ErrorCode.CONTRACT_ADDRESS_NOT_FOUND, "배당 컨트랙트 주소가 등록되지 않았습니다.");
        }

        TransactionReceipt transactionReceipt;
        try {
            // 2. Web3j를 사용하여 배당 스마트 컨트랙트의 `distribute` 함수 호출
            ContractGasProvider gasProvider = new DefaultGasProvider();
            DividendDistributor dividendDistributor = DividendDistributor.load(
                    product.getDividendContractAddress(),
                    web3j,
                    adminCredentials,
                    gasProvider
            );

            BigInteger totalAmount = BigInteger.valueOf(request.getTotalAmount());
            transactionReceipt = dividendDistributor.distribute(totalAmount).send();

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.BLOCKCHAIN_ERROR, "배당 실행 중 블록체인 오류가 발생했습니다.", e);
        }

        // 3. 트랜잭션 발생 후 `BlockchainTransaction` 엔티티 생성 및 저장
        BlockchainTransaction tx = BlockchainTransaction.builder()
                .txHash(transactionReceipt.getTransactionHash())
                .txType(TransactionType.DIVIDEND)
                .fromAddress(transactionReceipt.getFrom())
                .toAddress(transactionReceipt.getTo())
                .amount(request.getTotalAmount())
                .blockNumber(transactionReceipt.getBlockNumber().longValue())
                .status(transactionReceipt.isStatusOK() ? TransactionStatus.SUCCESS : TransactionStatus.FAILED)
                .user(adminUser)
                .build();
        blockchainTransactionRepository.save(tx);

        // 4. `Dividend` 엔티티 생성 및 저장
        Dividends dividend = Dividends.builder()
                .product(product)
                .recordDate(request.getRecordDate())
                .payoutDate(request.getPaymentDate())
                .totalAmount(request.getTotalAmount())
                .transactionHash(transactionReceipt.getTransactionHash())
                .blockNumber(transactionReceipt.getBlockNumber().longValue())
                .status(transactionReceipt.isStatusOK() ? DividendStatus.COMPLETED : DividendStatus.FAILED)
                .build();
        Dividends savedDividend = dividendsRepository.save(dividend);

        // 5. 최종 `DividendExecuteResponse` DTO 생성 및 반환
        return DividendExecuteResponse.builder()
                .dividendId(savedDividend.getDividendId())
                .projectId(product.getProductId())
                .projectName(product.getProjectName())
                .totalAmount(request.getTotalAmount())
                .distributedAmount(0L) // TODO: 이벤트 리스너를 통해 실제 분배된 금액 업데이트 필요
                .remainderAmount(0L)   // TODO: 이벤트 리스너를 통해 실제 남은 금액 업데이트 필요
                .recipientCount(0)     // TODO: 이벤트 리스너를 통해 실제 수령자 수 업데이트 필요
                .transactionHash(transactionReceipt.getTransactionHash())
                .status(transactionReceipt.isStatusOK() ? "COMPLETED" : "FAILED")
                .executedAt(LocalDateTime.now())
                .build();
    }

    public DividendSimulateResponse simulateDividend(DividendSimulateRequest request) {
        // 1. 시뮬레이션 로직
        throw new UnsupportedOperationException("배당 시뮬레이션 기능이 아직 구현되지 않았습니다");
    }

    @Transactional(readOnly = true)
    public MyDividendsResponse getMyDividends(Long userId, int page, int size) {
        // 1. 내 배당 내역 조회 로직
        throw new UnsupportedOperationException("내 배당 조회 기능이 아직 구현되지 않았습니다");
    }

    @Transactional(readOnly = true)
    public ProjectDividendsResponse getProjectDividends(Long projectId) {
        // 1. 프로젝트별 배당 내역 조회 로직
        throw new UnsupportedOperationException("프로젝트 배당 조회 기능이 아직 구현되지 않았습니다");
    }
}

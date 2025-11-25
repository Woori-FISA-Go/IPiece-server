package com.masterpiece.IPiece.integration.besu;

import com.masterpiece.IPiece.common.exception.BlockchainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.crypto.RawTransaction; // RawTransaction import 추가
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;
import lombok.extern.slf4j.Slf4j;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Arrays;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.DefaultBlockParameterName;

@Slf4j
@Component
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true", matchIfMissing = true)
public class BesuClient {

    private final Web3j web3j;
    private final Credentials credentials;
    private final String krwtContractAddress;

    public BesuClient(
            Web3j web3j,
            @Value("${admin.private-key}") String adminPrivateKey,
            @Value("${krwt.contract.address}") String krwtContractAddress) {
        validateInputs(adminPrivateKey, krwtContractAddress);
        this.web3j = web3j;
        this.credentials = Credentials.create(adminPrivateKey);
        this.krwtContractAddress = krwtContractAddress;
    }

    private void validateInputs(String privateKey, String contractAddress) {
        if (!StringUtils.hasText(privateKey)) {
            throw new IllegalArgumentException("Admin private key must not be empty");
        }
        if (!WalletUtils.isValidPrivateKey(privateKey)) {
            throw new IllegalArgumentException("Admin private key has invalid format");
        }
        if (!StringUtils.hasText(contractAddress)) {
            throw new IllegalArgumentException("KRWT contract address must not be empty");
        }
        // Basic Ethereum address format validation (0x followed by 40 hex characters)
        if (!contractAddress.matches("^0x[0-9a-fA-F]{40}$")) {
            throw new IllegalArgumentException("Invalid KRWT contract address format");
        }
    }

    /**
     * 배당/송금 로그에 사용하기 위한 admin 지갑 주소 반환
     */
    public String getAdminAddress() {
        return credentials.getAddress();
    }

    public BigDecimal getKrwtBalance(String walletAddress) {
        // Validate wallet address format
        if (!StringUtils.hasText(walletAddress) || !walletAddress.matches("^0x[0-9a-fA-F]{40}$")) {
            throw new IllegalArgumentException("Invalid wallet address: " + walletAddress);
        }

        // 1. 'balanceOf(address)' 함수 시그니처 생성
        Function function = new Function(
                "balanceOf",
                Collections.singletonList(new Address(walletAddress)),
                Collections.singletonList(new TypeReference<Uint256>() {
                })
        );

        // 2. web3j를 통해 컨트랙트 함수 호출 (eth_call)
        try {
            String encodedFunction = FunctionEncoder.encode(function);
            Transaction transaction = Transaction.createEthCallTransaction(
                    credentials.getAddress(), krwtContractAddress, encodedFunction);

            EthCall response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();

            // 3. 결과 디코딩
            List<Type> result = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
            if (result.isEmpty()) {
                throw new BlockchainException("Failed to get balance, empty response from contract.");
            }

            BigInteger balanceInWei = (BigInteger) result.get(0).getValue();

            // 4. KRWT는 소수점 0자리를 사용하므로, BigInteger를 BigDecimal로 직접 변환
            return new BigDecimal(balanceInWei); // decimals가 0이므로 Convert.fromWei 사용 안 함

        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch KRWT balance for wallet: " + walletAddress, e);
        }
    }

    public String transferKrwt(String toAddress, long amount) {
        if (!StringUtils.hasText(toAddress) || !toAddress.matches("^0x[0-9a-fA-F]{40}$")) {
            throw new IllegalArgumentException("Invalid receiver address: " + toAddress);
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        try {
            Function function = new Function(
                    "transfer",
                    java.util.Arrays.asList(
                            new Address(toAddress),
                            new Uint256(BigInteger.valueOf(amount))
                    ),
                    java.util.Collections.<TypeReference<?>>emptyList()
            );

            String data = FunctionEncoder.encode(function);

            // 🔹 gasPrice 설정
            BigInteger gasPrice = BigInteger.ZERO;
            BigInteger gasLimit = DefaultGasProvider.GAS_LIMIT;

            TransactionManager txManager = new RawTransactionManager(web3j, credentials);
            EthSendTransaction response = txManager.sendTransaction(
                    gasPrice,
                    gasLimit,
                    krwtContractAddress,
                    data,
                    BigInteger.ZERO
            );

            if (response.hasError()) {
                var err = response.getError();
                log.error("[BesuClient] KRWT transfer RPC error: code={}, message={}",
                        err.getCode(), err.getMessage());
                throw new BlockchainException("KRWT transfer failed: " + response.getError().getMessage());
            }

            String txHash = response.getTransactionHash();
            log.info("KRWT transfer submitted: from={} to={} amount={} txHash={} gasPrice={}",
                    credentials.getAddress(), toAddress, amount, txHash, gasPrice);
            return txHash;

        } catch (Exception e) {
            throw new BlockchainException("Failed to send KRWT transfer to " + toAddress, e);
        }
    }

    public String mintKrwt(String toAddress, BigInteger amount) {
        log.info("Minting {} KRWT to {}", amount, toAddress);
        Function mintFunction = new Function(
                "mint",
                Arrays.asList(new Address(toAddress), new Uint256(amount)),
                Collections.emptyList());
        return sendContractTransaction(mintFunction);
    }

    public String burnKrwt(String fromAddress, BigInteger amount) {
        log.info("Burning {} KRWT from {}", amount, fromAddress);
        Function burnFunction = new Function(
                "burn",
                Arrays.asList(new Address(fromAddress), new Uint256(amount)),
                Collections.emptyList());
        return sendContractTransaction(burnFunction);
    }

    // ✅ 공통 트랜잭션 전송 로직을 헬퍼 메서드로 추출
    private String sendContractTransaction(Function function) {
        try {
            BigInteger currentNonce = web3j.ethGetTransactionCount(
                    credentials.getAddress(), DefaultBlockParameterName.LATEST
            ).send().getTransactionCount();

            String data = FunctionEncoder.encode(function);
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
            BigInteger gasLimit = BigInteger.valueOf(9000000); // 적절한 gasLimit 설정

            RawTransaction rawTx = RawTransaction.createTransaction(
                    currentNonce, gasPrice, gasLimit, krwtContractAddress, BigInteger.ZERO, data);

            byte[] signedTx = TransactionEncoder.signMessage(
                    rawTx,
                    web3j.ethChainId().send().getChainId().longValue(), credentials);

            EthSendTransaction response = web3j
                    .ethSendRawTransaction(Numeric.toHexString(signedTx)).send();

            if (response.hasError()) {
                throw new BlockchainException("Transaction failed: " + response.getError().getMessage());
            }

            String txHash = response.getTransactionHash();
            waitForTransactionReceipt(txHash); // 트랜잭션 마이닝 대기
            return txHash;

        } catch (Exception e) {
            log.error("Failed to send contract transaction: {}", e.getMessage(), e);
            throw new BlockchainException("Failed to send contract transaction", e);
        }
    }

    private void waitForTransactionReceipt(String transactionHash) {
        int attempts = 0;
        int maxAttempts = 30; // Wait for up to 30 seconds
        long sleepTime = 1000; // 1 second

        while (attempts < maxAttempts) {
            try {
                org.web3j.protocol.core.methods.response.EthGetTransactionReceipt ethGetTransactionReceipt =
                        web3j.ethGetTransactionReceipt(transactionHash).send();

                if (ethGetTransactionReceipt.getTransactionReceipt().isPresent()) {
                    log.info("Transaction {} mined after {} attempts.", transactionHash, attempts + 1);
                    return; // Transaction mined
                }
            } catch (Exception e) {
                log.warn("Error while waiting for transaction receipt {}: {}", transactionHash, e.getMessage());
            }

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BlockchainException("Transaction receipt waiting interrupted", e);
            }
            attempts++;
        }
        log.warn("Transaction {} not mined after {} attempts.", transactionHash, maxAttempts);
    throw new BlockchainException("Transaction not mined within timeout: " + transactionHash); // 이 라인 추가
    }

    /**
     * KRWT 컨트랙트 주소 반환
     * - BlockchainTransaction.contractAddress 로 사용
     */
    public String getKrwtContractAddress() {
        return krwtContractAddress;
    }

    public BigDecimal getTokenBalance(String contractAddress, String walletAddress) {
        if (!StringUtils.hasText(walletAddress) || !walletAddress.matches("^0x[0-9a-fA-F]{40}$")) {
            throw new IllegalArgumentException("Invalid wallet address: " + walletAddress);
        }
        if (!StringUtils.hasText(contractAddress) || !contractAddress.matches("^0x[0-9a-fA-F]{40}$")) {
            throw new IllegalArgumentException("Invalid contract address: " + contractAddress);
        }

        Function function = new Function(
                "balanceOf",
                Collections.singletonList(new Address(walletAddress)),
                Collections.singletonList(new TypeReference<Uint256>() {})
        );

        try {
            String encodedFunction = FunctionEncoder.encode(function);
            Transaction transaction = Transaction.createEthCallTransaction(
                    credentials.getAddress(), contractAddress, encodedFunction);
            EthCall response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
            List<Type> result = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
            if (result.isEmpty()) {
                throw new BlockchainException("Failed to get balance, empty response from contract.");
            }
            BigInteger balanceInWei = (BigInteger) result.get(0).getValue();
            return Convert.fromWei(new BigDecimal(balanceInWei), Convert.Unit.ETHER);
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch token balance for contract: " + contractAddress, e);
        }
    }

    /**
     * Adds a user's wallet address to the whitelist of a specific token contract.
     * This is a placeholder method.
     * @param contractAddress The address of the token smart contract.
     * @param userWalletAddress The wallet address of the user to be whitelisted.
     */
    public String addToWhitelist(String contractAddress, String userWalletAddress) {
        // Validate contract address format
        if (!StringUtils.hasText(contractAddress) || !contractAddress.matches("^0x[0-9a-fA-F]{40}$")) {
            throw new IllegalArgumentException("Invalid contract address: " + contractAddress);
        }
        // Validate user wallet address format
        if (!StringUtils.hasText(userWalletAddress) || !userWalletAddress.matches("^0x[0-9a-fA-F]{40}$")) {
            throw new IllegalArgumentException("Invalid user wallet address: " + userWalletAddress);
        }
        log.info("[MOCK] Adding address {} to whitelist for contract {}", userWalletAddress, contractAddress);
        // In a real implementation, this would encode and send a transaction
        // to the 'addToWhitelist' function of the smart contract at 'contractAddress'.
        return randomHash();
    }

    /**
     * Transfers a specified amount of tokens from the admin's wallet to a target address.
     * This is a placeholder method.
     * @param contractAddress The address of the token smart contract.
     * @param toAddress The recipient's wallet address.
     * @param amount The amount of tokens to transfer.
     * @return A dummy transaction hash.
     */
    public String transferToken(String contractAddress, String toAddress, Integer amount) {
        // Validate contract address format
        if (!StringUtils.hasText(contractAddress) || !contractAddress.matches("^0x[0-9a-fA-F]{40}$")) {
            throw new IllegalArgumentException("Invalid contract address: " + contractAddress);
        }
        // Validate recipient address format
        if (!StringUtils.hasText(toAddress) || !toAddress.matches("^0x[0-9a-fA-F]{40}$")) {
            throw new IllegalArgumentException("Invalid recipient address: " + toAddress);
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than 0.");
        }

        log.info("[MOCK] Transferring {} tokens from admin to {} for contract {}", amount, toAddress, contractAddress);
        // In a real implementation, this would encode and send a transaction
        // to the 'transfer' function of the smart contract at 'contractAddress'.
        return randomHash(); // Dummy transaction hash
    }

    /**
     * 트랜잭션 해시로 실제 이더리움/Besu 트랜잭션 리시트를 조회한다.
     * - eth_getTransactionReceipt 호출
     * - 없으면(Pending) status=PENDING 으로 반환
     * - 있으면 block/timestamp 정보까지 채워서 Map 으로 반환
     */
    public Map<String, Object> getTransactionReceipt(String transactionHash) {
        if (!StringUtils.hasText(transactionHash) || !transactionHash.matches("^0x[0-9a-fA-F]{64}$")) {
            throw new IllegalArgumentException("Invalid transaction hash: " + transactionHash);
        }
        log.info("Getting transaction receipt for hash {}", transactionHash);

        try {
            org.web3j.protocol.core.methods.response.EthGetTransactionReceipt ethGetTransactionReceipt =
                    web3j.ethGetTransactionReceipt(transactionHash).send();

            if (ethGetTransactionReceipt.hasError()) {
                throw new BlockchainException("Error fetching transaction receipt: " + ethGetTransactionReceipt.getError().getMessage());
            }

            var optReceipt = ethGetTransactionReceipt.getTransactionReceipt();

            // ✅ PENDING 상태 처리
            if (optReceipt.isEmpty()) {
                Map<String, Object> pendingResult = new java.util.HashMap<>();
                pendingResult.put("hash", transactionHash);
                pendingResult.put("status", "PENDING");
                pendingResult.put("blockNumber", null);
                pendingResult.put("blockHash", null);
                pendingResult.put("from", null);
                pendingResult.put("to", null);
                pendingResult.put("value", "0");
                pendingResult.put("gasUsed", null);
                pendingResult.put("gasPrice", null); // PENDING 상태일 때 gasPrice는 알 수 없음
                pendingResult.put("timestamp", null);
                pendingResult.put("logs", Collections.emptyList()); // PENDING 상태일 때 logs도 비어있음
                return pendingResult;
            }

            org.web3j.protocol.core.methods.response.TransactionReceipt receipt = optReceipt.get();
            log.info("Transaction receipt status for hash {}: {}", transactionHash, receipt.getStatus());

            // ✅ 블록 타임스탬프 조회
            OffsetDateTime timestamp = null;
            if (receipt.getBlockHash() != null) {
                try {
                    org.web3j.protocol.core.methods.response.EthBlock ethBlock =
                            web3j.ethGetBlockByHash(receipt.getBlockHash(), false).send();
                    if (ethBlock.hasError()) {
                        log.warn("Error fetching block for hash {}: {}",
                                receipt.getBlockHash(), ethBlock.getError().getMessage());
                    } else if (ethBlock.getBlock() != null && ethBlock.getBlock().getTimestamp() != null) {
                        timestamp = OffsetDateTime.ofInstant(
                                java.time.Instant.ofEpochSecond(
                                        ethBlock.getBlock().getTimestamp().longValue()),
                                java.time.ZoneOffset.UTC
                        );
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch block timestamp for {}: {}",
                            receipt.getBlockHash(), e.getMessage());
                }
            }

            // ✅ Logs 처리 (HashMap 사용)
            List<Map<String, Object>> logs = receipt.getLogs().stream()
                    .map(log -> {
                        Map<String, Object> logMap = new java.util.HashMap<>();
                        logMap.put("address", log.getAddress());
                        logMap.put("topics", log.getTopics() != null ? log.getTopics() : java.util.List.of());
                        logMap.put("data", log.getData() != null ? log.getData() : "");
                        logMap.put("blockNumber", log.getBlockNumber() != null ?
                                log.getBlockNumber().toString() : null);
                        logMap.put("transactionHash", log.getTransactionHash() != null ?
                                log.getTransactionHash() : null);
                        logMap.put("logIndex", log.getLogIndex() != null ?
                                log.getLogIndex().toString() : null);
                        return logMap;
                    })
                    .toList();

            // ✅ Status 매핑 (0x1=success, 0x0=failed)
            String rawStatus = receipt.getStatus();
            String mappedStatus;
            if ("0x1".equalsIgnoreCase(rawStatus)) {
                mappedStatus = "success";
            } else if ("0x0".equalsIgnoreCase(rawStatus)) {
                mappedStatus = "failed";
            } else {
                mappedStatus = rawStatus != null ? rawStatus : "unknown";
            }

            // ✅ 최종 결과 (HashMap 사용)
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("hash", transactionHash);
            result.put("status", mappedStatus);
            result.put("blockHash", receipt.getBlockHash());
            result.put("blockNumber", receipt.getBlockNumber() != null ?
                    receipt.getBlockNumber().longValue() : null);
            result.put("from", receipt.getFrom());
            result.put("to", receipt.getTo() != null ? receipt.getTo() : "");
            result.put("value", BigInteger.ZERO.toString());
            result.put("gasUsed", receipt.getGasUsed() != null ?
                    receipt.getGasUsed().toString() : "0");
            result.put("gasPrice", null); // TransactionReceipt에는 gasPrice 없음
            result.put("timestamp", timestamp != null ? timestamp.toString() : null);
            result.put("logs", logs);

            return result;

        } catch (Exception e) {
            log.error("Failed to get transaction receipt for hash {}", transactionHash, e);
            throw new BlockchainException(
                    "Failed to get transaction receipt for hash: " + transactionHash, e
            );
        }
    }

    /**
     * Retrieves information about deployed contracts (KRWT, TokenFactory, etc.).
     * This is a placeholder method.
     * @return A dummy map containing contract information.
     */
    public Map<String, Object> getContractInfo() {
        log.info("[MOCK] Getting contract information.");
        // In a real implementation, this would query the blockchain for contract details.
        return Map.of(
                "krwt", Map.of(
                        "address", krwtContractAddress,
                        "name", "Korean Won Token",
                        "symbol", "KRWT",
                        "decimals", 0,
                        "totalSupply", "10000000000",
                        "owner", credentials.getAddress()
                ),
                "tokenFactory", Map.of(
                        "address", randomAddress(),
                        "tokensCreated", 5,
                        "owner", credentials.getAddress()
                ),
                "tokens", List.of(
                        Map.of(
                                "projectId", UUID.randomUUID().toString(),
                                "address", randomAddress(),
                                "dividendAddress", randomAddress()
                        )
                )
        );
    }

    // 체인 상태 조회용 메서드
    public long getLatestBlockNumber() {
        try {
            return web3j.ethBlockNumber().send().getBlockNumber().longValue();
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch latest block number", e);
        }
    }

    public int getPeerCount() {
        try {
            return web3j.netPeerCount().send().getQuantity().intValue();
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch peer count", e);
        }
    }

    public boolean isSyncing() {
        try {
            var response = web3j.ethSyncing().send();
            return response.isSyncing();
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch syncing status", e);
        }
    }

    public long getGasPrice() {
        try {
            return web3j.ethGasPrice().send().getGasPrice().longValue();
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch gas price", e);
        }
    }

    public String getNetworkId() {
        try {
            return web3j.netVersion().send().getNetVersion();
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch network id", e);
        }
    }

    // ==========================
    // 추가: 체인 ID 및 최신 블록 요약
    // ==========================

    public String getChainId() {
        try {
            // eth_chainId는 BigInteger로 체인 ID를 반환
            var response = web3j.ethChainId().send();
            java.math.BigInteger chainId = response.getChainId();

            // 0x-prefixed hex string 으로 변환
            return "0x" + chainId.toString(16);
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch chain id", e);
        }
    }

    public LatestBlockSummary getLatestBlockSummary() {
        try {
            var response = web3j.ethGetBlockByNumber(
                    DefaultBlockParameterName.LATEST,
                    false // 트랜잭션 전체가 아니라 hash만 가져오도록
            ).send();

            var block = response.getBlock();
            if (block == null) {
                throw new BlockchainException("Latest block is null");
            }

            long number = block.getNumber().longValue();
            long gasUsed = block.getGasUsed().longValue();
            long gasLimit = block.getGasLimit().longValue();
            int txCount = block.getTransactions().size();

            return new LatestBlockSummary(number, gasUsed, gasLimit, txCount);
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch latest block summary", e);
        }
    }

    public record LatestBlockSummary(
            long number,
            long gasUsed,
            long gasLimit,
            int txCount
    ) {}

    // Helper method to generate a dummy Ethereum transaction hash (0x + 64 hex chars)
    public String randomHash() {
        String hex = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        return "0x" + hex.substring(0, 64);
    }

    // Helper method to generate a dummy Ethereum address (0x + 40 hex chars)
    public String randomAddress() {
        String hex = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        return "0x" + hex.substring(0, 40);
    }

    /**
     * KRWT 컨트랙트의 소유자 주소 조회
     */
    public String getKrwtContractOwner() {
        Function function = new Function(
                "owner",  // owner() 함수
                Collections.emptyList(),  // 파라미터 없음
                Collections.singletonList(new TypeReference<Address>() {})
        );

        try {
            String encodedFunction = FunctionEncoder.encode(function);
            Transaction transaction = Transaction.createEthCallTransaction(
                    credentials.getAddress(),
                    krwtContractAddress,
                    encodedFunction
            );

            EthCall response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
            List<Type> result = FunctionReturnDecoder.decode(
                    response.getValue(),
                    function.getOutputParameters()
            );

            if (result.isEmpty()) {
                throw new BlockchainException("Failed to get owner, empty response");
            }

            String ownerAddress = result.get(0).getValue().toString();
            log.info("KRWT Contract Owner: {}", ownerAddress);
            log.info("Admin Address: {}", credentials.getAddress());
            log.info("Owner matches Admin: {}",
                    ownerAddress.equalsIgnoreCase(credentials.getAddress()));

            return ownerAddress;

        } catch (Exception e) {
            throw new BlockchainException("Failed to get KRWT contract owner", e);
        }
    }
}
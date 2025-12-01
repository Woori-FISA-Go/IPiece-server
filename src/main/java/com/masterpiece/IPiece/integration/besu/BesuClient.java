package com.masterpiece.IPiece.integration.besu;

import com.masterpiece.IPiece.common.exception.BlockchainException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Component
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true", matchIfMissing = true)
public class BesuClient {

    private final BigInteger gasPrice = BigInteger.ONE;          // legacy gas price (wei) fixed to 1 for Besu CLI alignment
    private final BigInteger defaultGasLimit;   // fallback gas limit when estimate fails
    // 토큰 생성 이벤트 (TokenFactory.sol 기준)
    // event TokenCreated(string name, string symbol, address tokenAddress, address dividendAddress, uint256 totalSupply);
    private static final Event TOKEN_CREATED_EVENT = new Event(
            "TokenCreated",
            Arrays.<TypeReference<?>>asList(
                    new TypeReference<Utf8String>(true) {},  // name
                    new TypeReference<Utf8String>() {},  // symbol
                    new TypeReference<Address>() {},     // tokenAddress
                    new TypeReference<Address>() {},     // dividendAddress
                    new TypeReference<Uint256>() {}      // totalSupply
            )
    );

    private final Web3j web3j;
    private final Credentials credentials;
    private final String krwtContractAddress;
    private final String tokenFactoryAddress;
    private final BigInteger chainId;
    private final String adminAddress;

    public BesuClient(
            Web3j web3j,
            @Value("${admin.private-key}") String adminPrivateKey,
            @Value("${krwt.contract.address}") String krwtContractAddress,
            @Value("${tokenfactory.contract.address:}") String tokenFactoryAddress,
            @Value("${besu.chain-id:0x1350195}") String chainIdHex,
            @Value("${besu.tx.gas-limit.default:3000000}") String defaultGasLimit
    ) {
        validateInputs(adminPrivateKey, krwtContractAddress);
        this.web3j = web3j;
        this.credentials = Credentials.create(adminPrivateKey);
        this.krwtContractAddress = krwtContractAddress;
        this.tokenFactoryAddress = tokenFactoryAddress;
        this.chainId = parseChainId(chainIdHex);
        this.defaultGasLimit = parsePositiveBigInt(defaultGasLimit, "default gas limit");
        this.adminAddress = this.credentials.getAddress();
    }

    private BigInteger parseChainId(String chainIdHex) {
        try {
            if (chainIdHex.startsWith("0x") || chainIdHex.startsWith("0X")) {
                return new BigInteger(chainIdHex.substring(2), 16);
            }
            return new BigInteger(chainIdHex);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid chain id: " + chainIdHex, e);
        }
    }

    private BigInteger parsePositiveBigInt(String value, String label) {
        try {
            BigInteger bi = new BigInteger(value);
            if (bi.compareTo(BigInteger.ZERO) < 0) {
                throw new IllegalArgumentException(label + " must be >= 0");
            }
            return bi;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid " + label + ": " + value, e);
        }
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
        if (!contractAddress.matches("^0x[0-9a-fA-F]{40}$")) {
            throw new IllegalArgumentException("Invalid KRWT contract address format");
        }
    }

    public String getAdminAddress() {
        return credentials.getAddress();
    }

    // ---------- Low-level helpers ----------

    public BigInteger getNonce(String address) {
        try {
            EthGetTransactionCount res = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send();
            return res.getTransactionCount();
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch nonce for " + address, e);
        }
    }

    public BigInteger estimateGas(String to, String data, BigInteger value) {
        try {
            Transaction tx = Transaction.createFunctionCallTransaction(
                    credentials.getAddress(),
                    null,
                    gasPrice,
                    null,
                    to,
                    value,
                    data
            );
            EthEstimateGas res = web3j.ethEstimateGas(tx).send();
            if (res.hasError()) {
                log.warn("estimateGas error: code={}, message={}", res.getError().getCode(), res.getError().getMessage());
                return defaultGasLimit;
            }
            BigInteger gas = res.getAmountUsed();
            // add simple buffer
            return gas.add(BigInteger.valueOf(100_000L));
        } catch (Exception e) {
            log.warn("estimateGas failed, fallback to default: {}", e.getMessage());
            return defaultGasLimit;
        }
    }

    private String sendRawTransaction(String to, String data, BigInteger value) {
        try {
            BigInteger nonce = getNonce(credentials.getAddress());
            BigInteger gasLimit = estimateGas(to, data, value);

            RawTransaction rawTx = RawTransaction.createTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    to,
                    value,
                    data
            );

            byte[] signed = TransactionEncoder.signMessage(rawTx, chainId.longValue(), credentials);
            String hex = Numeric.toHexString(signed);
            EthSendTransaction resp = web3j.ethSendRawTransaction(hex).send();
            if (resp.hasError()) {
                throw new BlockchainException("sendRawTransaction failed: " + resp.getError().getMessage());
            }
            String txHash = resp.getTransactionHash();
            log.info("Submitted tx: from={} to={} nonce={} gasPrice={} gasLimit={} chainId={} txHash={}", credentials.getAddress(), to, nonce, gasPrice, gasLimit, chainId, txHash);
            return txHash;
        } catch (Exception e) {
            throw new BlockchainException("Failed to send raw transaction to " + to, e);
        }
    }

    // Overload: use pre-computed gasLimit (legacy tx, gasPrice=1)
    private String sendRawTransaction(String to, String data, BigInteger value, BigInteger gasLimit) {
        try {
            BigInteger nonce = getNonce(credentials.getAddress());

            RawTransaction rawTx = RawTransaction.createTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    to,
                    value,
                    data
            );

            byte[] signed = TransactionEncoder.signMessage(rawTx, chainId.longValue(), credentials);
            String hex = Numeric.toHexString(signed);
            EthSendTransaction resp = web3j.ethSendRawTransaction(hex).send();
            if (resp.hasError()) {
                throw new BlockchainException("sendRawTransaction failed: " + resp.getError().getMessage());
            }
            String txHash = resp.getTransactionHash();
            log.info("Submitted tx: from={} to={} nonce={} gasPrice={} gasLimit={} chainId={} txHash={}", credentials.getAddress(), to, nonce, gasPrice, gasLimit, chainId, txHash);
            return txHash;
        } catch (Exception e) {
            throw new BlockchainException("Failed to send raw transaction to " + to, e);
        }
    }

    // ---------- Read helpers ----------

    public BigDecimal getKrwtBalance(String walletAddress) {
        validateAddress(walletAddress, "wallet");
        Function fn = new Function(
                "balanceOf",
                Collections.singletonList(new Address(walletAddress)),
                Collections.singletonList(new TypeReference<Uint256>() {})
        );
        try {
            String encoded = FunctionEncoder.encode(fn);
            Transaction call = Transaction.createEthCallTransaction(credentials.getAddress(), krwtContractAddress, encoded);
            EthCall resp = web3j.ethCall(call, DefaultBlockParameterName.LATEST).send();
            List<Type> decoded = FunctionReturnDecoder.decode(resp.getValue(), fn.getOutputParameters());
            if (decoded.isEmpty()) throw new BlockchainException("Empty balanceOf response");
            return new BigDecimal((BigInteger) decoded.get(0).getValue());
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch KRWT balance for " + walletAddress, e);
        }
    }

    public BigDecimal getTokenBalance(String contractAddress, String walletAddress) {
        validateAddress(contractAddress, "contract");
        validateAddress(walletAddress, "wallet");
        Function fn = new Function(
                "balanceOf",
                Collections.singletonList(new Address(walletAddress)),
                Collections.singletonList(new TypeReference<Uint256>() {})
        );
        try {
            String encoded = FunctionEncoder.encode(fn);
            Transaction call = Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, encoded);
            EthCall resp = web3j.ethCall(call, DefaultBlockParameterName.LATEST).send();
            List<Type> decoded = FunctionReturnDecoder.decode(resp.getValue(), fn.getOutputParameters());
            if (decoded.isEmpty()) throw new BlockchainException("Empty balanceOf response");
            return new BigDecimal((BigInteger) decoded.get(0).getValue());
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch token balance for contract: " + contractAddress, e);
        }
    }

    // ---------- Write operations ----------

    public String transferKrwt(String toAddress, long amount) {
        validateAddress(toAddress, "receiver");
        if (amount <= 0) throw new IllegalArgumentException("Transfer amount must be positive");
        Function fn = new Function(
                "transfer",
                Arrays.asList(new Address(toAddress), new Uint256(BigInteger.valueOf(amount))),
                Collections.emptyList()
        );
        String data = FunctionEncoder.encode(fn);
        return sendRawTransaction(krwtContractAddress, data, BigInteger.ZERO);
    }

    // buyer → seller KRWT 전송 (관리자(owner)가 on-chain 강제 이체 수행)
    public String transferKrwtFrom(String fromAddress, String toAddress, long amount) {
        validateAddress(fromAddress, "from");
        validateAddress(toAddress, "to");
        if (amount <= 0) throw new IllegalArgumentException("Transfer amount must be positive");

        Function fn = new Function(
                "forceTransfer",  // ← 여기!
                Arrays.asList(
                        new Address(fromAddress),
                        new Address(toAddress),
                        new Uint256(BigInteger.valueOf(amount))
                ),
                Collections.emptyList()
        );

        String data = FunctionEncoder.encode(fn);
        return sendRawTransaction(this.krwtContractAddress, data, BigInteger.ZERO);
    }

    public String transferToken(String contractAddress, String toAddress, Long amount) {
        validateAddress(contractAddress, "contract");
        validateAddress(toAddress, "receiver");
        if (amount == null || amount <= 0) throw new IllegalArgumentException("Transfer amount must be greater than 0");
        Function fn = new Function(
                "transfer",
                Arrays.asList(new Address(toAddress), new Uint256(BigInteger.valueOf(amount))),
                Collections.emptyList()
        );
        String data = FunctionEncoder.encode(fn);
        return sendRawTransaction(contractAddress, data, BigInteger.ZERO);
    }

    // seller → buyer 토큰 전송 (관리자(owner)가 on-chain 강제 이체 수행)
    public String transferTokenFrom(String contractAddress, String fromAddress, String toAddress, Long amount) {
        validateAddress(contractAddress, "contract");
        validateAddress(fromAddress, "from");
        validateAddress(toAddress, "to");
        if (amount == null || amount <= 0) throw new IllegalArgumentException("Transfer amount must be greater than 0");

        Function fn = new Function(
                "forceTransfer",  // ← 여기!
                Arrays.asList(
                        new Address(fromAddress),
                        new Address(toAddress),
                        new Uint256(BigInteger.valueOf(amount))
                ),
                Collections.emptyList()
        );

        String data = FunctionEncoder.encode(fn);
        return sendRawTransaction(contractAddress, data, BigInteger.ZERO);
    }


    public String addToWhitelist(String contractAddress, String userWalletAddress) {
        validateAddress(contractAddress, "contract");
        validateAddress(userWalletAddress, "user");
        Function fn = new Function(
                "addToWhitelist",
                Collections.singletonList(new Address(userWalletAddress)),
                Collections.emptyList()
        );
        String data = FunctionEncoder.encode(fn);
        return sendRawTransaction(contractAddress, data, BigInteger.ZERO);
    }

    // ---------- TokenFactory integration ----------

    @Getter
    @RequiredArgsConstructor
    public static class CreateTokenResult {
        private final String tokenAddress;
        private final String dividendAddress;
        private final String txHash;
        private final BigInteger blockNumber;
    }

    public CreateTokenResult createTokenViaFactory(String name, String symbol, BigInteger totalSupply) {
        try {
            if (!StringUtils.hasText(name) || !StringUtils.hasText(symbol)) {
                throw new BlockchainException("토큰 이름/심볼은 비어 있을 수 없습니다.");
            }

            if (totalSupply == null || totalSupply.compareTo(BigInteger.ZERO) <= 0) {
                throw new BlockchainException("totalSupply는 0보다 커야 합니다.");
            }

            final String factoryAddress = this.tokenFactoryAddress;
            final String initialOwner = this.adminAddress;

            if (!StringUtils.hasText(factoryAddress)) {
                throw new BlockchainException("TokenFactory 주소가 설정되지 않았습니다.");
            }

            Function function = new Function(
                    "createToken",
                    Arrays.asList(
                            new Utf8String(name),
                            new Utf8String(symbol),
                            new Uint256(totalSupply),
                            new Address(initialOwner)
                    ),
                    Collections.emptyList()
            );

            String data = FunctionEncoder.encode(function);
            BigInteger value = BigInteger.ZERO;

            BigInteger gasLimit = estimateGas(factoryAddress, data, value);

            log.info("Calling TokenFactory.createToken(name={}, symbol={}, totalSupply={}, owner={}, gasLimit={})",
                    name, symbol, totalSupply, initialOwner, gasLimit);

            String txHash = sendRawTransaction(
                    factoryAddress,
                    data,
                    value,
                    gasLimit
            );

            log.info("createToken tx sent: txHash={}", txHash);

            TransactionReceipt receipt = waitForTransactionReceipt(txHash);
            if (receipt == null) {
                throw new BlockchainException("Transaction receipt not found for tx " + txHash);
            }
            if ("0x0".equalsIgnoreCase(receipt.getStatus())) {
                throw new BlockchainException("TokenFactory transaction reverted: " + txHash);
            }

            log.info("createToken tx mined: txHash={}, blockNumber={}", txHash, receipt.getBlockNumber());

            String tokenAddress = null;
            String dividendAddress = null;
            String expectedTopic0 = EventEncoder.encode(TOKEN_CREATED_EVENT);

            for (org.web3j.protocol.core.methods.response.Log logEntry : receipt.getLogs()) {
                List<String> topics = logEntry.getTopics();
                if (topics == null || topics.isEmpty()) {
                    continue;
                }

                if (!expectedTopic0.equalsIgnoreCase(topics.get(0))) {
                    continue;
                }

                String nameTopicHash = (topics.size() > 1) ? topics.get(1) : null;

                List<Type> decoded = FunctionReturnDecoder.decode(
                        logEntry.getData(),
                        TOKEN_CREATED_EVENT.getNonIndexedParameters()
                );

                if (decoded.size() != 4) {
                    log.warn("TokenCreated event found but non-indexed param size={} (expected 4). tx={}",
                            decoded.size(), txHash);
                    continue;
                }

                Utf8String evSymbol        = (Utf8String) decoded.get(0);
                Address    evTokenAddress  = (Address) decoded.get(1);
                Address    evDividendAddress = (Address) decoded.get(2);
                Uint256    evTotalSupply   = (Uint256) decoded.get(3);

                tokenAddress = evTokenAddress.getValue();
                dividendAddress = evDividendAddress.getValue();

                log.info(
                        "TokenCreated event decoded: name={}, symbol={}, tokenAddress={}, dividendAddress={}, totalSupply={}, txHash={}",
                        nameTopicHash,
                        evSymbol.getValue(),
                        tokenAddress,
                        dividendAddress,
                        evTotalSupply.getValue(),
                        txHash
                );
                break;
            }

            if (!StringUtils.hasText(tokenAddress) || !StringUtils.hasText(dividendAddress)) {
                throw new BlockchainException("TokenCreated 이벤트를 찾을 수 없습니다. tx=" + txHash);
            }

            return new CreateTokenResult(
                    tokenAddress,
                    dividendAddress,
                    receipt.getTransactionHash(),
                    receipt.getBlockNumber()
            );
        } catch (BlockchainException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating token via factory", e);
            throw new BlockchainException("토큰 생성 중 알 수 없는 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private String decodeAddressTopic(String topic) {
        if (!StringUtils.hasText(topic)) return null;
        // topic 형식: 0x + 64 hex, 마지막 40자리가 주소
        String hex = topic.startsWith("0x") ? topic.substring(2) : topic;
        if (hex.length() < 40) return null;
        String addr = hex.substring(hex.length() - 40);
        return "0x" + addr.toLowerCase();
    }

    private BigInteger getLatestTokenIndex() {
        try {
            Function fn = new Function(
                    "getTokenCount",
                    Collections.emptyList(),
                    Collections.singletonList(new TypeReference<Uint256>() {})
            );
            String encoded = FunctionEncoder.encode(fn);
            Transaction call = Transaction.createEthCallTransaction(credentials.getAddress(), tokenFactoryAddress, encoded);
            EthCall resp = web3j.ethCall(call, DefaultBlockParameterName.LATEST).send();
            List<Type> decoded = FunctionReturnDecoder.decode(resp.getValue(), fn.getOutputParameters());
            if (decoded.isEmpty()) return BigInteger.valueOf(-1);
            BigInteger count = (BigInteger) decoded.get(0).getValue();
            return count.subtract(BigInteger.ONE);
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch token count from factory", e);
        }
    }

    private TokenRecord getTokenRecord(BigInteger index) {
        try {
            Function fn = new Function(
                "tokens",
                Collections.singletonList(new Uint256(index)),
                Arrays.asList(
                        new TypeReference<Utf8String>() {}, // name
                        new TypeReference<Utf8String>() {}, // symbol
                        new TypeReference<Address>() {},    // tokenAddress
                        new TypeReference<Address>() {},    // dividendAddress
                        new TypeReference<Uint256>() {}     // createdAt
                )
            );
            String encoded = FunctionEncoder.encode(fn);
            Transaction call = Transaction.createEthCallTransaction(credentials.getAddress(), tokenFactoryAddress, encoded);
            EthCall resp = web3j.ethCall(call, DefaultBlockParameterName.LATEST).send();
            List<Type> decoded = FunctionReturnDecoder.decode(resp.getValue(), fn.getOutputParameters());
            if (decoded.size() < 4) throw new BlockchainException("Failed to decode token record");
            String tokenAddr = ((Address) decoded.get(2)).getValue();
            String dividendAddr = ((Address) decoded.get(3)).getValue();
            return new TokenRecord(tokenAddr, dividendAddr);
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch token record for index " + index, e);
        }
    }

    private record TokenRecord(String tokenAddress, String dividendAddress) {}

    // ---------- Receipt ----------

    public Map<String, Object> getTransactionReceipt(String transactionHash) {
        validateHash(transactionHash);
        try {
            EthGetTransactionReceipt res = web3j.ethGetTransactionReceipt(transactionHash).send();
            if (res.hasError()) {
                throw new BlockchainException("Error fetching transaction receipt: " + res.getError().getMessage());
            }
            Optional<TransactionReceipt> opt = res.getTransactionReceipt();
            if (opt.isEmpty()) {
                Map<String, Object> pending = new HashMap<>();
                pending.put("hash", transactionHash);
                pending.put("status", "PENDING");
                pending.put("blockNumber", null);
                pending.put("blockHash", null);
                pending.put("from", null);
                pending.put("to", null);
                pending.put("value", "0");
                pending.put("gasUsed", null);
                pending.put("gasPrice", null);
                pending.put("timestamp", null);
                pending.put("logs", Collections.emptyList());
                return pending;
            }
            TransactionReceipt receipt = opt.get();
            OffsetDateTime ts = null;
            try {
                EthBlock block = web3j.ethGetBlockByHash(receipt.getBlockHash(), false).send();
                if (block.getBlock() != null && block.getBlock().getTimestamp() != null) {
                    ts = OffsetDateTime.ofInstant(
                            java.time.Instant.ofEpochSecond(block.getBlock().getTimestamp().longValue()),
                            java.time.ZoneOffset.UTC
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to fetch block timestamp for {}: {}", receipt.getBlockHash(), e.getMessage());
            }

            List<Map<String, Object>> logs = receipt.getLogs().stream().map(log -> {
                Map<String, Object> m = new HashMap<>();
                m.put("address", log.getAddress());
                m.put("topics", log.getTopics() != null ? log.getTopics() : List.of());
                m.put("data", log.getData() != null ? log.getData() : "");
                m.put("blockNumber", log.getBlockNumber() != null ? log.getBlockNumber().toString() : null);
                m.put("transactionHash", log.getTransactionHash());
                m.put("logIndex", log.getLogIndex() != null ? log.getLogIndex().toString() : null);
                return m;
            }).toList();

            String status = "unknown";
            if ("0x1".equalsIgnoreCase(receipt.getStatus())) status = "success";
            if ("0x0".equalsIgnoreCase(receipt.getStatus())) status = "failed";

            Map<String, Object> result = new HashMap<>();
            result.put("hash", transactionHash);
            result.put("status", status);
            result.put("blockHash", receipt.getBlockHash());
            result.put("blockNumber", receipt.getBlockNumber() != null ? receipt.getBlockNumber().longValue() : null);
            result.put("from", receipt.getFrom());
            result.put("to", receipt.getTo());
            result.put("value", BigInteger.ZERO.toString());
            result.put("gasUsed", receipt.getGasUsed() != null ? receipt.getGasUsed().toString() : "0");
            result.put("gasPrice", null);
            result.put("timestamp", ts != null ? ts.toString() : null);
            result.put("logs", logs);
            return result;
        } catch (Exception e) {
            throw new BlockchainException("Failed to get transaction receipt for " + transactionHash, e);
        }
    }

    // ---------- Misc ----------

    public String getKrwtContractAddress() {
        return krwtContractAddress;
    }

    public String getKrwtContractOwner() {
        Function fn = new Function(
                "owner",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Address>() {})
        );
        try {
            String encoded = FunctionEncoder.encode(fn);
            Transaction call = Transaction.createEthCallTransaction(credentials.getAddress(), krwtContractAddress, encoded);
            EthCall resp = web3j.ethCall(call, DefaultBlockParameterName.LATEST).send();
            List<Type> decoded = FunctionReturnDecoder.decode(resp.getValue(), fn.getOutputParameters());
            if (decoded.isEmpty()) throw new BlockchainException("owner() empty response");
            return decoded.get(0).getValue().toString();
        } catch (Exception e) {
            throw new BlockchainException("Failed to get KRWT owner", e);
        }
    }

    /**
     * 간단한 컨트랙트 정보 조회 (프론트 계약 유지용)
     */
    public Map<String, Object> getContractInfo() {
        Map<String, Object> krwt = Map.of(
                "address", krwtContractAddress,
                "name", "Korean Won Token",
                "symbol", "KRWT",
                "decimals", 0,
                "owner", credentials.getAddress()
        );
        Map<String, Object> factory = Map.of(
                "address", StringUtils.hasText(tokenFactoryAddress) ? tokenFactoryAddress : "",
                "tokensCreated", 0,
                "owner", credentials.getAddress()
        );
        return Map.of(
                "krwt", krwt,
                "tokenFactory", factory,
                "tokens", List.of()
        );
    }

    public long getLatestBlockNumber() {
        try {
            return web3j.ethBlockNumber().send().getBlockNumber().longValue();
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch latest block number", e);
        }
    }

    public boolean isSyncing() {
        try {
            return web3j.ethSyncing().send().isSyncing();
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch syncing status", e);
        }
    }

    public String getChainIdHex() {
        return "0x" + chainId.toString(16);
    }

    // --- Compatibility methods used by admin status ---
    public String getChainId() {
        return getChainIdHex();
    }

    public String getNetworkId() {
        try {
            return web3j.netVersion().send().getNetVersion();
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch network id", e);
        }
    }

    public int getPeerCount() {
        try {
            return web3j.netPeerCount().send().getQuantity().intValue();
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch peer count", e);
        }
    }

    public long getGasPrice() {
        try {
            return web3j.ethGasPrice().send().getGasPrice().longValue();
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch gas price", e);
        }
    }

    public LatestBlockSummary getLatestBlockSummary() {
        try {
            EthBlock.Block block = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock();
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

    // -------- KRWT mint/burn ----------
    public String mintKrwt(String toAddress, BigInteger amount) {
        validateAddress(toAddress, "to");
        if (amount == null || amount.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("mint amount must be > 0");
        }
        Function fn = new Function(
                "mint",
                Arrays.asList(new Address(toAddress), new Uint256(amount)),
                Collections.emptyList()
        );
        String data = FunctionEncoder.encode(fn);
        return sendRawTransaction(krwtContractAddress, data, BigInteger.ZERO);
    }

    public String burnKrwt(String fromAddress, BigInteger amount) {
        validateAddress(fromAddress, "from");
        if (amount == null || amount.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("burn amount must be > 0");
        }
        Function fn = new Function(
                "burn",
                Arrays.asList(new Address(fromAddress), new Uint256(amount)),
                Collections.emptyList()
        );
        String data = FunctionEncoder.encode(fn);
        return sendRawTransaction(krwtContractAddress, data, BigInteger.ZERO);
    }

    // -------- Whitelist view ----------
    public boolean isWhitelisted(String contractAddress, String userWalletAddress) {
        validateAddress(contractAddress, "contract");
        validateAddress(userWalletAddress, "user");
        Function fn = new Function(
                "whitelist",
                Collections.singletonList(new Address(userWalletAddress)),
                Collections.singletonList(new TypeReference<Bool>() {})
        );
        try {
            String encoded = FunctionEncoder.encode(fn);
            Transaction call = Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, encoded);
            EthCall resp = web3j.ethCall(call, DefaultBlockParameterName.LATEST).send();
            List<Type> decoded = FunctionReturnDecoder.decode(resp.getValue(), fn.getOutputParameters());
            if (decoded.isEmpty()) return false;
            return ((Bool) decoded.get(0)).getValue();
        } catch (Exception e) {
            log.warn("isWhitelisted call failed: {}", e.getMessage());
            return false;
        }
    }

    private TransactionReceipt waitForTransactionReceipt(String txHash) {
        int attempts = 0;
        int maxAttempts = 120; // allow up to ~2 minutes
        long sleepMs = 1000;
        while (attempts < maxAttempts) {
            try {
                EthGetTransactionReceipt res = web3j.ethGetTransactionReceipt(txHash).send();
                if (res.getTransactionReceipt().isPresent()) return res.getTransactionReceipt().get();
            } catch (Exception e) {
                log.warn("waitForReceipt error for {}: {}", txHash, e.getMessage());
            }
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BlockchainException("Interrupted while waiting for receipt", e);
            }
            attempts++;
        }
        throw new BlockchainException("Transaction not mined within timeout: " + txHash);
    }

    public Optional<TransactionReceipt> fetchTransactionReceipt(String txHash) {
        validateHash(txHash);
        try {
            EthGetTransactionReceipt res = web3j.ethGetTransactionReceipt(txHash).send();
            return res.getTransactionReceipt();
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch receipt for tx: " + txHash, e);
        }
    }

    // 외부 서비스에서 receipt 대기 필요 시 사용
    public TransactionReceipt waitForReceipt(String txHash) {
        return waitForTransactionReceipt(txHash);
    }

    private void validateAddress(String addr, String label) {
        if (!StringUtils.hasText(addr) || !addr.matches("^0x[0-9a-fA-F]{40}$")) {
            throw new IllegalArgumentException("Invalid " + label + " address: " + addr);
        }
    }

    private void validateHash(String hash) {
        if (!StringUtils.hasText(hash) || !hash.matches("^0x[0-9a-fA-F]{64}$")) {
            throw new IllegalArgumentException("Invalid transaction hash: " + hash);
        }
    }
}

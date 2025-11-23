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
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

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

            // 4. KRWT는 소수점 18자리를 사용한다고 가정하고, 실제 값으로 변환
            return Convert.fromWei(new BigDecimal(balanceInWei), Convert.Unit.ETHER);

        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch KRWT balance for wallet: " + walletAddress, e);
        }
    }

    /**
     * Adds a user's wallet address to the whitelist of a specific token contract.
     * This is a placeholder method.
     * @param contractAddress The address of the token smart contract.
     * @param userWalletAddress The wallet address of the user to be whitelisted.
     */
    public void addToWhitelist(String contractAddress, String userWalletAddress) {
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
     * Retrieves a transaction receipt by its hash.
     * This is a placeholder method.
     * @param transactionHash The hash of the transaction.
     * @return A dummy transaction receipt.
     */
    public Map<String, Object> getTransactionReceipt(String transactionHash) {
        if (!StringUtils.hasText(transactionHash) || !transactionHash.matches("^0x[0-9a-fA-F]{64}$")) {
            throw new IllegalArgumentException("Invalid transaction hash: " + transactionHash);
        }
        log.info("[MOCK] Getting transaction receipt for hash {}", transactionHash);
        // In a real implementation, this would query the blockchain for the transaction receipt.
        return Map.of(
                "hash", transactionHash,
                "status", "success",
                "blockNumber", 12345L,
                "from", randomAddress(),
                "to", randomAddress(),
                "value", "100",
                "gasUsed", "21000",
                "timestamp", OffsetDateTime.now().toString()
        );
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

    /**
     * Returns the wallet address of the admin user.
     * @return The admin's wallet address.
     */
    public String getAdminAddress() {
        return credentials.getAddress();
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
    private String randomHash() {
        String hex = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        return "0x" + hex.substring(0, 64);
    }

    // Helper method to generate a dummy Ethereum address (0x + 40 hex chars)
    private String randomAddress() {
        String hex = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        return "0x" + hex.substring(0, 40);
    }
}
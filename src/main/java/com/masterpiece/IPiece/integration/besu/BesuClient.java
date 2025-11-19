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
import java.util.Collections;
import java.util.List;

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
}

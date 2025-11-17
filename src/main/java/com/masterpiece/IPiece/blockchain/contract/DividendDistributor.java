package com.masterpiece.IPiece.blockchain.contract;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

/**
 * Placeholder for Web3j-generated contract wrapper.
 * This class allows the service layer to compile before the actual wrapper is generated.
 */
public class DividendDistributor extends Contract {

    protected DividendDistributor(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        super("", contractAddress, web3j, credentials, gasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> distribute(BigInteger totalAmount) {
        final Function function = new Function(
                "distribute",
                Arrays.asList(new Uint256(totalAmount)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public static DividendDistributor load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return new DividendDistributor(contractAddress, web3j, credentials, gasProvider);
    }
}

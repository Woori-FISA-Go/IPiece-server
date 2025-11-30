package com.masterpiece.IPiece.admin.blockchain.application;

import com.masterpiece.IPiece.admin.blockchain.api.dto.response.AdminBlockchainStatusResponse;
import com.masterpiece.IPiece.admin.blockchain.api.dto.response.AdminBlockchainStatusResponse.LatestBlock;
import com.masterpiece.IPiece.integration.besu.BesuClient;
import com.masterpiece.IPiece.integration.besu.BesuClient.LatestBlockSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminBlockchainStatusService {

    private final BesuClient besuClient;

    public AdminBlockchainStatusResponse getStatus() {
        String chainId = besuClient.getChainId();
        String networkId = besuClient.getNetworkId();
        long latestBlockNumber = besuClient.getLatestBlockNumber();
        int peerCount = besuClient.getPeerCount();
        boolean syncing = besuClient.isSyncing();
        long gasPrice = besuClient.getGasPrice();
        LatestBlockSummary blockSummary = besuClient.getLatestBlockSummary();

        boolean healthy =
                latestBlockNumber > 0 &&
                        peerCount > 0 &&
                        !syncing;

        LatestBlock latestBlockDto = LatestBlock.builder()
                .number(blockSummary.number())
                .gasUsed(blockSummary.gasUsed())
                .gasLimit(blockSummary.gasLimit())
                .txCount(blockSummary.txCount())
                .build();

        return AdminBlockchainStatusResponse.builder()
                .chainId(chainId)
                .networkId(networkId)
                .syncing(syncing)
                .latestBlockNumber(latestBlockNumber)
                .peerCount(peerCount)
                .gasPrice(gasPrice)
                .latestBlock(latestBlockDto)
                .healthy(healthy)
                .build();
    }
}

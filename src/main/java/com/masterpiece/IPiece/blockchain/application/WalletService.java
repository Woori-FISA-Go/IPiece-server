package com.masterpiece.IPiece.blockchain.application;

import com.masterpiece.IPiece.blockchain.api.dto.response.MyWalletResponse;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountRepository;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import com.masterpiece.IPiece.mypage.infra.HoldingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletService {

    private final VirtualAccountRepository virtualAccountRepository;
    private final HoldingsRepository holdingsRepository;

    public MyWalletResponse getMyWallet(Long userId) {
        VirtualAccount virtualAccount = virtualAccountRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User " + userId + " has no virtual account"));

        List<Holdings> holdings = holdingsRepository.findAllByVirtualAccount(virtualAccount);

        List<MyWalletResponse.TokenInfo> tokenInfos = holdings.stream()
                .map(holding -> {
                    BigDecimal balance = new BigDecimal(holding.getQuantity());
                    BigDecimal totalSupply = new BigDecimal(holding.getProduct().getTotalTokenQuantity());
                    BigDecimal sharePercentage = totalSupply.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                            balance.divide(totalSupply, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));

                    return MyWalletResponse.TokenInfo.builder()
                            .projectId(holding.getProduct().getProductId())
                            .projectName(holding.getProduct().getProductName())
                            .tokenAddress(null) // Assuming token address is not available
                            .symbol(holding.getProduct().getTokenName())
                            .balance(holding.getQuantity())
                            .totalSupply(holding.getProduct().getTotalTokenQuantity())
                            .sharePercentage(sharePercentage.toPlainString() + "%")
                            .totalDividendsReceived(0L) // Assuming total dividends received is not available
                            .build();
                })
                .collect(Collectors.toList());

        long totalHoldingsValue = holdings.stream()
                .mapToLong(holding -> holding.getQuantity() * holding.getProduct().getCurrentPrice())
                .sum();

        long totalValueKrw = totalHoldingsValue + virtualAccount.getBalanceKrw();

        return MyWalletResponse.builder()
                .walletAddress(virtualAccount.getWalletAddress())
                .balanceKrw(virtualAccount.getBalanceKrw())
                .createdAt(virtualAccount.getCreatedAt())
                .tokens(tokenInfos)
                .totalValueKrw(totalValueKrw)
                .build();
    }
}

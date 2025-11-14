package com.masterpiece.IPiece.mypage.application;

import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountRepository;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.mypage.api.dto.AssetDto;
import com.masterpiece.IPiece.mypage.api.dto.response.MyhomeResponse;
import com.masterpiece.IPiece.mypage.application.mapper.MypageMapper;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import com.masterpiece.IPiece.mypage.infra.HoldingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageService {

    private final VirtualAccountRepository virtualAccountRepository;
    private final HoldingsRepository holdingsRepository;
    private final MypageMapper mypageMapper;

    private static final int PAGE_SIZE = 10;

    /**
     * 마이홈 조회 (보유자산 페이징)
     */
    public MyhomeResponse getMyHome(Long userId, int page) {
        // 1. 가상계좌 조회
        VirtualAccount account = virtualAccountRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));


        // 2. 전체 보유자산 조회
        List<Holdings> allHoldings = holdingsRepository.findAllByVirtualAccount(account);


        // 3. Holdings → AssetDto 변환
        List<AssetDto> allAssets = mypageMapper.toMergedAssetDtos(allHoldings);

        // 4. 페이징 처리 (1-based → 0-based 변환)
        int pageIndex = page - 1;
        int start = pageIndex * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allAssets.size());

        List<AssetDto> pagedAssets = (start < allAssets.size())
                ? allAssets.subList(start, end)
                : List.of();

        // 5. MyhomeResponse 생성
        return mypageMapper.toMyhomeResponse(userId, account, allHoldings, allAssets, pagedAssets);
    }
}
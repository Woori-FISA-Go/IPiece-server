package com.masterpiece.IPiece.blockchain.api.dto.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class TokenListResponse {
    private final List<TokenInfoResponse> tokens;
    private final int totalPages;
    private final long totalElements;
    private final int currentPage;
    private final int pageSize;

    public TokenListResponse(Page<TokenInfoResponse> tokenPage) {
        this.tokens = tokenPage.getContent();
        this.totalPages = tokenPage.getTotalPages();
        this.totalElements = tokenPage.getTotalElements();
        this.currentPage = tokenPage.getNumber();
        this.pageSize = tokenPage.getSize();
    }
}

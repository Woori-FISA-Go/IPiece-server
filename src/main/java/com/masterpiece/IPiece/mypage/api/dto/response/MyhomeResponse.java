package com.masterpiece.IPiece.mypage.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.masterpiece.IPiece.mypage.api.dto.AssetDto;
import com.masterpiece.IPiece.mypage.api.dto.PortfolioRatioDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyhomeResponse {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_made_id")
    private String userMadeId;

    @JsonProperty("total_krw")
    private Long totalKrw;

    @JsonProperty("total_assets")
    private Long totalAssets;

    @JsonProperty("total_buy_amount")
    private Long totalBuyAmount;

    @JsonProperty("total_evaluation")
    private Long totalEvaluation;

    @JsonProperty("total_profit")
    private Long totalProfit;

    @JsonProperty("total_profit_rate")
    private Double totalProfitRate;

    @JsonProperty("available_krw")
    private Long availableKrw;

    @JsonProperty("holding_count")
    private Integer holdingCount;

    @JsonProperty("portfolio_ratio")
    private List<PortfolioRatioDto> portfolioRatio;

    @JsonProperty("asset_list")
    private List<AssetDto> assetList;

    @JsonProperty("offering_list")
    private List<OfferingAssetDto> offeringList;

    @JsonProperty("offering_total_count")
    private Integer offeringTotalCount;

    @JsonProperty("offering_has_next")
    private boolean offeringHasNext;

    @JsonProperty("offering_next_page")
    private Integer offeringNextPage;
}
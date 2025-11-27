package com.masterpiece.IPiece.admin.offeringandtrade.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminCreateProductRequest {

    @JsonProperty("product_name")
    @NotBlank
    private String productName;

    @JsonProperty("project_name")
    @NotBlank
    private String projectName;

    @JsonProperty("owner")
    @NotBlank
    private String owner;

    @JsonProperty("issue_amount")
    @NotNull
    @Min(1)
    private Long issueAmount;

    @JsonProperty("issue_date")
    @NotBlank
    private String issueDate; // OffsetDateTime.parse 가능한 문자열 기대

    @JsonProperty("token_name")
    @NotBlank
    private String tokenName;

    @JsonProperty("token_symbol")
    @NotBlank
    private String tokenSymbol;

    @JsonProperty("token_contract_address")
    @NotBlank
    private String tokenContractAddress;

    @JsonProperty("token_quantity")
    @NotNull
    @Min(1)
    private Long tokenQuantity;

    @JsonProperty("dividend_ratio")
    @NotNull
    private Double dividendRatio;

/*    @JsonProperty("present_img")
    @NotBlank
    private String presentImg;

    @JsonProperty("thumbnail_img")
    @NotBlank
    private String thumbnailImg;*/

    @JsonProperty("offering")
    @Valid
    @NotNull
    private AdminCreateProductOfferingRequest offering;
}
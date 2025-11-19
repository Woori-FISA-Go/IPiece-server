package com.masterpiece.IPiece.admin.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminCreateProductOfferingRequest {

    @JsonProperty("offering_amount")
    @NotNull
    @Min(1)
    private Long offeringAmount;

    @JsonProperty("offering_price")
    @NotNull
    @Min(1)
    private Long offeringPrice;

    @JsonProperty("offering_start_date")
    @NotBlank
    private String offeringStartDate; // "2025-12-01T00:00:00+09:00" 같은 ISO 문자열 기대

    @JsonProperty("offering_end_date")
    @NotBlank
    private String offeringEndDate;

    @JsonProperty("detail_img")
    @NotBlank
    private String detailImg;
}
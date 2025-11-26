package com.masterpiece.IPiece.investment.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor; // Add this import
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Add this annotation
@Builder
public class InvestmentRequest {

    @NotNull
    @JsonProperty("project_id")
    private Long projectId;

    @NotNull
    @Min(1)
    @JsonProperty("amount")
    private Integer amount;

    @NotNull
    @Min(1)
    @JsonProperty("token_amount")
    private Integer tokenAmount;
}

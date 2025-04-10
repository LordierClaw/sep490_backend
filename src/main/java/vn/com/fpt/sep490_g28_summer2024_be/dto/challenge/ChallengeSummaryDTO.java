package vn.com.fpt.sep490_g28_summer2024_be.dto.challenge;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChallengeSummaryDTO {

    private List<ChallengeResponseDTO> challenges;
    @JsonProperty("total")
    private BigDecimal totalDonations;
}

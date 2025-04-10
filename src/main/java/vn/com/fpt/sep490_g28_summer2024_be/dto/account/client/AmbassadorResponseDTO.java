package vn.com.fpt.sep490_g28_summer2024_be.dto.account.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AmbassadorResponseDTO {
    private BigInteger accountId;
    private String code;
    private String fullname;
    private String avatar;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("total_donation")
    private BigDecimal totalDonation;
    @JsonProperty("total_challenges")
    private Long countChallenges;
}

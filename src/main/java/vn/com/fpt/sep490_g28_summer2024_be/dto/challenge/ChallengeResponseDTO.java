package vn.com.fpt.sep490_g28_summer2024_be.dto.challenge;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChallengeResponseDTO {

    @JsonProperty("challenge_id")
    private BigInteger challengeId;

    @JsonProperty("challenge_code")
    private String challengeCode;

    private String title;
    private String slug;

    @JsonProperty("thumbnail")
    String thumbnail;

    private String content;

    private BigDecimal goal;

    @JsonProperty("created_by")
    private AccountDTO createdBy;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("finished_at")
    @FutureOrPresent(message = "Ngày kết thúc là ngày hợp lệ")
    private LocalDate finishedAt;

    @JsonProperty("projects")
    List<ProjectResponseDTO> projectResponseDTOS;

    @JsonProperty("total_donation")
    private BigDecimal totalDonation;
}

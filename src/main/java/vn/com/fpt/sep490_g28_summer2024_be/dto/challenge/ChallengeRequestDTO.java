package vn.com.fpt.sep490_g28_summer2024_be.dto.challenge;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectRequestDTO;

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
public class ChallengeRequestDTO {
    @JsonProperty("challenge_id")
    private BigInteger challengeId;

    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    private String title;

    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    private String thumbnail;

    private String content;

    @JsonProperty("projects")
    private List<ProjectRequestDTO> projects;

    @JsonProperty("finished_at")
    private LocalDate finishedAt;

    @Min(value = 0, message = "Không được nhập số âm")
    private BigDecimal goal;
}

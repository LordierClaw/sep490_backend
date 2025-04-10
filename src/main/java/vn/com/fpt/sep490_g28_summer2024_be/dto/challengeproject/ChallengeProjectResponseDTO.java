package vn.com.fpt.sep490_g28_summer2024_be.dto.challengeproject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.ChallengeResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChallengeProjectResponseDTO {
    @JsonProperty("challenge_project_id")
    private BigInteger challengeProjectId;

    @JsonProperty("project")
    private ProjectResponseDTO project;

    @JsonProperty("challenge")
    private ChallengeResponseDTO challenge;
}

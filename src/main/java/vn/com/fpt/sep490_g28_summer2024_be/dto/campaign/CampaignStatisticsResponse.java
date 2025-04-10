package vn.com.fpt.sep490_g28_summer2024_be.dto.campaign;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectByStatusAndCampaignResponseDTO;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CampaignStatisticsResponse {
    private List<ProjectByStatusAndCampaignResponseDTO> data;
    @JsonProperty("total")
    private Long totalProjects;
}

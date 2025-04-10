package vn.com.fpt.sep490_g28_summer2024_be.dto.campaign;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;

import java.math.BigInteger;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CampaignProjectsDTO {
    @JsonProperty("campaign_id")
    BigInteger campaignId;
    @JsonProperty("title")
    @Length(max = 255, message = "Tiêu đề không được quá 255 ký tự")
    String title;
    @JsonProperty("projects")
    List<ProjectResponseDTO> projects;
}

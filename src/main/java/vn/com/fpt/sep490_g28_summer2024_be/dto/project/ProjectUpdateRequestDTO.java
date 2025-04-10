package vn.com.fpt.sep490_g28_summer2024_be.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.construction.ConstructionUpdateRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.file.ProjectImageDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.file.RelatedFileDTO;

import java.math.BigInteger;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectUpdateRequestDTO {
    @JsonProperty("project_id")
    private BigInteger projectId;

    @Length(max = 300, message = "Title không được vượt quá 300 ký tự")
    private String title;

    private String background;

    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    private String address;

    @NotEmpty(message = "Không được để trốn trường xã")
    @Length(max = 100, message = "Không được vượt quá 100 ký tự")
    private String ward;

    @NotEmpty(message = "Không được để trống trường huyện")
    @Length(max = 100, message = "Không được vượt quá 100 ký tự")
    private String district;

    @NotEmpty(message = "Không được để trống trường tỉnh")
    @Length(max = 100, message = "Không được vượt quá 100 ký tự")
    private String province;

    @NotEmpty(message = "Không được để trống")
    private List<ConstructionUpdateRequestDTO> constructions;

    @JsonProperty("total_budget")
    private String totalBudget;

    @JsonProperty("amount_needed_to_raise")
    private String amountNeededToRaise;

    @NotNull(message = "Không được để trống chiến dịch")
    @JsonProperty("campaign")
    private CampaignResponseDTO campaign;

    private List<ProjectImageDTO> images;
    private List<RelatedFileDTO> files;
}

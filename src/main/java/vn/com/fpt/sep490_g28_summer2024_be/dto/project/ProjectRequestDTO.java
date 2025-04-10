package vn.com.fpt.sep490_g28_summer2024_be.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import vn.com.fpt.sep490_g28_summer2024_be.dto.assign.AssignRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.budget.BudgetRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.construction.ConstructionRequestDTO;

import java.math.BigInteger;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectRequestDTO {
    @JsonProperty("project_id")
    private BigInteger projectId;

    @Length(max = 300, message = "Title không được vượt quá 300 ký tự")
    private String title;

    private String background;

    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    private String address;

    @Length(max = 100, message = "Không được vượt quá 100 ký tự")
    @NotEmpty(message = "Không được để trống")
    private String ward;

    @Length(max = 100, message = "Không được vượt quá 100 ký tự")
    @NotEmpty(message = "Không được để trống")
    private String district;

    @Length(max = 100, message = "Không được vượt quá 100 ký tự")
    @NotEmpty(message = "Không được để trống")
    private String province;

    @NotEmpty(message = "Không được để trống")
    private List<ConstructionRequestDTO> constructions;

    @JsonProperty("total_budget")
    private String totalBudget;

    @JsonProperty("amount_needed_to_raise")
    private String amountNeededToRaise;

    @NotNull(message = "Không được để trống chiến dịch")
    @JsonProperty("campaign")
    private CampaignResponseDTO campaign;

    @JsonProperty("assign")
    AssignRequestDTO assign;

    @JsonProperty("budgets")
    List<BudgetRequestDTO> budgets;
}

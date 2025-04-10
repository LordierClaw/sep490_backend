package vn.com.fpt.sep490_g28_summer2024_be.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.construction.ConstructionResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.donation.DonationResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.file.ProjectImageDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.file.RelatedFileDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectResponseDTO {
    @JsonProperty("project_id")
    private BigInteger projectId;
    private String title;
    private String slug;
    private String code;
    private String background;
    private String address;
    private String ward;
    private String district;
    private String province;
    private List<ConstructionResponseDTO> constructions;

    @JsonProperty("total_budget")
    private BigDecimal totalBudget;

    @JsonProperty("amount_needed_to_raise")
    private BigDecimal amountNeededToRaise;

    @JsonProperty("totalDonation")
    private BigDecimal totalDonation;

    @JsonProperty("number_sponsors")
    private Long numberSponsors;

    private Integer status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("campaign")
    private CampaignResponseDTO campaign;

    private List<ProjectImageDTO> images;

    private List<RelatedFileDTO> files;

    @JsonProperty("list_donate")
    private List<DonationResponseDTO> donationResponseDTOS;
}

package vn.com.fpt.sep490_g28_summer2024_be.dto.project;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectByStatusAndCampaignResponseDTO {
    private String title;
    private Long count;
    @JsonProperty("chua-co-ntt")
    private Long status1;

    @JsonProperty("can-quyen-gop")
    private Long status2;

    @JsonProperty("dang-thi-cong")
    private Long status3;

    @JsonProperty("hoan-thanh")
    private Long status4;
}

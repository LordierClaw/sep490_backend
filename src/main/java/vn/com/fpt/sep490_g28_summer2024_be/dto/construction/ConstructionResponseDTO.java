package vn.com.fpt.sep490_g28_summer2024_be.dto.construction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConstructionResponseDTO {
    @JsonProperty("construction_id")
    private BigInteger constructionId;
    private ProjectResponseDTO project;
    private String title;
    private Integer quantity;
    private String unit;
    private String note;
}

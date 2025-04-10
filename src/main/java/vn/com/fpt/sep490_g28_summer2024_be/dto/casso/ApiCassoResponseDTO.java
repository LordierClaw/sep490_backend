package vn.com.fpt.sep490_g28_summer2024_be.dto.casso;

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
public class ApiCassoResponseDTO {
    private Integer error;
    private String message;
    private CassoResponseDTO data;
}

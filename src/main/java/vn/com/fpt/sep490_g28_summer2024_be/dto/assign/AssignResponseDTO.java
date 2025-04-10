package vn.com.fpt.sep490_g28_summer2024_be.dto.assign;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignResponseDTO {
    @JsonProperty("assign_id")
    private BigInteger assignId;

    private AccountDTO accountDTO;

    private ProjectResponseDTO projectResponseDTO;

}

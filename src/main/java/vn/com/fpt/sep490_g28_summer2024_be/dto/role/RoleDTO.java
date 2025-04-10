package vn.com.fpt.sep490_g28_summer2024_be.dto.role;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDTO {
    @JsonProperty("role_id")
    BigInteger roleId;
    @JsonProperty("role_name")
    String roleName;
    @JsonProperty("role_description")
    String roleDescription;
    @JsonProperty("created_by")
    BigInteger createdBy;
    @JsonProperty("created_at")
    LocalDateTime createdAt;
    @JsonProperty("updated_by")
    BigInteger updatedBy;
    @JsonProperty("updated_at")
    LocalDateTime updatedAt;
    @JsonProperty("is_active")
    Boolean isActive;
}

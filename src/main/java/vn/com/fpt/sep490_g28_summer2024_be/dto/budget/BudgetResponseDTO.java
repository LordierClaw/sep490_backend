package vn.com.fpt.sep490_g28_summer2024_be.dto.budget;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetResponseDTO {
    @JsonProperty("budget_id")
    BigInteger budgetId;

    String title;

    @JsonProperty("unit_price")
    BigDecimal unitPrice;

    String note;

    Integer status;

    @JsonProperty("created_by")
    AccountDTO createdBy;

    @JsonProperty("created_at")
    LocalDateTime createdAt;

    @JsonProperty("updated_by")
    AccountDTO updatedBy;
    @JsonProperty("total_budget")
    BigDecimal totalBudget;

    @JsonProperty("updated_at")
    LocalDateTime updatedAt;
}

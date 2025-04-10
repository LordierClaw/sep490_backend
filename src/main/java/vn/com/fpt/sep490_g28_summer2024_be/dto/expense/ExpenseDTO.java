package vn.com.fpt.sep490_g28_summer2024_be.dto.expense;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpenseDTO {
    @JsonProperty("expense_id")
    private BigInteger expenseId;
    @JsonProperty("title")
    @Length(max = 255, message = "Tiêu đề không được quá 255 ký tự")
    private String title;
    @JsonProperty("unit_price")
    @NotNull(message = "Chi phí không được để trống")
    @DecimalMin(value = "0.00", message = "Chi phí phải lớn hơn hoặc bằng 0")
    private BigDecimal unitPrice;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    @JsonProperty("project_id")
    ProjectResponseDTO project;
    @JsonProperty("expense_files")
    private List<ExpenseFileDTO> expenseFiles;
}

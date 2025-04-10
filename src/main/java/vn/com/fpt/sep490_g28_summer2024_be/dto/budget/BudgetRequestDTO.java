package vn.com.fpt.sep490_g28_summer2024_be.dto.budget;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BudgetRequestDTO {

    @JsonProperty("budget_id")
    private BigInteger budgetId;


    @NotNull(message = "Không được để trống title!")
    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    private String title;

    @JsonProperty("unit_price")
    @NotNull(message = "Không được để trống giá trị!")
    private String unitPrice;

    @Length(max = 500, message = "Không được vượt quá 500 ký tự")
    private String note;
}

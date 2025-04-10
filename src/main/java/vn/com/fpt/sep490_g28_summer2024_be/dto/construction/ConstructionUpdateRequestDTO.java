package vn.com.fpt.sep490_g28_summer2024_be.dto.construction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConstructionUpdateRequestDTO {

    @JsonProperty("construction_id")
    private BigInteger constructionId;

    @NotEmpty
    @Length(max = 200, message = "Không được vượt quá 200 ký tự")
    private String title;

    @NotEmpty(message = "Không được để trống")
    @Min(value = 0, message = "Không được nhập giá trị âm")
    private Integer quantity;

    @NotEmpty(message = "Không được để trống")
    @Length(max = 20, message = "Không được vượt quá 20 ký tự")
    private String unit;

    @Length(max = 200, message = "Không được vượt quá 200 ký tự")
    private String note;
}

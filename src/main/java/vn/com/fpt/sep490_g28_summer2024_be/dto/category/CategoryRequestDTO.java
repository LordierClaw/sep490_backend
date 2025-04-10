package vn.com.fpt.sep490_g28_summer2024_be.dto.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryRequestDTO {

    @JsonProperty("category_id")
    BigInteger categoryId;

    @Length(min = 1, max = 100, message = "title phải từ 0 đến 100 ký tự")
    String title;

    @Length(min = 1, max = 255, message = "title phải từ 0 đến 255 ký tự")
    String description;

}

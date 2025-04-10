package vn.com.fpt.sep490_g28_summer2024_be.dto.news;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import vn.com.fpt.sep490_g28_summer2024_be.dto.category.CategoryResponseDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsDTO {
    @NotNull(message = "không được null")
    @Length(min = 1, max = 100, message = "title phải từ 0 đến 250 ký tự")
    String title;

    @Length(max = 500, message = "mô tả ngắn tối đa 500 ký tự")
    @JsonProperty("short_description")
    String short_description;

    @NotNull(message = "Nội dung là trường bắt buộc")
    @NotBlank(message = "Nội dung không được để trống")
    String content;

    @NotNull(message = "Không được bỏ trống phân loại tin tức")
    @JsonProperty("category")
    CategoryResponseDTO category;
}

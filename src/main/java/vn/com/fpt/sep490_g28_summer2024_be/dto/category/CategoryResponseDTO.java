package vn.com.fpt.sep490_g28_summer2024_be.dto.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsDTO;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponseDTO {

    @JsonProperty("category_id")
    BigInteger categoryId;

    String title;
    String slug;

    String description;

    @JsonProperty("created_at")
    LocalDateTime createdAt;

    @JsonProperty("updated_at")
    LocalDateTime updatedAt;

    @JsonProperty("news_list")
    List<NewsDTO> newsList;

    @JsonProperty("total")
    Long numberNewsByCategories;

    @JsonProperty("is_active")
    Boolean isActive;
}

package vn.com.fpt.sep490_g28_summer2024_be.dto.news;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.category.CategoryResponseDTO;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsResponseDTO {

    @JsonProperty("news_id")
    BigInteger newsId;
    String title;
    String slug;

    String thumbnail;

    @JsonProperty("short_description")
    String short_description;

    String content;

    Integer status;

    @JsonProperty("created_by")
    AccountDTO createdBy;

    @JsonProperty("created_at")
    LocalDateTime createdAt;

    @JsonProperty("updated_by")
    AccountDTO updatedBy;

    @JsonProperty("updated_at")
    LocalDateTime updatedAt;

    @JsonProperty("category")
    CategoryResponseDTO category;
}

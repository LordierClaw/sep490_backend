package vn.com.fpt.sep490_g28_summer2024_be.dto.news;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;


import java.math.BigInteger;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewsChangeStatusDTO {
    @NotNull(message = "Id không được để trống")
    @JsonProperty("news_id")
    BigInteger newsId;
    @NotNull(message = "status không được để trống")
    Integer status;
}

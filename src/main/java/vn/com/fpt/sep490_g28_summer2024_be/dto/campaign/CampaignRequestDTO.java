package vn.com.fpt.sep490_g28_summer2024_be.dto.campaign;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.math.BigInteger;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CampaignRequestDTO {
    @JsonProperty("campaign_id")
    BigInteger campaignId;

    @JsonProperty("title")
    @Length(max = 255, message = "Tiêu đề không được quá 255 ký tự")
    String title;

    @JsonProperty("description")
    @Length(max = 500, message = "Description can be up to 500 characters")
    String description;

    @JsonProperty("created_at")
    LocalDate createdAt;

    @JsonProperty("thumbnail")
    String thumbnail;

    @JsonProperty("updated_at")
    LocalDate updatedAt;

    @JsonProperty("is_active")
    Boolean isActive;
}
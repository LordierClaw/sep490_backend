package vn.com.fpt.sep490_g28_summer2024_be.dto.campaign;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.validator.constraints.Length;

import java.math.BigInteger;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CampaignResponseDTO {
    @JsonProperty("campaign_id")
    BigInteger campaignId;

    @JsonProperty("title")
    String title;

    @JsonProperty("slug")
    String slug;

    @JsonProperty("description")
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
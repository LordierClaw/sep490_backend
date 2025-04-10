package vn.com.fpt.sep490_g28_summer2024_be.dto.tracking;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrackingImageDTO {
    @JsonProperty("image_id")
    Long imageId;

    @JsonProperty("image_url")
    String imageUrl;

    @JsonProperty("tracking_id")
    TrackingDTO trackingId;
}

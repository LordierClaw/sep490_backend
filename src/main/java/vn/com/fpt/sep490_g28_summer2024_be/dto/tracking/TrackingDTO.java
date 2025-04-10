package vn.com.fpt.sep490_g28_summer2024_be.dto.tracking;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrackingDTO {
    @JsonProperty("tracking_id")
    BigInteger trackingId;

    @JsonProperty("title")
    @Length(max = 255, message = "Tiêu đề không được quá 255 ký tự")
    String title;

    @JsonProperty("content")
    String content;

    @JsonProperty("created_at")
    LocalDateTime createdAt;

    @JsonProperty("date")
    @PastOrPresent(message = "Ngày phải là ngày hợp lệ")
    LocalDate date;

    @JsonProperty("updated_at")
    LocalDateTime updatedAt;

    @JsonProperty("tracking_images")
    List<TrackingImageDTO> trackingImages;

    @JsonProperty("project_id")
    ProjectResponseDTO project;
}

package vn.com.fpt.sep490_g28_summer2024_be.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardDatetimeRequestDTO {

    @PastOrPresent
    @JsonProperty("start_date")
    private LocalDateTime startDate;

    @PastOrPresent
    @JsonProperty("end_date")
    private LocalDateTime endDate;

    @AssertTrue(message = "start date must be before end date")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return startDate.isBefore(endDate) || startDate.isEqual(endDate);
    }

}

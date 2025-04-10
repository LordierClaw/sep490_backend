package vn.com.fpt.sep490_g28_summer2024_be.dto.casso;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookResponseDTO {
    @JsonProperty("error")
    private Integer error;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private List<TransactionDataDTO> data;
}

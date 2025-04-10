package vn.com.fpt.sep490_g28_summer2024_be.dto.casso;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CassoRecordDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("tid")
    private String tid;

    @JsonProperty("description")
    private String description;

    @JsonProperty("amount")
    private BigDecimal amount;

    private BigDecimal cusumBalance;

    @JsonProperty("when")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime when;

    @JsonProperty("bookingDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime bookingDate;

    private String bankSubAccId;
    private String paymentChannel;
    private String virtualAccount;
    private String virtualAccountName;
    private String corresponsiveName;
    private String corresponsiveAccount;
    private String corresponsiveBankId;
    private String corresponsiveBankName;
    private String accountId;
    private String bankCodeName;
}

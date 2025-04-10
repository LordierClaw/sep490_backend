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
public class TransactionDataDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("tid")
    private String tid;

    @JsonProperty("description")
    private String description;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("cusum_balance")
    private BigDecimal cusumBalance;

    @JsonProperty("when")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime when;

    @JsonProperty("bank_sub_acc_id")
    private String bankSubAccId;

    @JsonProperty("subAccId")
    private String subAccId;

    @JsonProperty("bankName")
    private String bankName;

    @JsonProperty("bankAbbreviation")
    private String bankAbbreviation;

    @JsonProperty("virtualAccount")
    private String virtualAccount;

    @JsonProperty("virtualAccountName")
    private String virtualAccountName;

    @JsonProperty("corresponsiveName")
    private String corresponsiveName;

    @JsonProperty("corresponsiveAccount")
    private String corresponsiveAccount;

    @JsonProperty("corresponsiveBankId")
    private String corresponsiveBankId;

    @JsonProperty("corresponsiveBankName")
    private String corresponsiveBankName;
}

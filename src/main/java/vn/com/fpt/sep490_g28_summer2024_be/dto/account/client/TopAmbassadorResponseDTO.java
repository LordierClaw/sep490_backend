package vn.com.fpt.sep490_g28_summer2024_be.dto.account.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopAmbassadorResponseDTO {
    private BigInteger accountId;
    private String code;
    private String fullname;
    private String avatar;
    private BigDecimal totalDonation;
    private Long countDonations;
}

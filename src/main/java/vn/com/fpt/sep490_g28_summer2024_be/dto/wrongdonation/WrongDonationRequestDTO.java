package vn.com.fpt.sep490_g28_summer2024_be.dto.wrongdonation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import vn.com.fpt.sep490_g28_summer2024_be.dto.donation.DonationRequestDTO;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WrongDonationRequestDTO {

    @JsonProperty("wrong_donation_id")
    private BigInteger wrongDonationId;

    @JsonProperty("donation")
    private DonationRequestDTO donation;

}

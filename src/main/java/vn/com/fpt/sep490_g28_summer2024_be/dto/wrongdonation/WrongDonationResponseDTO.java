package vn.com.fpt.sep490_g28_summer2024_be.dto.wrongdonation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import vn.com.fpt.sep490_g28_summer2024_be.dto.donation.DonationResponseDTO;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WrongDonationResponseDTO {

    @JsonProperty("wrong_donation_id")
    private BigInteger wrongDonationId;

    @JsonProperty("donation")
    private DonationResponseDTO donation;

}

package vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountProfilePageDTO {
    @JsonProperty("code")
    String code;

    @JsonProperty("refer_code")
    String referCode;

    @JsonProperty("total_donated")
    BigDecimal totalDonations;

    @JsonProperty("total_donation_refer")
    BigDecimal totalDonationsRefer;

    @JsonProperty("total_donation_refer_count")
    Long totalDonationReferCount;

    @JsonProperty("donated_count")
    Long donationCount;

    @JsonProperty("avatar")
    String avatar;

    @JsonProperty("fullname")
    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    @Pattern(regexp = "^[\\p{L}\\s]*$", message = "Tên chỉ được chứa chữ cái và dấu cách")
    String fullname;

}

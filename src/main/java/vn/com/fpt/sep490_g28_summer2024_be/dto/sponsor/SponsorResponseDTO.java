package vn.com.fpt.sep490_g28_summer2024_be.dto.sponsor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SponsorResponseDTO {

    @JsonProperty("sponsor_id")
    BigInteger sponsorId;

    @JsonProperty("company_name")
    String companyName;

    @JsonProperty("business_field")
    String businessField;

    @JsonProperty("representative")
    String representative;

    @JsonProperty("representative_email")
    String representativeEmail;

    @JsonProperty("phone_number")
    String phoneNumber;

    @JsonProperty("value")
    String value;

    @JsonProperty("note")
    String note;

    @JsonProperty("logo")
    String logo;

    @JsonProperty("contract")
    String contract;

    @JsonProperty("status")
    Integer status;

    @JsonProperty("created_by")
    AccountDTO createdBy;

    @JsonProperty("created_at")
    LocalDateTime createdAt;

    @JsonProperty("updated_by")
    AccountDTO updatedBy;

    @JsonProperty("updated_at")
    LocalDateTime updatedAt;
}

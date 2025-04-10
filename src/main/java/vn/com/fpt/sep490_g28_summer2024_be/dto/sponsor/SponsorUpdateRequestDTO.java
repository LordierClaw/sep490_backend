package vn.com.fpt.sep490_g28_summer2024_be.dto.sponsor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SponsorUpdateRequestDTO {

    @JsonProperty("company_name")
    String companyName;

    @Length(max = 100, message = "Không được vượt quá 100 ký tự")
    @JsonProperty("business_field")
    String businessField;

    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    @JsonProperty("representative")
    String representative;

    @Length(max = 100, message = "Không được vượt quá 100 ký tự")
    @JsonProperty("representative_email")
    String representativeEmail;

    @Length(max = 20, message = "Không được vượt quá 20 ký tự")
    @JsonProperty("phone_number")
    String phoneNumber;

    @NotNull(message = "Giá trị hợp đồng không được để trống")
    @JsonProperty("value")
    String value;

    @Length(max = 500, message = "Không được vượt quá 500 ký tự")
    @JsonProperty("note")
    String note;

    @JsonProperty("logo")
    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    String logo;

    @JsonProperty("contract")
    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    String contract;
}

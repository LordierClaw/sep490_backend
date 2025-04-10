package vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountRegisterDTO {
    String email;
    String fullname;
    String password;
    @JsonProperty("confirm_password")
    String confirmPassword;
    String otp;
}

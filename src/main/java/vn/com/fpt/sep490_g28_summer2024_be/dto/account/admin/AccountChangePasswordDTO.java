package vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountChangePasswordDTO {
    @JsonProperty("email")
    private String email;

    @JsonProperty("old_password")
    private String oldPassword;

    @JsonProperty("new_password")
    @Length(max = 15, message = "Không được vượt quá 10 ký tự")
    @Length(min = 8, message = "Tối thiểu 8 ký tự")
    @NotEmpty(message = "Mật khẩu không được để trống")
    private String newPassword;
}


package vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import vn.com.fpt.sep490_g28_summer2024_be.dto.role.RoleDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDTO {
    @JsonProperty("account_id")
    BigInteger accountId;

    @JsonProperty("code")
    String code;

    @JsonProperty("refer_code")
    String referCode;

    @JsonProperty("email")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "Email không hợp lệ")
    String email;

    @JsonProperty("password")
    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    String password;

    @JsonProperty("fullname")
    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    @Pattern(regexp = "^[\\p{L}\\s]*$", message = "Tên chỉ được chứa chữ cái và dấu cách")
    String fullname;

    @JsonProperty("phone")
    @Pattern(regexp = "^(\\d{10})?$", message = "Số điện thoại phải là 10 chữ số hoặc để trống")
    private String phone;

    @JsonProperty("address")
    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    String address;

    @JsonProperty("avatar")
    String avatar;

    @JsonProperty("dob")
    @PastOrPresent(message = "Ngày sinh phải là ngày hợp lệ")
    LocalDate dob;

    @JsonProperty("created_at")
    @PastOrPresent(message = "Ngày tạo phải là ngày hợp lệ")
    LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @PastOrPresent(message = "Ngày cập nhat phải là ngày hợp lệ")
    LocalDateTime updatedAt;

    @JsonProperty("is_active")
    Boolean isActive;

    @JsonProperty("gender")
    Integer gender;

    @JsonProperty("role")
    RoleDTO role;

    @JsonProperty("total_donation")
    BigDecimal totalDonations;

    @JsonProperty("donation_count")
    Long donationCount;
}

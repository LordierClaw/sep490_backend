package vn.com.fpt.sep490_g28_summer2024_be.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id", columnDefinition = "BIGINT")
    BigInteger accountId;

    @Length(max = 20, message = "Không được vượt quá 20 ký tự")
    @Column(name = "code", columnDefinition = "VARCHAR(20)", unique = true)
    String code;

    @Length(max = 20, message = "Không được vượt quá 20 ký tự")
    @Column(name = "refer_code", columnDefinition = "VARCHAR(20)", unique = true)
    String referCode;

    @Column(name = "email", columnDefinition = "VARCHAR(50)")
    @Length(max = 50, message = "Không được vượt quá 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "Email không hợp lệ")
    String email;

    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    @NotBlank(message = "Mật khẩu không được để trống")
    @Column(name = "password", columnDefinition = "NVARCHAR(255)")
    String password;

    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    @Pattern(regexp = "^[\\p{L}\\s]*$", message = "Tên chỉ được chứa chữ cái và dấu cách")
    @Column(name = "fullname", columnDefinition = "NVARCHAR(255)")
    String fullname;

    @Column(name = "gender", columnDefinition = "INT")
    Integer gender;

    @Pattern(regexp = "^(\\d{10})?$", message = "Số điện thoại phải là 10 chữ số hoặc để trống")
    @Column(name = "phone", columnDefinition = "VARCHAR(10)")
    String phone;

    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    @Column(name = "address", columnDefinition = "NVARCHAR(255)")
    String address;

    @Column(name = "avatar", columnDefinition = "VARCHAR(255)")
    String avatar;

    @PastOrPresent(message = "Ngày sinh phải là ngày hợp lệ")
    @Column(name = "dob", columnDefinition = "DATE")
    LocalDate dob;

    @PastOrPresent(message = "Ngày tạo phải là ngày hợp lệ")
    @Column(name = "created_at", columnDefinition = "DATETIME")
    LocalDateTime createdAt;

    @PastOrPresent(message = "Ngày cập nhật phải là ngày hợp lệ")
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    LocalDateTime updatedAt;

    @Column(name = "is_active", columnDefinition = "BOOLEAN")
    Boolean isActive;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "role_id")
    Role role;

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", fullname='" + fullname + '\'' +
                ", gender=" + gender +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", avatar='" + avatar + '\'' +
                ", dob=" + dob +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isActive=" + isActive +
                '}';
    }
}

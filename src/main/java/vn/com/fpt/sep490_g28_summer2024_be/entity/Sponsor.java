package vn.com.fpt.sep490_g28_summer2024_be.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "sponsor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Sponsor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sponsor_id", columnDefinition = "BIGINT")
    BigInteger sponsorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    Project project;

    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    @Column(name = "company_name", columnDefinition = "NVARCHAR(255)")
    String companyName;

    @Length(max = 100, message = "Không được vượt quá 100 ký tự")
    @Column(name = "business_field", columnDefinition = "NVARCHAR(100)")
    String businessField;

    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    @Column(name = "representative",columnDefinition = "NVARCHAR(255)")
    String representative;

    @Length(max = 100, message = "Không được vượt quá 100 ký tự")
    @Column(name = "representative_email", columnDefinition = "VARCHAR(100)")
    String representativeEmail;

    @Length(max = 20, message = "Không được vượt quá 20 ký tự")
    @Column(name = "phone_number", columnDefinition = "VARCHAR(20)")
    String phoneNumber;

    @NotNull(message = "Giá trị hợp đồng không được để trống")
    @Column(name = "value", columnDefinition = "DECIMAL(20, 2)")
    BigDecimal value;

    @Length(max = 500, message = "Không được vượt quá 500 ký tự")
    @Column(name = "note", columnDefinition = "NVARCHAR(500)")
    String note;

    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    @Column(name = "logo", columnDefinition = "VARCHAR(255)")
    String logo;

    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    @Column(name = "contract", columnDefinition = "VARCHAR(255)")
    String contract;

    @PastOrPresent(message = "Ngày tạo phải là ngày hợp lệ")
    @Column(name = "created_at", columnDefinition = "DATETIME")
    LocalDateTime createdAt;

    @PastOrPresent(message = "Ngày cập nhật phải là ngày hợp lệ")
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "Sponsor{" +
                "sponsorId=" + sponsorId +
                ", companyName='" + companyName + '\'' +
                ", businessField='" + businessField + '\'' +
                ", representative='" + representative + '\'' +
                ", representativeEmail='" + representativeEmail + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", value=" + value +
                ", note='" + note + '\'' +
                ", contract='" + contract + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

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
@Table(name = "budget")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id", columnDefinition = "BIGINT")
    BigInteger budgetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    Project project;

    @NotNull(message = "Không được để trống title!")
    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    @Column(columnDefinition = "VARCHAR(255)")
    String title;

    @Column(name = "unit_price", columnDefinition = "DECIMAL(20, 2)")
    BigDecimal unitPrice;

    @Length(max = 500, message = "Không được vượt quá 500 ký tự")
    @Column(columnDefinition = "NVARCHAR(500)")
    String note;

    @NotNull(message = "Không được để trống")
    @Column(name = "status", columnDefinition = "INT")
    Integer status;


    @PastOrPresent(message = "Ngày tạo phải là ngày hợp lệ")
    @Column(name = "created_at", columnDefinition = "DATETIME")
    LocalDateTime createdAt;

    @PastOrPresent(message = "Ngày cập nhật phải là ngày hợp lệ")
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "Budget{" +
                "budgetId=" + budgetId +
                ", title='" + title + '\'' +
                ", unitPrice=" + unitPrice +
                ", note='" + note + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

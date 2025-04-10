package vn.com.fpt.sep490_g28_summer2024_be.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "expense")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Expense {
    @Id
    @Column(name = "expense_id", columnDefinition = "BIGINT")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    BigInteger expenseId;
    @Column(name = "title", columnDefinition = "NVARCHAR(255)")
    @Length(max = 255, message = "Tiêu đề không được quá 255 ký tự")
     String title;
    @NotNull(message = "Chi phí không được để trống")
    @DecimalMin(value = "0.00", message = "Chi phí phải lớn hơn hoặc bằng 0")
    @Column(name = "unit_price", columnDefinition = "DECIMAL(20, 2)")
    BigDecimal unitPrice;
    @Column(name = "created_at", columnDefinition = "DATETIME")
    LocalDateTime createdAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    LocalDateTime updatedAt;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ExpenseFile> expenseFiles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @Override
    public String toString() {
        return "Expense{" +
                "expenseId=" + expenseId +
                ", title='" + title + '\'' +
                ", unitPrice=" + unitPrice +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
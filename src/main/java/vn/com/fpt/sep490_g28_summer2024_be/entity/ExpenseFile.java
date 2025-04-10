package vn.com.fpt.sep490_g28_summer2024_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigInteger;
@Entity
@Table(name = "expense_file")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpenseFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_file_id", columnDefinition = "BIGINT")
    private BigInteger expenseFileId;
    @ManyToOne
    @JoinColumn(name = "expense_id")
    Expense expense;
    @Column(name = "file", columnDefinition = "NVARCHAR(255)")
    String file;

    @Override
    public String toString() {
        return "ExpenseFile{" +
                "expenseFileId=" + expenseFileId +
                ", file='" + file + '\'' +
                '}';
    }
}

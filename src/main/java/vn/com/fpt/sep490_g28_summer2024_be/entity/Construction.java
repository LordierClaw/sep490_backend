package vn.com.fpt.sep490_g28_summer2024_be.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigInteger;

@Entity
@Table(name = "construction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Construction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "construction_id", columnDefinition = "BIGINT")
    private BigInteger constructionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    Project project;

    @Length(max = 200, message = "Không được vượt quá 200 ký tự")
    @Column(columnDefinition = "NVARCHAR(200)")
    private String title;

    @Min(value = 0, message = "Không được nhập số âm")
    @Max(value = Integer.MAX_VALUE, message = "Không được nhập vượt quá "+Integer.MAX_VALUE)
    private Integer quantity;

    @Length(max = 20, message = "Không được vượt quá 20 ký tự")
    @Column(columnDefinition = "NVARCHAR(20)")
    private String unit;

    @Length(max = 200, message = "Không được vượt quá 200 ký tự")
    @Column(columnDefinition = "NVARCHAR(200)")
    private String note;

    @Override
    public String toString() {
        return "Construction{" +
                "constructionId=" + constructionId +
                ", title='" + title + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}

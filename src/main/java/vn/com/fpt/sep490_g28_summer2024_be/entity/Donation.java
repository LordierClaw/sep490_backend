package vn.com.fpt.sep490_g28_summer2024_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "donation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donation_id", columnDefinition = "BIGINT")
    private BigInteger donationId;

    @Column(name = "id", columnDefinition = "BIGINT", unique = true)
    private String id;

    @Column(name = "tid", columnDefinition = "VARCHAR(100)", unique = true)
    private String tid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refer_id")
    private Account refer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Account createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferred_project_id")
    private Project transferredProject;

    @Column(name = "created_at", columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @Column(name = "value", columnDefinition = "DECIMAL(20,2)")
    private BigDecimal value;

    @Column(name = "description")
    private String description;

    @Length(max = 50, message = "Không được vượt quá 50 ký tự")
    @Column(name = "bank_sub_acc_id", columnDefinition = "VARCHAR(50)")
    private String bankSubAccId;

    @Length(max = 50, message = "Không được vượt quá 50 ký tự")
    @Column(name = "bank_name", columnDefinition = "VARCHAR(50)")
    private String bankName;

    @Length(max = 100, message = "Không được vượt quá 100 ký tự")
    @Column(name = "corresponsive_name", columnDefinition = "VARCHAR(100)")
    private String corresponsiveName;

    @Length(max = 50, message = "Không được vượt quá 50 ký tự")
    @Column(name = "corresponsive_account", columnDefinition = "VARCHAR(50)")
    private String corresponsiveAccount;

    @Length(max = 50, message = "Không được vượt quá 50 ký tự")
    @Column(name = "corresponsive_bank_id", columnDefinition = "VARCHAR(50)")
    private String corresponsiveBankId;

    @Length(max = 50, message = "Không được vượt quá 50 ký tự")
    @Column(name = "corresponsive_bank_name", columnDefinition = "VARCHAR(50)")
    private String corresponsiveBankName;

    @Length(max = 500, message = "Không được vượt quá 500 ký tự")
    @Column(name = "note", columnDefinition = "NVARCHAR(500)")
    private String note;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "donation")
    private WrongDonation wrongDonation;

    @Override
    public String toString() {
        return "Donation{" +
                "donationId=" + donationId +
                ", id='" + id + '\'' +
                ", tid='" + tid + '\'' +
                ", createdAt=" + createdAt +
                ", value=" + value +
                ", description='" + description + '\'' +
                ", bankSubAccId='" + bankSubAccId + '\'' +
                ", bankName='" + bankName + '\'' +
                ", corresponsiveName='" + corresponsiveName + '\'' +
                ", corresponsiveAccount='" + corresponsiveAccount + '\'' +
                ", corresponsiveBankId='" + corresponsiveBankId + '\'' +
                ", corresponsiveBankName='" + corresponsiveBankName + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}

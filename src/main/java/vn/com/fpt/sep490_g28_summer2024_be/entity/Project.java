package vn.com.fpt.sep490_g28_summer2024_be.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "project")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id", columnDefinition = "BIGINT")
    private BigInteger projectId;

    @Length(max = 20, message = "Không được vượt quá 20 ký tự")
    @Column(name = "code", columnDefinition = "VARCHAR(20)", unique = true)
    private String code;

    @Length(max = 300, message = "Title không được vượt quá 300 ký tự")
    @Column(name = "title", columnDefinition = "NVARCHAR(300)", unique = true)
    private String title;

    @Length(max = 400, message = "Slug không được vượt quá 400 ký tự")
    @Column(name = "slug", columnDefinition = "NVARCHAR(400)", unique = true)
    private String slug;

    @Column(name = "background", columnDefinition = "TEXT")
    private String background;

    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    @Column(name = "address", columnDefinition = "NVARCHAR(255)")
    private String address;

    @NotEmpty(message = "Không được để trống")
    @Length(max = 100, message = "Không được vượt quá 300 ký tự")
    @Column(name = "ward", columnDefinition = "NVARCHAR(100)")
    private String ward;

    @NotEmpty(message = "Không được để trống")
    @Length(max = 100, message = "Không được vượt quá 100 ký tự")
    @Column(name = "district", columnDefinition = "NVARCHAR(100)")
    private String district;

    @NotEmpty(message = "Không được để trống")
    @Length(max = 100, message = "Không được vượt quá 100 ký tự")
    @Column(name = "province", columnDefinition = "NVARCHAR(100)")
    private String province;

    @Min(value = 0, message = "Tổng chi phí phải là số dương hợp lệ")
    @Column(name = "total_budget", columnDefinition = "DECIMAL(20, 2)")
    private BigDecimal totalBudget;

    @Min(value = 0, message = "Chi phí cần kêu gọi phải là số dương hợp lệ")
    @Column(name = "amount_needed_to_raise", columnDefinition = "DECIMAL(20, 2)")
    private BigDecimal amountNeededToRaise;

    @Min(value = 1, message = "Trạng thái phải là số dương hợp lệ")
    @Column(name = "status", columnDefinition = "INT")
    private Integer status;

    @PastOrPresent(message = "Ngày tạo phải là ngày hợp lệ")
    @Column(name = "created_at", columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @PastOrPresent(message = "Ngày cập nhật phải là ngày hợp lệ")
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "project")
    private List<Construction> constructions;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "project")
    private List<RelatedFile> relatedFile;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "project")
    private List<Assign> assigns;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "project")
    private List<ChallengeProject> challengeProjects;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "project")
    private List<ProjectImage> projectImages;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "project")
    private List<Budget> budgets;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "project")
    private List<Sponsor> sponsors;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "project")
    List<Tracking> trackings;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "project")
    List<Expense> expenses;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "project")
    private List<Donation> donations;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "transferredProject")
    private List<Donation> transferredDonations;

    @Override
    public String toString() {
        return "Project{" +
                "projectId=" + projectId +
                ", title='" + title + '\'' +
                ", background='" + background + '\'' +
                ", totalBudget=" + totalBudget +
                ", amountNeededToRaise=" + amountNeededToRaise +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", assigns=" + assigns +
                '}';
    }
}
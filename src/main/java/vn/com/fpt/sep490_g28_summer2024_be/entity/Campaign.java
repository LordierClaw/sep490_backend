package vn.com.fpt.sep490_g28_summer2024_be.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "campaign")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_id", columnDefinition = "BIGINT")
    BigInteger campaignId;

    @Column(name = "title", columnDefinition = "NVARCHAR(255)")
    @Length(max = 255, message = "Tiêu đề không được quá 255 ký tự")
    String title;

    @Column(name = "slug", columnDefinition = "NVARCHAR(355)")
    @Length(max = 355, message = "Tiêu đề không được quá 355 ký tự")
    String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "thumbnail", columnDefinition = "VARCHAR(255)")
    String thumbnail;

    @Column(name = "created_at")
    LocalDate createdAt;

    @Column(name = "updated_at")
    LocalDate updatedAt;

    @Column(name = "is_active", columnDefinition = "BOOLEAN")
    Boolean isActive;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "campaign")
    @JsonIgnore
    List<Project> projects;

    @Override
    public String toString() {
        return "Campaign{" +
                "campaignId=" + campaignId +
                ", title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", description='" + description + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isActive=" + isActive +
                '}';
    }
}

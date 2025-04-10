package vn.com.fpt.sep490_g28_summer2024_be.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "tracking")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Tracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tracking_id", columnDefinition = "BIGINT")
    BigInteger trackingId;

    @Column(name = "title", columnDefinition = "NVARCHAR(255)")
    @Length(max = 255, message = "Tiêu đề không được quá 255 ký tự")
    String title;

    @Column(name = "content", columnDefinition = "TEXT")
    String content;

    @PastOrPresent(message = "Ngày phải là ngày hợp lệ")
    @Column(name = "date", columnDefinition = "DATE")
    LocalDate date;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "tracking")
    List<TrackingImage> trackingImages;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @Override
    public String toString() {
        return "Tracking{" +
                "trackingId=" + trackingId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", date=" + date +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

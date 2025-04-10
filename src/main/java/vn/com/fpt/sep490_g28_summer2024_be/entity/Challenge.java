package vn.com.fpt.sep490_g28_summer2024_be.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "challenge")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id", columnDefinition = "BIGINT")
    BigInteger challengeId;

    @Length(max = 20, message = "Không được vượt quá 20 ký tự")
    @Column(name = "challenge_code", columnDefinition = "VARCHAR(20)")
    String challengeCode;

    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    @Column(columnDefinition = "NVARCHAR(255)", unique = true)
    String title;

    @Length(max = 355, message = "Slug không được vượt quá 355 ký tự")
    @Column(columnDefinition = "NVARCHAR(355)", unique = true)
    String slug;

    @Length(max = 255, message = "Không được vượt quá 255 ký tự")
    @Column(columnDefinition = "NVARCHAR(255)")
    String thumbnail;

    @Column(columnDefinition = "TEXT")
    String content;

    @Column(columnDefinition = "DECIMAL(20,2)")
    BigDecimal goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    Account createdBy;

    @PastOrPresent(message = "Ngày tạo phải là ngày hợp lệ")
    @Column(name = "created_at", columnDefinition = "DATETIME")
    LocalDateTime createdAt;


    @Column(name = "finished_at", columnDefinition = "DATE")
    LocalDate finishedAt;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "challenge")
    List<ChallengeProject> challengeProjects;

    @Override
    public String toString() {
        return "Challenge{" +
                "challengeId=" + challengeId +
                ", challengeCode='" + challengeCode + '\'' +
                ", title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", content='" + content + '\'' +
                ", goal=" + goal +
                ", createdAt=" + createdAt +
                ", finishedAt=" + finishedAt +
                '}';
    }
}

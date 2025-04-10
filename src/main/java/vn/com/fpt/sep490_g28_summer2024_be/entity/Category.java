package vn.com.fpt.sep490_g28_summer2024_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", columnDefinition = "BIGINT")
    BigInteger categoryId;

    @Column(name = "title", columnDefinition = "NVARCHAR(100)", unique = true)
    @Length(min = 1, max = 100, message = "title phải từ 0 đến 100 ký tự")
    String title;

    @Column(name = "slug", columnDefinition = "NVARCHAR(200)", unique = true)
    @Length(min = 1, max = 200, message = "slug phải từ 0 đến 200 ký tự")
    String slug;

    @Column(name = "description", columnDefinition = "NVARCHAR(255)")
    @Length(min = 1, max = 255, message = "title phải từ 0 đến 255 ký tự")
    String description;

    @Column(name = "created_at", columnDefinition = "DATETIME")
    LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "DATETIME")
    LocalDateTime updatedAt;

    @Column(name = "is_active", columnDefinition = "BOOLEAN")
    Boolean isActive;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "category")
    List<News> newsList;

    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isActive=" + isActive +
                '}';
    }
}

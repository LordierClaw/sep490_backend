    package vn.com.fpt.sep490_g28_summer2024_be.entity;

    import jakarta.persistence.*;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
    import lombok.*;
    import lombok.experimental.FieldDefaults;
    import org.hibernate.validator.constraints.Length;

    import java.math.BigInteger;
    import java.time.LocalDateTime;

    @Entity
    @Table(name = "news")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public class News {
        @Id
        @Column(name = "news_id",columnDefinition = "BIGINT")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        BigInteger newsId;

        @Column(name = "title", columnDefinition = "NVARCHAR(250)")
        @Length(min = 1, max = 250, message = "title phải từ 1 đến 250 ký tự")
        String title;

        @Column(name = "slug", columnDefinition = "VARCHAR(350)")
        @Length(min = 1, max = 350, message = "slug phải từ 1 đến 350 ký tự")
        String slug;

        @Column(name = "thumbnail", columnDefinition = "NVARCHAR(255)")
        String thumbnail;

        @Column(name = "short_description", columnDefinition = "NVARCHAR(500)")
        @Length(max = 500, message = "mô tả ngắn tối đa 500 ký tự")
        String short_description;

        @NotNull(message = "Nội dung là trường bắt buộc")
        @NotBlank(message = "Nội dung không được để trống")
        @Column(name = "content", columnDefinition = "TEXT")
        String content;

        @Column(name = "status", columnDefinition = "INT")
        Integer status;

        @ManyToOne
        @JoinColumn(name = "created_by")
        Account createdBy;

        @Column(name = "created_at", columnDefinition = "DATETIME")
        LocalDateTime createdAt;

        @ManyToOne
        @JoinColumn(name = "updated_by")
        Account updatedBy;

        @Column(name = "updated_at", columnDefinition = "DATETIME")
        LocalDateTime updatedAt;

        @ManyToOne
        @JoinColumn(name = "category_id")
        Category category;

        @Override
        public String toString() {
            return "News{" +
                    "newsId=" + newsId +
                    ", title='" + title + '\'' +
                    ", thumbnail='" + thumbnail + '\'' +
                    ", short_description='" + short_description + '\'' +
                    ", content='" + content + '\'' +
                    ", createdAt=" + createdAt +
                    ", updatedAt=" + updatedAt +
                    '}';
        }

    }

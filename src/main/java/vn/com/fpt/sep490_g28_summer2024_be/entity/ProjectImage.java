package vn.com.fpt.sep490_g28_summer2024_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigInteger;

@Entity
@Table(name = "project_image")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_image_id", columnDefinition = "BIGINT")
    BigInteger projectImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    Project project;

    @Column(name = "image", columnDefinition = "VARCHAR(255)")
    String image;

    @Override
    public String toString() {
        return "ProjectImage{" +
                "projectImageId=" + projectImageId +
                ", image='" + image + '\'' +
                '}';
    }
}

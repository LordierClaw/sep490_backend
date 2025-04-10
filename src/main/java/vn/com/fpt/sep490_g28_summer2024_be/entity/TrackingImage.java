package vn.com.fpt.sep490_g28_summer2024_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigInteger;

@Entity
@Data
@Table(name = "tracking_image")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrackingImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tracking_image_id", columnDefinition = "BIGINT")
    BigInteger trackingImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracking_id", nullable = false)
    Tracking tracking;

    @Column(name = "image", length = 255)
    String image;

    @Override
    public String toString() {
        return "TrackingImage{" +
                "trackingImageId=" + trackingImageId +
                ", image='" + image + '\'' +
                '}';
    }
}
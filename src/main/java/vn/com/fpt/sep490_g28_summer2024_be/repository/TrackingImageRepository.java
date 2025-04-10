package vn.com.fpt.sep490_g28_summer2024_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fpt.sep490_g28_summer2024_be.entity.TrackingImage;

import java.math.BigInteger;
import java.util.List;

public interface TrackingImageRepository extends JpaRepository<TrackingImage, BigInteger> {
    @Query(value = """
        SELECT ti.* 
        FROM tracking_image ti 
        WHERE ti.tracking_id = :trackingId
        """, nativeQuery = true)
    List<TrackingImage> findByTracking_Id(@Param("trackingId") BigInteger trackingId);
}

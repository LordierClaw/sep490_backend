package vn.com.fpt.sep490_g28_summer2024_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Tracking;

import java.math.BigInteger;
import java.util.List;

public interface TrackingRepository extends JpaRepository<Tracking, BigInteger> {
    @Query(value = """
        SELECT t.* 
        FROM tracking t 
        WHERE t.project_id = :projectId 
        AND (:title IS NULL OR t.title LIKE CONCAT('%', :title, '%')) 
        ORDER BY t.tracking_id DESC
        """,
            countQuery = """
        SELECT COUNT(t.tracking_id) 
        FROM tracking t 
        WHERE t.project_id = :projectId 
        AND (:title IS NULL OR t.title LIKE CONCAT('%', :title, '%'))
        """,
            nativeQuery = true)
    Page<Tracking> findTrackingByFilterAndProjectId(@Param("title") String title,
                                                    @Param("projectId") BigInteger projectId,
                                                    Pageable pageable);

    @Query(value = """
        SELECT t.* 
        FROM tracking t 
        WHERE t.project_id = :projectId 
        AND t.title IN :titles
        """, nativeQuery = true)
    List<Tracking> findByProjectIdAndTitles(@Param("projectId") BigInteger projectId,
                                            @Param("titles") List<String> titles);
}
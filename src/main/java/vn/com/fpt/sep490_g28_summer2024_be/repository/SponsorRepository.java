package vn.com.fpt.sep490_g28_summer2024_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Sponsor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

public interface SponsorRepository extends JpaRepository<Sponsor, BigInteger> {
    @Query(value = """
    SELECT * FROM sponsor s WHERE
    (:companyName IS NULL OR s.company_name LIKE CONCAT('%', :companyName, '%')) AND
    (s.project_id = :projectId)
    ORDER BY sponsor_id DESC
    """,
            nativeQuery = true)
    Page<Sponsor> findSponsorsByFilters(
            @Param("companyName") String companyName,
            @Param("projectId") BigInteger projectId,
            Pageable pageable
    );


    @Query(value = """
        SELECT COUNT(*) 
        FROM sponsor s 
        WHERE s.project_id = :projectId
        """, nativeQuery = true)
    Integer countSponsorsByProjectId(@Param("projectId") BigInteger projectId);

    @Query(value = """
    SELECT IFNULL(SUM(s.value), 0) FROM sponsor s
    WHERE (YEAR(s.created_at) = :year)
    AND (MONTH(s.created_at) = :month)
    """, nativeQuery = true)
    BigDecimal getAllSponsorValueByMonth(@Param("month") Integer month,
                                         @Param("year") Integer year);

}

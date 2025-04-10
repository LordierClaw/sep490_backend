package vn.com.fpt.sep490_g28_summer2024_be.repository;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.data.repository.query.Param;
import vn.com.fpt.sep490_g28_summer2024_be.dto.chart.interfacedto.DonationChartByMonthInterfaceDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.chart.interfacedto.DonationChartByWeekInterfaceDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Donation;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;


public interface DonationRepository extends JpaRepository<Donation, BigInteger> {

    @Query(
            value = """
                    SELECT *
                    FROM donation d
                    WHERE (d.project_id = :projectId OR d.transferred_project_id = :projectId)
                    AND (:description IS NULL OR d.description LIKE CONCAT('%', :description, '%'))
                    ORDER BY d.created_at DESC
                    """,
            countQuery = """
                    SELECT COUNT(d.donation_id)
                    FROM donation d
                    WHERE (d.project_id = :projectId OR d.transferred_project_id = :projectId)
                    AND (:description IS NULL OR d.description LIKE CONCAT('%', :description, '%'))
                    """,
            nativeQuery = true
    )
    Page<Donation> findDonationsByFilters(
            @Param("projectId") BigInteger projectId,
            @Param("description") String description,
            Pageable pageable
    );

    @Query(value = """
            SELECT *
            FROM donation d
            WHERE (:description IS NULL OR d.description LIKE CONCAT('%', :description, '%'))
            ORDER BY d.donation_id DESC
            """, countQuery = """
            SELECT COUNT(d.donation_id)
            FROM donation d
            WHERE (:description IS NULL OR d.description LIKE CONCAT('%', :description, '%'))
            """, nativeQuery = true)
    Page<Donation> findAllDonationsByFilters(@Param("description") String description,
                                             Pageable pageable);

    @Query(value = "SELECT * FROM donation d WHERE d.created_by = :accountId AND " +
            "(:description IS NULL OR d.description LIKE %:description%) " +
            "ORDER BY d.created_at DESC",
            countQuery = "SELECT count(*) FROM donation d WHERE d.created_by = :accountId " +
                    "AND (:description IS NULL OR d.description LIKE %:description%)",
            nativeQuery = true)
    Page<Donation> findByCreatedByAndDescription(@Param("accountId") BigInteger accountId,
                                                 @Param("description") String description, Pageable pageable);

    List<Donation> findAllByProject(Project project);

    @Query(value = "SELECT d.created_at FROM donation d ORDER BY d.created_at DESC LIMIT 1", nativeQuery = true)
    LocalDateTime getLastDonationDate();

    Boolean existsDonationById(String id);

    @Query(value = """
            SELECT SUM(d.value)
            FROM donation d
            WHERE d.created_by = (SELECT a.account_id FROM account a WHERE a.code = :accountCode)
            """, nativeQuery = true)
    BigDecimal sumDonationsByAccountCode(@Param("accountCode") String accountCode);

    @Query(value = """
            SELECT COUNT(*)
            FROM donation d
            WHERE d.created_by = (SELECT a.account_id FROM account a WHERE a.code = :accountCode)
            """, nativeQuery = true)
    Long countDonationsByAccountCode(@Param("accountCode") String accountCode);


    @Query(value = """
            SELECT d.*
            FROM donation d
            LEFT JOIN wrong_donation wd ON d.donation_id = wd.donation_id
            WHERE (d.project_id = :projectId OR d.transferred_project_id = :projectId)
              AND (:description IS NULL OR d.description LIKE CONCAT('%', :description, '%'))
            ORDER BY d.created_at DESC
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM donation d
                    LEFT JOIN wrong_donation wd ON d.donation_id = wd.donation_id
                    WHERE (d.project_id = :projectId OR d.transferred_project_id = :projectId)
                      AND (:description IS NULL OR d.description LIKE CONCAT('%', :description, '%'))
                    """,
            nativeQuery = true)
    Page<Donation> findDonationsAdminByFilters(
            @Param("projectId") BigInteger projectId,
            @Param("description") String description,
            Pageable pageable
    );

    @Query(value = """
            SELECT SUM(d.value)
            FROM donation d
            WHERE d.challenge_id = :challengeId
            """, nativeQuery = true)
    BigDecimal sumDonationsByChallengeId(@Param("challengeId") BigInteger challengeId);


    @Query(value = """
    SELECT d.*
    FROM donation d
    WHERE d.challenge_id = :challengeId
    AND (:description IS NULL OR d.description LIKE CONCAT('%', :description, '%'))
    ORDER BY d.donation_id
    """, countQuery = """
    SELECT COUNT(d.id)
    FROM donation d
    WHERE d.challenge_id = :challengeId
    AND (:description IS NULL OR d.description LIKE CONCAT('%', :description, '%'))
    """, nativeQuery = true)
    Page<Donation> findDonationsByChallengeIdAndDescription(
            @Param("challengeId") BigInteger challengeId,
            @Param("description") String description,
            Pageable pageable);


    @Query(value = """
    SELECT IFNULL(SUM(d.value), 0)
    FROM donation d
    WHERE (YEAR(d.created_at) = :year)
    AND (MONTH(d.created_at) = :month)
    """, nativeQuery = true)
    BigDecimal getTotalDonationByMonth(@Param("year") Integer year,
                                       @Param("month") Integer month);

    @Query(value = """
    SELECT IFNULL(SUM(d.value), 0)
    FROM donation d
    LEFT JOIN wrong_donation wd ON wd.donation_id = d.donation_id
    WHERE wd.wrong_donation_id IS NOT NULL
    AND (YEAR(d.created_at) = :year)
    AND (MONTH(d.created_at) = :month)
    """, nativeQuery = true)
    BigDecimal getTotalWrongDonationByMonth(@Param("year") Integer year,
                                            @Param("month") Integer month);

    @Query(value = """
    SELECT COUNT(d.donation_id)
    FROM donation d
    WHERE (YEAR(d.created_at) = :year)
    AND   (MONTH(d.created_at) = :month)
    """, nativeQuery = true)
    Long getTotalCountDonationByMonth(@Param("year") Integer year,
                                      @Param("month") Integer month);

    @Query(value = """
    SELECT
        MONTH(d.created_at) AS monthNumber,
        SUM(d.value) AS 'value'
    FROM
        donation d
    WHERE
        YEAR(d.created_at) = :year
    GROUP BY
        MONTH(d.created_at)
    ORDER BY
        monthNumber ASC;
    """, nativeQuery = true)
    List<DonationChartByMonthInterfaceDTO> getTotalDonationByYear(@Param("year") Integer year);

    @Query(value = """
    SELECT
        WEEK(d.created_at, 3) - WEEK(DATE_SUB(d.created_at, INTERVAL DAYOFMONTH(d.created_at) - 1 DAY), 3) + 1 AS weekOfMonth,
        SUM(d.value) AS 'value'
    FROM
        donation d
    WHERE
        YEAR(d.created_at) = :year
        AND MONTH(d.created_at) = :month
        AND d.created_at <= CURDATE()
    GROUP BY
        weekOfMonth
    ORDER BY
        weekOfMonth ASC
    """, nativeQuery = true)
    List<DonationChartByWeekInterfaceDTO> getTotalDonationByWeekOfMonth(@Param("year") Integer year,
                                                                        @Param("month") Integer month);

    @Query(value = """
    SELECT d.*
    FROM donation d
    LEFT JOIN challenge ch ON d.challenge_id = ch.challenge_id
    WHERE (:description IS NULL OR d.description LIKE %:description%)
    AND ((:referId IS NULL OR d.refer_id = :referId) OR ch.created_by = :referId)
    ORDER BY d.donation_id DESC
    """,countQuery = """
    SELECT COUNT(d.donation_id)
    FROM donation d
    LEFT JOIN challenge ch ON d.challenge_id = ch.challenge_id
    WHERE (:description IS NULL OR d.description LIKE %:description%)
    AND ((:referId IS NULL OR d.refer_id = :referId) OR ch.created_by = :referId)
    """,
            nativeQuery = true)
    Page<Donation> getDonationsByReferId(@Param("description") String description,
                                         @Param("referId") BigInteger referId,
                                         Pageable pageable);


    @Query(value = """
            SELECT IFNULL(SUM(d.value), 0)
            FROM donation d
            WHERE d.refer_id = :referId
            """, nativeQuery = true)
    BigDecimal getTotalDonationByReferId(@Param("referId") BigInteger referId);

    @Query(value = """
            SELECT COUNT(*)
            FROM donation d
            WHERE d.refer_id = :referId
            """, nativeQuery = true)
    Long countDonationsByReferId(@Param("referId") BigInteger referId);

    @Query(value = """
            SELECT SUM(d.value) as total_donation
            FROM donation d
            JOIN challenge c ON d.challenge_id = c.challenge_id
            WHERE c.created_by = :createdBy
            """, nativeQuery = true)
    BigDecimal getTotalDonationsChallengeByCreatedBy(@Param("createdBy") BigInteger createdBy);

    @Query(value = """
            SELECT COUNT(*)
            FROM donation d
            JOIN challenge c ON d.challenge_id = c.challenge_id
            WHERE c.created_by = :createdBy
            """, nativeQuery = true)
    Long countDonationsChallengeByCreatedBy(@Param("createdBy") BigInteger createdBy);


    @Query(value = """
    SELECT d.*
    FROM donation d
    WHERE d.project_id = :projectId OR d.transferred_project_id = :projectId
    """, nativeQuery = true)
    List<Donation> getAllDonationByProjectId(@Param("projectId") BigInteger projectId);


    @Query(value = """
    SELECT * 
    FROM donation d 
    WHERE :description LIKE CONCAT('%', d.tid, '%') 
    """, nativeQuery = true)
    Donation getDonationByTid(@Param("description") String description);

}

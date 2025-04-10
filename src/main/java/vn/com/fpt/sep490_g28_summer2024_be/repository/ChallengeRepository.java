package vn.com.fpt.sep490_g28_summer2024_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.interfacedto.ChallengeInterfaceDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Challenge;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, BigInteger> {

    @Query(value = """
            SELECT * FROM challenge ch
            WHERE :description LIKE CONCAT('%', ch.challenge_code, '%')
            AND (ch.finished_at >= CURRENT_DATE)
            LIMIT 1
            """, nativeQuery = true)
    Challenge findChallengeByDescription(@Param("description") String description);

    @Query(value = """
    SELECT ch.challenge_id as challengeId,
           ch.challenge_code as challengeCode,
           ch.title,
           ch.slug,
           ch.thumbnail,
           ch.content,
           ch.goal,
           a.account_id as accountId,
           a.fullname,
           a.code,
           a.avatar,
           ch.created_at as createdAt,
           ch.finished_at as finishedAt,
           IFNULL(SUM(d.value), 0) as totalDonation
    FROM challenge ch
    LEFT JOIN donation d ON d.challenge_id = ch.challenge_id
    LEFT JOIN account a ON a.account_id = ch.created_by
    WHERE (:title IS NULL OR ch.title LIKE %:title%)
    AND (:year IS NULL OR YEAR(ch.created_at) = :year)
    GROUP BY ch.challenge_id, ch.challenge_code, ch.title, ch.thumbnail, ch.content, ch.goal, ch.created_by, ch.created_at, ch.finished_at
    HAVING (:minTotalDonation IS NULL OR COALESCE(SUM(d.value), 0) >= :minTotalDonation)
    AND    (:maxTotalDonation IS NULL OR COALESCE(SUM(d.value), 0) <= :maxTotalDonation)
    ORDER BY ch.challenge_id DESC
    """, countQuery = """
    SELECT COUNT(*)
    FROM (
        SELECT c.challenge_id
        FROM challenge c
        LEFT JOIN donation d ON c.challenge_id = d.challenge_id
        WHERE (:title IS NULL OR c.title LIKE %:title%)
        AND (:year IS NULL OR YEAR(c.created_at) = :year)
        GROUP BY c.challenge_id, c.challenge_code, c.title, c.thumbnail, c.content, c.goal, c.created_by, c.created_at, c.finished_at
        HAVING (:minTotalDonation IS NULL OR COALESCE(SUM(d.value), 0) >= :minTotalDonation)
               AND (:maxTotalDonation IS NULL OR COALESCE(SUM(d.value), 0) <= :maxTotalDonation)
    ) AS count_query
    """, nativeQuery = true)
    Page<ChallengeInterfaceDTO> findChallengesByFilters(
            @Param("title") String title,
            @Param("year") Integer year,
            @Param("minTotalDonation") BigDecimal minDonate,
            @Param("maxTotalDonation") BigDecimal maxDonate,
            Pageable pageable
    );

    @Query(value = """
    SELECT ch.challenge_id as challengeId,
           ch.challenge_code as challengeCode,
           ch.title,
           ch.slug,
           ch.thumbnail,
           ch.content,
           ch.goal,
           a.account_id as accountId,
           a.fullname,
           a.code,
           a.avatar,
           ch.created_at as createdAt,
           ch.finished_at as finishedAt,
           IFNULL(SUM(d.value), 0) as totalDonation
    FROM challenge ch
    LEFT JOIN donation d ON d.challenge_id = ch.challenge_id
    LEFT JOIN account a ON a.account_id = ch.created_by
    WHERE (:accountCode IS NULL OR a.code = :accountCode)
    AND   (ch.finished_at IS NULL OR ch.finished_at < CURRENT_DATE)
    GROUP BY ch.challenge_id, ch.challenge_code,
             ch.title, ch.slug, ch.thumbnail, ch.content, ch.goal,
             a.account_id, a.fullname, a.code, a.avatar,
             ch.created_at, ch.finished_at
    ORDER BY ch.challenge_id DESC
    """,countQuery = """
    SELECT COUNT(*)
    FROM `challenge` c
    WHERE (:accountCode IS NULL OR EXISTS (SELECT 1 FROM `account` a WHERE a.account_id = c.created_by AND a.code = :accountCode))
    AND (c.finished_at IS NULL OR c.finished_at < CURRENT_DATE)
    """, nativeQuery = true)
    Page<ChallengeInterfaceDTO> findExpiredChallengesByAccountCode(@Param("accountCode") String accountCode,
                                                       Pageable pageable);

    @Query(value = """
    SELECT ch.challenge_id as challengeId,
           ch.challenge_code as challengeCode,
           ch.title,
           ch.slug,
           ch.thumbnail,
           ch.content,
           ch.goal,
           a.account_id as accountId,
           a.fullname,
           a.code,
           a.avatar,
           ch.created_at as createdAt,
           ch.finished_at as finishedAt,
           IFNULL(SUM(d.value), 0) as totalDonation
    FROM challenge ch
    LEFT JOIN donation d ON d.challenge_id = ch.challenge_id
    LEFT JOIN account a ON a.account_id = ch.created_by
    WHERE (:accountCode IS NULL OR a.code = :accountCode)
    AND   (ch.finished_at IS NULL OR ch.finished_at >= CURRENT_DATE())
    GROUP BY ch.challenge_id, ch.challenge_code,
             ch.title, ch.slug, ch.thumbnail, ch.content, ch.goal,
             a.account_id, a.fullname, a.code, a.avatar,
             ch.created_at, ch.finished_at
    ORDER BY ch.challenge_id DESC
    """,countQuery = """
    SELECT COUNT(*)
    FROM `challenge` c
    WHERE (:accountCode IS NULL OR EXISTS (SELECT 1 FROM `account` a WHERE a.account_id = c.created_by AND a.code = :accountCode))
    AND (c.finished_at IS NULL OR c.finished_at >= CURRENT_DATE())
    """, nativeQuery = true)
    Page<ChallengeInterfaceDTO> findOngoingChallengesByAccountCode(@Param("accountCode") String accountCode,
                                                       Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM challenge WHERE title = :title AND challenge_id != :id", nativeQuery = true)
    Long countChallengeByTitle(@Param("title") String title, @Param("id") BigInteger id);

    boolean existsByTitle(String title);

    @Query(value = """
    SELECT ch.challenge_id as challengeId,
           ch.challenge_code as challengeCode,
           ch.title,
           ch.slug,
           ch.thumbnail,
           ch.content,
           ch.goal,
           a.account_id as accountId,
           a.fullname,
           a.code,
           a.avatar,
           ch.created_at as createdAt,
           ch.finished_at as finishedAt,
           IFNULL(SUM(d.value), 0) as totalDonation
    FROM challenge ch
    LEFT JOIN donation d ON d.challenge_id = ch.challenge_id
    LEFT JOIN account a ON a.account_id = ch.created_by
    GROUP BY ch.challenge_id, ch.challenge_code,
             ch.title, ch.slug, ch.thumbnail, ch.content, ch.goal,
             a.account_id, a.fullname, a.code, a.avatar,
             ch.created_at, ch.finished_at
    ORDER BY totalDonation DESC
    LIMIT :top
    """, nativeQuery = true)
    List<ChallengeInterfaceDTO> getTopChallenge(@Param("top") Integer top);

    @Query(value = """
    SELECT ch.challenge_id as challengeId,
           ch.challenge_code as challengeCode,
           ch.title,
           ch.slug,
           ch.thumbnail,
           ch.content,
           ch.goal,
           a.account_id as accountId,
           a.fullname,
           a.code,
           a.avatar,
           ch.created_at as createdAt,
           ch.finished_at as finishedAt,
           IFNULL(SUM(d.value), 0) as totalDonation
    FROM challenge ch
    LEFT JOIN donation d ON d.challenge_id = ch.challenge_id
    LEFT JOIN account a ON a.account_id = ch.created_by
    WHERE (:title IS NULL OR ch.title LIKE CONCAT('%', :title, '%'))
    AND (:fullname IS NULL OR a.fullname LIKE CONCAT('%', :fullname, '%'))
    GROUP BY ch.challenge_id, ch.challenge_code,
             ch.title, ch.slug, ch.thumbnail, ch.content, ch.goal,
             a.account_id, a.fullname, a.code, a.avatar,
             ch.created_at, ch.finished_at
    HAVING (:minTotalDonation IS NULL OR totalDonation >= :minTotalDonation)
    AND (:maxTotalDonation IS NULL OR totalDonation <= :maxTotalDonation)
    ORDER BY ch.challenge_id DESC
    """, countQuery = """
        SELECT COUNT(*)
        FROM (
            SELECT ch.challenge_id
            FROM challenge ch
            LEFT JOIN donation d ON d.challenge_id = ch.challenge_id
            LEFT JOIN account a ON a.account_id = ch.created_by
            WHERE (:title IS NULL OR ch.title LIKE CONCAT('%', :title, '%'))
            AND (:fullname IS NULL OR a.fullname LIKE CONCAT('%', :fullname, '%'))
            GROUP BY ch.challenge_id, ch.challenge_code,
                     ch.title, ch.slug, ch.thumbnail, ch.content, ch.goal,
                     a.account_id, a.fullname, a.code, a.avatar,
                     ch.created_at, ch.finished_at
            HAVING (:minTotalDonation IS NULL OR IFNULL(SUM(d.value), 0) >= :minTotalDonation)
            AND (:maxTotalDonation IS NULL OR IFNULL(SUM(d.value), 0) <= :maxTotalDonation)
        ) AS subquery
    """, nativeQuery = true)
    Page<ChallengeInterfaceDTO> getChallenges(@Param("title") String title,
                                             @Param("fullname") String fullname,
                                             @Param("minTotalDonation") BigDecimal min,
                                             @Param("maxTotalDonation") BigDecimal max,
                                             Pageable pageable);

}

package vn.com.fpt.sep490_g28_summer2024_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fpt.sep490_g28_summer2024_be.dto.chart.interfacedto.StatisticsInterfaceDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.interfacedto.ProjectDonationInformattionDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.interfacedto.ProjectInterfaceDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.interfacedto.ProjectTransactionDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, BigInteger> {

    boolean existsByTitle(String title);

    @Query(value = """
            SELECT * FROM project WHERE
            (:status IS NULL OR status = :status)
            ORDER BY project_id DESC
            """, nativeQuery = true)
    List<Project> findProjectByStatus(@Param("status") BigInteger status);

    @Query(value = """
            SELECT p.project_id AS projectId,
                   p.title,
                   p.code,
                   p.created_at AS createdAt,
                   p.status,
                   p.amount_needed_to_raise AS amountNeededToRaise,
                   c.campaign_id AS campaignId,
                   c.title AS campaignTitle,
                   p.address,
                   p.ward,
                   p.district,
                   p.province,
                   p.total_budget AS totalBudget,
                   IFNULL((SUM(CASE WHEN d.transferred_project_id IS NULL AND wd.wrong_donation_id IS NULL THEN d.value ELSE 0 END)+SUM(CASE WHEN d.transferred_project_id = p.project_id THEN d.value ELSE 0 END)),0) as totalDonation
            FROM project p
            LEFT JOIN donation d ON p.project_id = d.project_id OR d.transferred_project_id = p.project_id
            LEFT JOIN campaign c ON c.campaign_id = p.campaign_id
            LEFT JOIN wrong_donation wd ON wd.donation_id = d.donation_id
            WHERE (:title IS NULL OR p.title LIKE CONCAT('%', :title, '%'))
              AND (:campaign_id IS NULL OR c.campaign_id = :campaign_id)
              AND (:status IS NULL OR p.status = :status)
              AND (:province IS NULL OR p.province = :province)
              AND (:year IS NULL OR YEAR(p.created_at) = :year)
            GROUP BY p.project_id, p.title, p.code, p.created_at, p.status, p.amount_needed_to_raise,
                     c.campaign_id, c.title, p.address, p.ward, p.district, p.province, p.total_budget
            ORDER BY p.project_id DESC
            """,
            countQuery = """
                    SELECT COUNT(p.project_id)
                    FROM project p
                    LEFT JOIN campaign c ON c.campaign_id = p.campaign_id
                    WHERE (:title IS NULL OR p.title LIKE CONCAT('%', :title, '%'))
                      AND (:campaign_id IS NULL OR c.campaign_id = :campaign_id)
                      AND (:status IS NULL OR p.status = :status)
                      AND (:province IS NULL OR p.province = :province)
                      AND (:year IS NULL OR YEAR(p.created_at) = :year)
                    """,
            nativeQuery = true)
    Page<ProjectInterfaceDTO> findProjectByFilters(@Param("title") String title,
                                                   @Param("campaign_id") BigInteger campaignId,
                                                   @Param("year") String year,
                                                   @Param("province") String province,
                                                   @Param("status") Integer status,
                                                   Pageable pageable);


    @Query(value = """
    SELECT p.project_id as projectId,
           p.title,
           p.code,
           p.created_at as createdAt,
           p.status,
           p.background,
           p.amount_needed_to_raise as amountNeededToRaise,
           c.campaign_id as campaignId,
           c.title as campaignTitle,
           p.address,
           p.ward,
           p.district,
           p.province,
           p.total_budget as totalBudget,
           IFNULL(SUM(CASE WHEN d.transferred_project_id IS NULL AND wd.wrong_donation_id IS NULL THEN d.value ELSE 0 END) +
                  SUM(CASE WHEN d.transferred_project_id = p.project_id THEN d.value ELSE 0 END), 0) AS totalDonation
    FROM project p
    LEFT JOIN donation d ON p.project_id = d.project_id OR d.transferred_project_id = p.project_id
    LEFT JOIN campaign c ON c.campaign_id = p.campaign_id
    LEFT JOIN wrong_donation wd ON wd.donation_id = d.donation_id
    WHERE (:title IS NULL OR p.title LIKE CONCAT('%', :title, '%'))
          AND (:campaign_id IS NULL OR c.campaign_id = :campaign_id)
          AND (:status IS NULL OR p.status = :status)
          AND (:year IS NULL OR YEAR(p.created_at) = :year)
          AND (:code IS NULL OR (p.code) LIKE CONCAT('%', :code, '%'))
    GROUP BY p.project_id, p.title, p.code, p.created_at, p.status, p.background, p.amount_needed_to_raise,
             c.campaign_id, c.title, p.address, p.ward, p.district, p.province, p.total_budget
    HAVING (:minTotalBudget IS NULL OR p.total_budget >= :minTotalBudget)
           AND (:maxTotalBudget IS NULL OR p.total_budget <= :maxTotalBudget)
    ORDER BY totalBudget ASC
            """, countQuery = """
    SELECT COUNT(*)
    FROM (
        SELECT p.project_id,
               p.total_budget
        FROM project p
        LEFT JOIN donation d ON p.project_id = d.project_id OR d.transferred_project_id = p.project_id
        LEFT JOIN campaign c ON c.campaign_id = p.campaign_id
        LEFT JOIN wrong_donation wd ON wd.donation_id = d.donation_id
        WHERE (:title IS NULL OR p.title LIKE CONCAT('%', :title, '%'))
              AND (:campaign_id IS NULL OR c.campaign_id = :campaign_id)
              AND (:status IS NULL OR p.status = :status)
              AND (:year IS NULL OR YEAR(p.created_at) = :year)
              AND (:code IS NULL OR (p.code) LIKE CONCAT('%', :code, '%'))
        GROUP BY p.project_id, p.title, p.code, p.created_at, p.status, p.background, p.amount_needed_to_raise,
                 c.campaign_id, c.title, p.address, p.ward, p.district, p.province, p.total_budget
        HAVING (:minTotalBudget IS NULL OR p.total_budget >= :minTotalBudget)
               AND (:maxTotalBudget IS NULL OR p.total_budget <= :maxTotalBudget)
    ) AS count_query
    """, nativeQuery = true)
    Page<ProjectInterfaceDTO> findProjectCards(@Param("title") String title,
                                               @Param("campaign_id") BigInteger campaignId,
                                               @Param("status") Integer status,
                                               @Param("year") Integer year,
                                               @Param("code") String code,
                                               @Param("minTotalBudget") BigDecimal minTotalBudget,
                                               @Param("maxTotalBudget") BigDecimal maxTotalBudget,
                                               Pageable pageable);


    @Query(value = """
    SELECT p.project_id AS projectId,
           IFNULL(SUM(CASE WHEN d.transferred_project_id IS NULL AND wd.wrong_donation_id IS NULL THEN d.value ELSE 0 END) +
                  SUM(CASE WHEN d.transferred_project_id = p.project_id THEN d.value ELSE 0 END), 0) AS totalDonation
    FROM project p
    LEFT JOIN donation d ON p.project_id = d.project_id OR d.transferred_project_id = p.project_id
    LEFT JOIN campaign c ON c.campaign_id = p.campaign_id
    LEFT JOIN wrong_donation wd ON wd.donation_id = d.donation_id
    WHERE p.campaign_id = :campaignId
      AND (:status IS NULL OR p.status = :status)
      AND (:minBudget IS NULL OR p.total_budget >= :minBudget)
      AND (:maxBudget IS NULL OR p.total_budget <= :maxBudget)
    GROUP BY p.project_id, p.title, p.code, p.created_at, p.status, p.background, p.amount_needed_to_raise,
             c.campaign_id, c.title, p.address, p.ward, p.district, p.province, p.total_budget
    HAVING IFNULL(SUM(CASE WHEN d.transferred_project_id IS NULL AND wd.wrong_donation_id IS NULL THEN d.value ELSE 0 END) +
    SUM(CASE WHEN d.transferred_project_id = p.project_id THEN d.value ELSE 0 END), 0) < p.amount_needed_to_raise
    ORDER BY p.created_at Desc
    """, nativeQuery = true)
    Page<ProjectInterfaceDTO> findProjectsClientByCampaignId(@Param("status") Integer status,
                                                             @Param("campaignId") BigInteger campaignId,
                                                             @Param("minBudget") BigDecimal minBudget,
                                                             @Param("maxBudget") BigDecimal maxBudget,
                                                             Pageable pageable);


    @Query(value = """
            SELECT p.project_id AS projectId,
                   p.title,
                   p.code,
                   p.created_at AS createdAt,
                   p.status,
                   p.amount_needed_to_raise AS amountNeededToRaise,
                   c.campaign_id AS campaignId,
                   c.title AS campaignTitle,
                   p.address,
                   p.ward,
                   p.district,
                   p.province,
                   p.total_budget AS totalBudget,
                   IFNULL(SUM(CASE WHEN d.transferred_project_id IS NULL AND wd.wrong_donation_id IS NULL THEN d.value ELSE 0 END) +
                          SUM(CASE WHEN d.transferred_project_id = p.project_id THEN d.value ELSE 0 END), 0) AS totalDonation
            FROM project p
            JOIN assign a ON p.project_id = a.project_id
            LEFT JOIN donation d ON p.project_id = d.project_id OR d.transferred_project_id = p.project_id
            LEFT JOIN campaign c ON c.campaign_id = p.campaign_id
            LEFT JOIN wrong_donation wd ON wd.donation_id = d.donation_id
            WHERE a.account_id = :accountId
              AND (:title IS NULL OR p.title LIKE CONCAT('%', :title, '%'))
              AND (:campaign_id IS NULL OR c.campaign_id = :campaign_id)
              AND (:status IS NULL OR p.status = :status)
              AND (:province IS NULL OR p.province = :province)
              AND (:year IS NULL OR YEAR(p.created_at) = :year)
            GROUP BY p.project_id, p.title, p.code, p.created_at, p.status, p.amount_needed_to_raise,
                     c.campaign_id, c.title, p.address, p.ward, p.district, p.province, p.total_budget
            ORDER BY p.project_id DESC
            """, nativeQuery = true)
    Page<ProjectInterfaceDTO> findProjectsByAccountId(@Param("accountId") BigInteger accountId,
                                                      @Param("title") String title,
                                                      @Param("campaign_id") BigInteger campaignId,
                                                      @Param("year") String year,
                                                      @Param("province") String province,
                                                      @Param("status") Integer status,
                                                      Pageable pageable);

    @Query(value = """
            SELECT p.project_id AS projectId,
                   p.code,
                   p.amount_needed_to_raise AS goal,
                   p.status,
                   IFNULL((SUM(CASE WHEN d.transferred_project_id IS NULL AND wd.wrong_donation_id IS NULL THEN d.value ELSE 0 END)+
                   SUM(CASE WHEN d.transferred_project_id = p.project_id THEN d.value ELSE 0 END)),0) as totalDonation
            FROM project p
            LEFT JOIN donation d ON p.project_id = d.project_id OR d.transferred_project_id = p.project_id
            LEFT JOIN wrong_donation wd ON wd.donation_id = d.donation_id
            WHERE (:donationDescription IS NULL OR :donationDescription LIKE CONCAT('%', p.code, '%'))
              AND (:campaignId IS NULL OR p.campaign_id = :campaignId)
              AND (:status IS NULL OR p.status = :status)
            GROUP BY p.project_id, p.code, p.amount_needed_to_raise
            HAVING (:isAll OR p.amount_needed_to_raise > totalDonation)
            ORDER BY totalDonation ASC
            LIMIT 1
            """, nativeQuery = true)
    ProjectTransactionDTO findProjectByCampaignIdAndDonationDescriptionAndStatus(
            @Param("donationDescription") String donationDescription,
            @Param("campaignId") BigInteger campaignId,
            @Param("status") Integer status,
            @Param("isAll") Boolean isAll);

    @Query(value = """
            SELECT p.project_id AS projectId,
                   p.code,
                   p.amount_needed_to_raise AS goal,
                   IFNULL((SUM(CASE WHEN d.transferred_project_id IS NULL AND wd.wrong_donation_id IS NULL THEN d.value ELSE 0 END) +
                          SUM(CASE WHEN d.transferred_project_id = p.project_id THEN d.value ELSE 0 END)), 0) AS totalDonation
            FROM challenge ch
            LEFT JOIN challenge_project cp ON ch.challenge_id = cp.challenge_id
            LEFT JOIN project p ON p.project_id = cp.project_id
            LEFT JOIN donation d ON p.project_id = d.project_id OR d.transferred_project_id = p.project_id
            LEFT JOIN wrong_donation wd ON wd.donation_id = d.donation_id
            WHERE ch.challenge_id = :challengeId
              AND p.status = 2
              AND (:donationDescription IS NULL OR :donationDescription LIKE CONCAT('%', p.code, '%'))
            GROUP BY p.project_id, p.code, p.amount_needed_to_raise
            HAVING (:isAll OR p.amount_needed_to_raise > totalDonation)
            ORDER BY totalDonation ASC
            LIMIT 1
            """, nativeQuery = true)
    ProjectTransactionDTO findValidProjectByChallengeIdAndDescription(@Param("challengeId") BigInteger challengeId,
                                                                      @Param("donationDescription") String donationDescription,
                                                                      @Param("isAll") Boolean isAll);


    @Query(value = """
            SELECT p.project_id AS projectId,
                   IFNULL(SUM(CASE WHEN d.transferred_project_id IS NULL AND wd.wrong_donation_id IS NULL THEN d.value ELSE 0 END) +
                          SUM(CASE WHEN d.transferred_project_id = p.project_id THEN d.value ELSE 0 END), 0) AS totalDonation
            FROM project p
            LEFT JOIN donation d ON p.project_id = d.project_id OR d.transferred_project_id = p.project_id
            LEFT JOIN campaign c ON c.campaign_id = p.campaign_id
            LEFT JOIN wrong_donation wd ON wd.donation_id = d.donation_id
            WHERE p.project_id = :projectId
            GROUP BY p.project_id, p.title, p.code, p.created_at, p.status, p.amount_needed_to_raise,
                     c.campaign_id, c.title, p.address, p.ward, p.district, p.province, p.total_budget
            """, nativeQuery = true)
    ProjectInterfaceDTO getProjectDetailByProjectId(@Param("projectId") BigInteger projectId);

    @Query(value = """
            SELECT p.campaign_id, p.status, COUNT(p.project_id)
            FROM project p
            GROUP BY p.campaign_id, p.status
            """, nativeQuery = true)
    List<Object[]> countProjectsGroupedByCampaignAndStatus();

    /// for export file
    @Query(value = """
            SELECT COUNT(p.project_id)
            FROM project p
            WHERE p.campaign_id = :campaignId
              AND (:status IS NULL OR p.status = :status)
            """, nativeQuery = true)
    int countProjectsByCampaignIdAndStatus(@Param("campaignId") BigInteger campaignId, @Param("status") Integer status);


    @Query(value = """
            SELECT IFNULL(SUM(CASE WHEN d.transferred_project_id IS NULL THEN d.value ELSE 0 END) +
                          SUM(CASE WHEN d.transferred_project_id = p.project_id THEN d.value ELSE 0 END), 0) AS totalDonation
            FROM project p
            LEFT JOIN donation d ON p.project_id = d.project_id OR d.transferred_project_id = p.project_id
            WHERE p.campaign_id = :campaignId
              AND (:status IS NULL OR p.status = :status)
            """, nativeQuery = true)
    BigDecimal getTotalDonationByCampaignId(@Param("campaignId") BigInteger campaignId, @Param("status") Integer status);

    @Query(value = """
            SELECT IFNULL(SUM(p.total_budget), 0) AS totalBudget
            FROM project p
            WHERE p.campaign_id = :campaignId
              AND (:status IS NULL OR p.status = :status)
            """, nativeQuery = true)
    BigDecimal getTotalBudgetByCampaignIdAndStatus(@Param("campaignId") BigInteger campaignId, @Param("status") Integer status);


    @Query(value = """
            SELECT
                p.project_id AS projectId,
                p.amount_needed_to_raise AS target,
                IFNULL((
                    SUM(CASE
                        WHEN d.transferred_project_id IS NULL AND wd.wrong_donation_id IS NULL
                        THEN d.value
                        ELSE 0
                    END) +
                    SUM(CASE
                        WHEN d.transferred_project_id = p.project_id
                        THEN d.value
                        ELSE 0
                    END)
                ), 0) AS totalDonation
            FROM project p
            LEFT JOIN donation d
                ON p.project_id = d.project_id OR d.transferred_project_id = p.project_id
            LEFT JOIN wrong_donation wd
                ON wd.donation_id = d.donation_id
            WHERE p.project_id = :projectId
            GROUP BY p.project_id, p.code, p.amount_needed_to_raise
            """, nativeQuery = true)
    ProjectDonationInformattionDTO getDonationInformationTotal(@Param("projectId") BigInteger projectId);

    @Query(value = """
    SELECT COUNT(DISTINCT p.project_id) AS totalProjects,
           COUNT(DISTINCT CASE WHEN p.status = 2 THEN p.project_id ELSE NULL END) AS totalOnGoingProjects,
           COUNT(DISTINCT CASE WHEN p.status = 3 THEN p.project_id ELSE NULL END) AS totalProcessingProjects,
           COUNT(DISTINCT CASE WHEN p.status = 4 THEN p.project_id ELSE NULL END) AS totalDoneProjects
    FROM project p
    WHERE (:campaignId IS NULL OR p.campaign_id = :campaignId)
    AND YEAR(p.created_at) = :year
    """, nativeQuery = true)
    StatisticsInterfaceDTO getProjectStaticByCampaign(@Param("campaignId") BigInteger campaignId,
                                                      @Param("year") Integer year);


}

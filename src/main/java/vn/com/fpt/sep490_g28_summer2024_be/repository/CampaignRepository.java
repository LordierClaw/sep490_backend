package vn.com.fpt.sep490_g28_summer2024_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fpt.sep490_g28_summer2024_be.dto.chart.interfacedto.StatisticsByCampaignInterfaceDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.chart.interfacedto.StatisticsInterfaceDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Campaign;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign,BigInteger> {

    @Query(value = "SELECT COUNT(*) FROM campaign WHERE title = :title AND campaign_id != :id", nativeQuery = true)
    Long countCampaignByTitle(@Param("title") String title, @Param("id") BigInteger id);

    boolean existsByTitle(String title);

    @Query(value = """
    SELECT *
    FROM campaign
    WHERE (:title IS NULL OR title LIKE %:title%)
    AND (:is_active IS NULL OR is_active = :is_active)
    ORDER BY campaign_id DESC
    """, countQuery = """
    SELECT COUNT(*)
    FROM campaign
    WHERE (:title IS NULL OR title LIKE %:title%)
    AND (:is_active IS NULL OR is_active = :is_active)
    """, nativeQuery = true)
    Page<Campaign> findCampaignsByFilters(@Param("title") String title,
                                          @Param("is_active") Boolean is_active,
                                          Pageable pageable);


    Page<Campaign> findAll(Pageable pageable);

    @Query(value = """
    SELECT c.campaign_id AS id, 
           c.title AS label, 
           IFNULL(SUM(d.value), 0) AS value
    FROM donation d
    LEFT JOIN project p ON (d.project_id = p.project_id AND d.transferred_project_id IS NULL)
                       OR d.transferred_project_id = p.project_id
    LEFT JOIN campaign c ON c.campaign_id = p.campaign_id
    WHERE YEAR(d.created_at) = :year
      AND MONTH(d.created_at) = :month
    GROUP BY c.campaign_id, c.title;
    """, nativeQuery = true)
    List<StatisticsByCampaignInterfaceDTO> getCampaignPieChart(@Param("month") Integer month,
                                                               @Param("year") Integer year);


    @Query(value = """
    SELECT IFNULL(SUM(d.value), 0) AS totalDonation,
           COUNT(DISTINCT d.donation_id) AS totalNumberDonations
    FROM donation d
    LEFT JOIN project p ON (d.project_id = p.project_id AND d.transferred_project_id IS NULL)
    				   OR d.transferred_project_id = p.project_id
    LEFT JOIN campaign c ON c.campaign_id = p.campaign_id
    WHERE (:campaignId IS NULL OR c.campaign_id = :campaignId)
    AND YEAR(d.created_at) = :year
    """, nativeQuery = true)
    StatisticsInterfaceDTO getDonationStaticByCampaign(@Param("campaignId") BigInteger campaignId,
                                                       @Param("year") Integer year);


}

package vn.com.fpt.sep490_g28_summer2024_be.unittest.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import vn.com.fpt.sep490_g28_summer2024_be.dto.chart.interfacedto.StatisticsInterfaceDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Campaign;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Donation;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Project;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseServiceImpl;
import vn.com.fpt.sep490_g28_summer2024_be.repository.CampaignRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.DonationRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.SponsorRepository;
import vn.com.fpt.sep490_g28_summer2024_be.service.statistics.StatisticsService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
@MockBean(FirebaseServiceImpl.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StatisticsServiceTest {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private SponsorRepository sponsorRepository;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Clear relevant tables before each test
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE sponsor").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE donation").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE wrong_donation").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE project").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE campaign").executeUpdate();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    @Test
    @DisplayName("DSS_getTotalSponsorValue_01")
    void getTotalSponsorValue_shouldReturnTotalValue_whenSponsorsExist() {
        // Arrange
        // Insert test sponsors with different values for month 4, year 2025
        entityManager.createNativeQuery("""
            INSERT INTO sponsor (value, created_at)
            VALUES 
            (1000000, '2025-04-01'),
            (2000000, '2025-04-15'),
            (3000000, '2025-04-30')
        """).executeUpdate();

        // Insert sponsors for different months/years (should not be included)
        entityManager.createNativeQuery("""
            INSERT INTO sponsor (value, created_at)
            VALUES 
            (500000, '2025-03-31'),
            (500000, '2025-05-01')
        """).executeUpdate();

        // Act
        BigDecimal totalValue = statisticsService.getTotalSponsorValue(4, 2025);

        // Assert
        assertNotNull(totalValue, "Total value should not be null");
        assertEquals(new BigDecimal("6000000.00"), totalValue, "Total value should be 6,000,000 (sum of sponsors in April 2025)");
    }

    @Test
    @DisplayName("DSS_getTotalDonation_01")
    void getTotalDonation_shouldReturnTotalValue_whenDonationsExist() {
        // Arrange
        // Insert test donations with different values for month 4, year 2025
        entityManager.createNativeQuery("""
            INSERT INTO donation (value, created_at)
            VALUES 
            (1000000, '2025-04-01'),
            (2000000, '2025-04-15'),
            (3000000, '2025-04-30')
        """).executeUpdate();

        // Insert donations for different months/years (should not be included)
        entityManager.createNativeQuery("""
            INSERT INTO donation (value, created_at)
            VALUES 
            (500000, '2025-03-31'),
            (500000, '2025-05-01')
        """).executeUpdate();

        // Act
        BigDecimal totalValue = statisticsService.getTotalDonation(4, 2025);

        // Assert
        assertNotNull(totalValue, "Total donation value should not be null");
        assertEquals(new BigDecimal("6000000.00"), totalValue, "Total donation value should be 6,000,000 (sum of donations in April 2025)");
    }

    @Test
    @DisplayName("DSS_getTotalWrongDonation_01")
    void getTotalWrongDonation_shouldReturnTotalValue_whenWrongDonationsExist() {
        // Arrange
        // Insert test donations first
        entityManager.createNativeQuery("""
            INSERT INTO donation (donation_id, value, created_at)
            VALUES 
            (1, 1000000, '2025-04-01'),
            (2, 2000000, '2025-04-15'),
            (3, 3000000, '2025-04-30'),
            (4, 500000, '2025-03-31'),
            (5, 500000, '2025-05-01'),
            (6, 1000000, '2025-04-10')
        """).executeUpdate();

        // Insert wrong_donation records for April 2025 donations
        entityManager.createNativeQuery("""
            INSERT INTO wrong_donation (donation_id)
            VALUES 
            (1), -- April 1st donation
            (2), -- April 15th donation
            (3)  -- April 30th donation
        """).executeUpdate();

        // Act
        BigDecimal totalValue = statisticsService.getTotalWrongDonation(4, 2025);

        // Assert
        assertNotNull(totalValue, "Total wrong donation value should not be null");
        assertEquals(new BigDecimal("6000000.00"), totalValue, "Total wrong donation value should be 6,000,000 (sum of wrong donations in April 2025)");
    }

    @Test
    @DisplayName("DSS_getTotalCountDonations_01")
    void getTotalCountDonations_shouldReturnTotalCount_whenDonationsExist() {
        // Arrange
        // Insert test donations for month 4, year 2025
        entityManager.createNativeQuery("""
            INSERT INTO donation (value, created_at)
            VALUES 
            (1000000, '2025-04-01'),
            (2000000, '2025-04-15'),
            (3000000, '2025-04-30')
        """).executeUpdate();

        // Insert donations for different months/years (should not be included)
        entityManager.createNativeQuery("""
            INSERT INTO donation (value, created_at)
            VALUES 
            (500000, '2025-03-31'),
            (500000, '2025-05-01')
        """).executeUpdate();

        // Act
        Long totalCount = statisticsService.getTotalCountDonations(4, 2025);

        // Assert
        assertNotNull(totalCount, "Total count should not be null");
        assertEquals(3L, totalCount, "Total count should be 3 (number of donations in April 2025)");
    }

    @Test
    @DisplayName("DSS_getDataPieChart_01")
    void getDataPieChart_shouldReturnCampaignData_whenDataExists() {
        // Arrange
        // First, create campaigns
        entityManager.createNativeQuery("""
            INSERT INTO campaign (campaign_id, title, is_active, created_at)
            VALUES 
            (1, 'Campaign 1', true, '2025-04-01'),
            (2, 'Campaign 2', true, '2025-04-01')
        """).executeUpdate();

        // Create projects for these campaigns
        entityManager.createNativeQuery("""
            INSERT INTO project (project_id, campaign_id, title, status, created_at)
            VALUES 
            (1, 1, 'Project 1', 1, '2025-04-01'),
            (2, 1, 'Project 2', 1, '2025-04-01'),
            (3, 2, 'Project 3', 1, '2025-04-01')
        """).   executeUpdate();

        // Insert donations for the projects
        entityManager.createNativeQuery("""
            INSERT INTO donation (project_id, value, created_at)
            VALUES 
            (1, 1000000, '2025-04-01'),
            (1, 2000000, '2025-04-15'),
            (2, 3000000, '2025-04-30'),
            (3, 4000000, '2025-04-01')
        """).executeUpdate();

        // Act
        var result = statisticsService.getDataPieChart(4, 2025);

        // Assert
        assertNotNull(result, "Pie chart data should not be null");
        assertEquals(2, result.size(), "Should return data for 2 campaigns");
        
        // Find and verify Campaign 1's data (total 6,000,000)
        var campaign1Data = result.stream()
            .filter(data -> data.getId().equals(BigInteger.valueOf(1)))
            .findFirst()
            .orElse(null);
        assertNotNull(campaign1Data, "Campaign 1 data should exist");
        assertEquals(new BigDecimal("6000000.00"), campaign1Data.getValue(), "Campaign 1 should have total value of 6,000,000");
        assertEquals("Campaign 1", campaign1Data.getLabel(), "Campaign 1 should have correct label");

        // Find and verify Campaign 2's data (total 4,000,000)
        var campaign2Data = result.stream()
            .filter(data -> data.getId().equals(BigInteger.valueOf(2)))
            .findFirst()
            .orElse(null);
        assertNotNull(campaign2Data, "Campaign 2 data should exist");
        assertEquals(new BigDecimal("4000000.00"), campaign2Data.getValue(), "Campaign 2 should have total value of 4,000,000");
        assertEquals("Campaign 2", campaign2Data.getLabel(), "Campaign 2 should have correct label");
    }

    @Test
    @DisplayName("DSS_getDataLineChart_01")
    void getDataLineChart_shouldReturnMonthlyData_whenDonationsExist() {
        // Arrange
        // Insert donations for all months in 2025
        entityManager.createNativeQuery("""
            INSERT INTO donation (value, created_at)
            VALUES 
            -- January 2025
            (1000000, '2025-01-15'),
            (2000000, '2025-01-20'),
            -- February 2025
            (1500000, '2025-02-10'),
            -- March 2025
            (3000000, '2025-03-10'),
            -- April 2025
            (4000000, '2025-04-01'),
            (5000000, '2025-04-15'),
            -- May 2025
            (2500000, '2025-05-05'),
            -- June 2025
            (3500000, '2025-06-15'),
            -- July 2025
            (4500000, '2025-07-20'),
            -- August 2025
            (3200000, '2025-08-08'),
            -- September 2025
            (2800000, '2025-09-12'),
            -- October 2025
            (3800000, '2025-10-25'),
            -- November 2025
            (4200000, '2025-11-30'),
            -- December 2025
            (6000000, '2025-12-25'),
            -- Other years (should not be included)
            (1000000, '2024-12-31'),
            (1000000, '2026-01-01')
        """).executeUpdate();

        // Act
        var result = statisticsService.getDataLineChart(2025);

        // Assert
        assertNotNull(result, "Line chart data should not be null");
        
        // Should have data for all months (12 entries)
        assertEquals(12, result.size(), "Should return data for all 12 months");

        // Verify each month's data
        var monthlyExpectedValues = new BigDecimal[] {
            new BigDecimal("3000000.00"),  // January
            new BigDecimal("1500000.00"),  // February
            new BigDecimal("3000000.00"),  // March
            new BigDecimal("9000000.00"),  // April
            new BigDecimal("2500000.00"),  // May
            new BigDecimal("3500000.00"),  // June
            new BigDecimal("4500000.00"),  // July
            new BigDecimal("3200000.00"),  // August
            new BigDecimal("2800000.00"),  // September
            new BigDecimal("3800000.00"),  // October
            new BigDecimal("4200000.00"),  // November
            new BigDecimal("6000000.00")   // December
        };

        for (int i = 0; i < 12; i++) {
            final int monthNumber = i + 1;
            var monthData = result.stream()
                .filter(data -> data.getLabel().equals("Tháng " + monthNumber))
                .findFirst()
                .orElse(null);
            
            assertNotNull(monthData, "Month " + monthNumber + " data should exist");
            assertEquals(monthlyExpectedValues[i], monthData.getValue(), 
                "Month " + monthNumber + " should have correct total donations");
        }
    }

    @Test
    @DisplayName("DSS_getDataBarChart_01")
    void getDataBarChart_shouldReturnWeeklyData_whenDonationsExist() {
        // Arrange
        // Insert donations for different weeks in April 2025
        entityManager.createNativeQuery("""
            INSERT INTO donation (value, created_at)
            VALUES 
            -- Week 1 (April 1-7)
            (1000000, '2025-04-01'),
            (2000000, '2025-04-03'),
            (1500000, '2025-04-07'),
            -- Week 2 (April 8-14)
            (3000000, '2025-04-10'),
            (2000000, '2025-04-12'),
            -- Week 3 (April 15-21)
            (4000000, '2025-04-15'),
            (2500000, '2025-04-20'),
            -- Week 4 (April 22-28)
            (3000000, '2025-04-25'),
            -- Week 5 (April 29-30)
            (2000000, '2025-04-30'),
            -- Other months (should not be included)
            (1000000, '2025-03-31'),
            (1000000, '2025-05-01')
        """).executeUpdate();

        // Act
        var result = statisticsService.getDataBarChart(4, 2025);

        // Assert
        assertNotNull(result, "Bar chart data should not be null");
        assertEquals(5, result.size(), "Should return data for all 5 weeks of April 2025");

        // Verify each week's data
        var weeklyExpectedValues = new BigDecimal[] {
            new BigDecimal("4500000.00"),  // Week 1 (1M + 2M + 1.5M)
            new BigDecimal("5000000.00"),  // Week 2 (3M + 2M)
            new BigDecimal("6500000.00"),  // Week 3 (4M + 2.5M)
            new BigDecimal("3000000.00"),  // Week 4 (3M)
            new BigDecimal("2000000.00")   // Week 5 (2M)
        };

        for (int i = 0; i < 5; i++) {
            final int weekNumber = i + 1;
            var weekData = result.stream()
                .filter(data -> data.getLabel().equals("Tuần " + weekNumber))
                .findFirst()
                .orElse(null);
            
            assertNotNull(weekData, "Week " + weekNumber + " data should exist");
            assertEquals(weeklyExpectedValues[i], weekData.getValue(), 
                "Week " + weekNumber + " should have correct total donations");
        }
    }

    @Test
    @DisplayName("DSS_getDonationStaticDataByCampaign_01")
    void getDonationStaticDataByCampaign_shouldReturnCorrectData() {
        // Arrange
        // Create a campaign
        Campaign campaign = Campaign.builder()
                .title("Test Campaign")
                .slug("test-campaign")
                .description("Test Description")
                .thumbnail("thumbnail.jpg")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaign = campaignRepository.save(campaign);

        // Create projects for the campaign
        Project project1 = Project.builder()
                .title("Project 1")
                .campaign(campaign)
                .status(1)
                .code("PRJ001")
                .slug("project-1")
                .ward("Ward 1")
                .district("District 1")
                .province("Province 1")
                .totalBudget(BigDecimal.valueOf(1000000))
                .amountNeededToRaise(BigDecimal.valueOf(1000000))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .background("background1.jpg")
                .address("Address 1")
                .build();

        Project project2 = Project.builder()
                .title("Project 2")
                .campaign(campaign)
                .status(2)
                .code("PRJ002")
                .slug("project-2")
                .ward("Ward 2")
                .district("District 2")
                .province("Province 2")
                .totalBudget(BigDecimal.valueOf(2000000))
                .amountNeededToRaise(BigDecimal.valueOf(2000000))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .background("background2.jpg")
                .address("Address 2")
                .build();

        Project project3 = Project.builder()
                .title("Project 3")
                .campaign(campaign)
                .status(3)
                .code("PRJ003")
                .slug("project-3")
                .ward("Ward 3")
                .district("District 3")
                .province("Province 3")
                .totalBudget(BigDecimal.valueOf(3000000))
                .amountNeededToRaise(BigDecimal.valueOf(3000000))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .background("background3.jpg")
                .address("Address 3")
                .build();

        Project project4 = Project.builder()
                .title("Project 4")
                .campaign(campaign)
                .status(4)
                .code("PRJ004")
                .slug("project-4")
                .ward("Ward 4")
                .district("District 4")
                .province("Province 4")
                .totalBudget(BigDecimal.valueOf(4000000))
                .amountNeededToRaise(BigDecimal.valueOf(4000000))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .background("background4.jpg")
                .address("Address 4")
                .build();

        projectRepository.saveAll(Arrays.asList(project1, project2, project3, project4));

        // Create donations for each project
        // For project 1 (status 1)
        Donation donation1 = Donation.builder()
                .project(project1)
                .value(BigDecimal.valueOf(1000000))
                .bankSubAccId("123")
                .bankName("Bank A")
                .corresponsiveAccount("456")
                .corresponsiveName("User A")
                .corresponsiveBankId("789")
                .corresponsiveBankName("Bank B")
                .tid("TID001")
                .description("Donation for Project 1")
                .note("Test donation 1")
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();

        // For project 2 (status 2)
        Donation donation2 = Donation.builder()
                .project(project2)
                .value(BigDecimal.valueOf(2000000))
                .bankSubAccId("234")
                .bankName("Bank C")
                .corresponsiveAccount("567")
                .corresponsiveName("User B")
                .corresponsiveBankId("890")
                .corresponsiveBankName("Bank D")
                .tid("TID002")
                .description("Donation for Project 2")
                .note("Test donation 2")
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();

        // For project 3 (status 3)
        Donation donation3 = Donation.builder()
                .project(project3)
                .value(BigDecimal.valueOf(3000000))
                .bankSubAccId("345")
                .bankName("Bank E")
                .corresponsiveAccount("678")
                .corresponsiveName("User C")
                .corresponsiveBankId("901")
                .corresponsiveBankName("Bank F")
                .tid("TID003")
                .description("Donation for Project 3")
                .note("Test donation 3")
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();

        // For project 4 (status 4)
        Donation donation4 = Donation.builder()
                .project(project4)
                .value(BigDecimal.valueOf(4000000))
                .bankSubAccId("456")
                .bankName("Bank G")
                .corresponsiveAccount("789")
                .corresponsiveName("User D")
                .corresponsiveBankId("012")
                .corresponsiveBankName("Bank H")
                .tid("TID004")
                .description("Donation for Project 4")
                .note("Test donation 4")
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();

        donationRepository.saveAll(Arrays.asList(donation1, donation2, donation3, donation4));

        // Act
        StatisticsInterfaceDTO result = statisticsService.getDonationStaticDataByCampaign(campaign.getCampaignId(), 2025);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(new BigDecimal("10000000.00"), result.getTotalDonation(), "Total donations should be 10,000,000");
        assertEquals(4L, result.getTotalNumberDonations(), "Total number of donations should be 4");
    }

    @Test
    @DisplayName("DSS_getProjectStaticDataByCampaign_01")
    void getProjectStaticDataByCampaign_shouldReturnStatistics_whenCampaignExists() {
        // Arrange
        // Create a campaign
        entityManager.createNativeQuery("""
            INSERT INTO campaign (campaign_id, title, slug, description, is_active, created_at, updated_at)
            VALUES (1, 'Test Campaign', 'test-campaign', 'Test Description', true, '2025-01-01', '2025-01-01')
        """).executeUpdate();

        // Create projects for the campaign with different statuses
        entityManager.createNativeQuery("""
            INSERT INTO project (
                project_id, campaign_id, code, title, slug,
                ward, district, province, status,
                total_budget, amount_needed_to_raise,
                created_at, updated_at, background
            )
            VALUES 
            -- 2 projects with status 2 (ongoing)
            (1, 1, 'PRJ001', 'Project 1', 'project-1',
             'Ward 1', 'District 1', 'Province 1', 2,
             1000000.00, 1000000.00,
             '2025-01-01', '2025-01-01', 'Background 1'),
            (2, 1, 'PRJ002', 'Project 2', 'project-2',
             'Ward 2', 'District 2', 'Province 2', 2,
             2000000.00, 2000000.00,
             '2025-02-01', '2025-02-01', 'Background 2'),
            -- 1 project with status 3 (processing)
            (3, 1, 'PRJ003', 'Project 3', 'project-3',
             'Ward 3', 'District 3', 'Province 3', 3,
             3000000.00, 3000000.00,
             '2025-03-01', '2025-03-01', 'Background 3'),
            -- 1 project with status 4 (done)
            (4, 1, 'PRJ004', 'Project 4', 'project-4',
             'Ward 4', 'District 4', 'Province 4', 4,
             4000000.00, 4000000.00,
             '2025-04-01', '2025-04-01', 'Background 4')
        """).executeUpdate();

        // Act
        var result = statisticsService.getProjectStaticDataByCampaign(BigInteger.valueOf(1), 2025);

        // Assert
        assertNotNull(result, "Statistics data should not be null");
        
        // Verify project counts
        assertEquals(4L, result.getTotalProjects(), 
            "Should have 4 total projects");
        assertEquals(2L, result.getTotalOnGoingProjects(), 
            "Should have 2 ongoing projects (status = 2)");
        assertEquals(1L, result.getTotalProcessingProjects(), 
            "Should have 1 processing project (status = 3)");
        assertEquals(1L, result.getTotalDoneProjects(), 
            "Should have 1 done project (status = 4)");
    }
}

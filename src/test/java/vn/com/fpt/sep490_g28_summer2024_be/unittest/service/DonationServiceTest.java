package vn.com.fpt.sep490_g28_summer2024_be.unittest.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.ChallengeResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.donation.DonationResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.interfacedto.ProjectDonationInformattionDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Campaign;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Challenge;
import vn.com.fpt.sep490_g28_summer2024_be.entity.ChallengeProject;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Donation;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Project;
import vn.com.fpt.sep490_g28_summer2024_be.entity.WrongDonation;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.CampaignRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ChallengeRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.DonationRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.WrongDonationRepository;
import vn.com.fpt.sep490_g28_summer2024_be.service.donation.DefaultDonationService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class DonationServiceTest {

    @Autowired
    private DonationRepository donationRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private ChallengeRepository challengeRepository;
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Autowired
    private WrongDonationRepository wrongDonationRepository;
    
    @Autowired
    private DefaultDonationService defaultDonationService;
    
    @Autowired
    private EntityManager entityManager;

    private Account account;
    private Campaign campaign;
    private Project project;
    private Challenge challenge;
    private Donation donation;
    private WrongDonation wrongDonation;

    @BeforeEach
    void setUp() {
        // Clean up data before each test
        wrongDonationRepository.deleteAll();
        donationRepository.deleteAll();
        challengeRepository.deleteAll();
        projectRepository.deleteAll();
        campaignRepository.deleteAll();
        accountRepository.deleteAll();
        
        // Create test data
        account = accountRepository.save(Account.builder()
                .email("test@example.com")
                .fullname("Test User")
                .password("password")
                .code("TEST001")
                .build());
                
        campaign = campaignRepository.save(Campaign.builder()
                .title("Test Campaign")
                .description("Test Campaign Description")
                .isActive(true)
                .createdAt(LocalDate.now())
                .build());
                
        project = projectRepository.save(Project.builder()
                .title("Test Project")
                .code("PROJ001")
                .status(2)
                .campaign(campaign)
                .ward("Test Ward")
                .district("Test District")
                .province("Test Province")
                .slug("test-project")
                .amountNeededToRaise(BigDecimal.valueOf(100000))
                .totalBudget(BigDecimal.valueOf(50000))
                .createdAt(LocalDateTime.now())
                .build());
                
        challenge = challengeRepository.save(Challenge.builder()
                .title("Test Challenge")
                .challengeCode("CHAL001")
                .content("Test Challenge Description")
                .createdAt(LocalDateTime.now())
                .goal(BigDecimal.valueOf(100000))
                .finishedAt(LocalDate.now().plusDays(30))
                .build());
        
        // Manually create ChallengeProject relation between Challenge and Project
        ChallengeProject challengeProject = new ChallengeProject();
        challengeProject.setChallenge(challenge);
        challengeProject.setProject(project);
        entityManager.persist(challengeProject);
                
        donation = donationRepository.save(Donation.builder()
                .tid("TID001")
                .project(project)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(50000))
                .description("Test Donation")
                .bankSubAccId("123456")
                .bankName("Test Bank")
                .corresponsiveName("Test Corresponsive")
                .corresponsiveAccount("987654321")
                .corresponsiveBankId("BID001")
                .corresponsiveBankName("Test Corresponsive Bank")
                .note("Test Note")
                .build());
    }

    @Test
    @DisplayName("DS_viewListDonations_01")
    void viewListDonations_shouldReturnDonationsForProject() {
        // Test viewListDonations with valid project ID
        Integer page = 0;
        Integer size = 10;
        String description = "";
        
        PageResponse<DonationResponseDTO> response = defaultDonationService.viewListDonations(
                page, size, project.getProjectId(), description);
        
        assertNotNull(response);
        assertEquals(size, response.getLimit());
        assertEquals(page, response.getOffset());
        assertTrue(response.getTotal() > 0);
        assertFalse(response.getContent().isEmpty());
        assertEquals(donation.getDonationId(), response.getContent().get(0).getDonationId());
        assertNotNull(response.getSummary());
        assertTrue(response.getSummary().containsKey("target"));
        assertTrue(response.getSummary().containsKey("total_donation"));
    }

    @Test
    @DisplayName("DS_viewListDonations_02")
    void viewListDonations_shouldFilterByDescription() {
        // Test viewListDonations with description filter
        Integer page = 0;
        Integer size = 10;
        String description = "Test";
        
        PageResponse<DonationResponseDTO> response = defaultDonationService.viewListDonations(
                page, size, project.getProjectId(), description);
        
        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
        
        // Test with non-matching description
        description = "NonExistent";
        response = defaultDonationService.viewListDonations(
                page, size, project.getProjectId(), description);
        
        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    @DisplayName("DS_viewListDonationsAdmin_01")
    void viewListDonationsAdmin_shouldReturnDonations() {
        // Test viewListDonationsAdmin
        Integer page = 0;
        Integer size = 10;
        String description = "";
        
        PageResponse<DonationResponseDTO> response = defaultDonationService.viewListDonationsAdmin(
                page, size, project.getProjectId(), description);
        
        assertNotNull(response);
        assertEquals(size, response.getLimit());
        assertEquals(page, response.getOffset());
        assertTrue(response.getTotal() > 0);
        assertFalse(response.getContent().isEmpty());
        assertEquals(donation.getDonationId(), response.getContent().get(0).getDonationId());
    }

    @Test
    @DisplayName("DS_viewDonationsByChallengeId_01")
    void viewDonationsByChallengeId_shouldReturnDonationsForChallenge() {
        // Add donation with challenge
        Donation challengeDonation = donationRepository.save(Donation.builder()
                .tid("CHTID001")
                .project(project)
                .challenge(challenge)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(75000))
                .description("Challenge Donation")
                .build());
        
        Integer page = 0;
        Integer size = 10;
        String description = "";
        
        PageResponse<DonationResponseDTO> response = defaultDonationService.viewDonationsByChallengeId(
                page, size, challenge.getChallengeId(), description);
        
        assertNotNull(response);
        assertEquals(size, response.getLimit());
        assertEquals(page, response.getOffset());
        assertTrue(response.getTotal() > 0);
        assertFalse(response.getContent().isEmpty());
        assertEquals(challengeDonation.getDonationId(), response.getContent().get(0).getDonationId());
        assertNotNull(response.getSummary());
        assertTrue(response.getSummary().containsKey("total_donation"));
    }

    @Test
    @DisplayName("DS_viewDonationsByChallengeId_02")
    void viewDonationsByChallengeId_shouldFilterByDescription() {
        // Tạo một donation với challenge và mô tả "Thử thách"
        Donation challengeDonation1 = donationRepository.save(Donation.builder()
                .tid("CHTID002")
                .project(project)
                .challenge(challenge)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(75000))
                .description("Thử thách")
                .build());
                
        // Tạo một donation khác với challenge nhưng có mô tả khác
        Donation challengeDonation2 = donationRepository.save(Donation.builder()
                .tid("CHTID003")
                .project(project)
                .challenge(challenge)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(45000))
                .description("Challenge Donation Other")
                .build());
        
        Integer page = 0;
        Integer size = 10;
        String description = "Thử thách";
        
        // Kiểm tra với description="Thử thách"
        PageResponse<DonationResponseDTO> response = defaultDonationService.viewDonationsByChallengeId(
                page, size, challenge.getChallengeId(), description);
        
        assertNotNull(response);
        assertEquals(size, response.getLimit());
        assertEquals(page, response.getOffset());
        assertFalse(response.getContent().isEmpty());
        assertEquals(1, response.getContent().size()); // Chỉ nên có 1 kết quả khớp
        assertEquals(challengeDonation1.getDonationId(), response.getContent().get(0).getDonationId());
        assertEquals("Thử thách", response.getContent().get(0).getDescription());
        assertNotNull(response.getSummary());
        assertTrue(response.getSummary().containsKey("total_donation"));
        
        // Kiểm tra với description không tồn tại
        String nonExistentDescription = "Không tồn tại";
        PageResponse<DonationResponseDTO> emptyResponse = defaultDonationService.viewDonationsByChallengeId(
                page, size, challenge.getChallengeId(), nonExistentDescription);
        
        assertNotNull(emptyResponse);
        assertTrue(emptyResponse.getContent().isEmpty());
        assertEquals(0, emptyResponse.getTotal());
    }

    @Test
    @DisplayName("DS_viewDonationsByAccount_01")
    void viewDonationsByAccount_shouldReturnDonationsForAccount() {
        Integer page = 0;
        Integer size = 10;
        String description = "";
        
        PageResponse<DonationResponseDTO> response = defaultDonationService.viewDonationsByAccount(
                page, size, account.getEmail(), description);
        
        assertNotNull(response);
        assertEquals(size, response.getLimit());
        assertEquals(page, response.getOffset());
        assertTrue(response.getTotal() > 0);
        assertFalse(response.getContent().isEmpty());
        assertEquals(donation.getDonationId(), response.getContent().get(0).getDonationId());
    }

    @Test
    @DisplayName("DS_viewDonationsByAccount_02")
    void viewDonationsByAccount_shouldThrowExceptionForInvalidAccount() {
        Integer page = 0;
        Integer size = 10;
        String description = "";
        
        Exception exception = assertThrows(AppException.class, () -> {
            defaultDonationService.viewDonationsByAccount(page, size, "nonexistent@example.com", description);
        });
        
        AppException appException = (AppException) exception;
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, appException.getErrorCode());
    }

    @Test
    @DisplayName("DS_viewDonationsByReferCode_01")
    void viewDonationsByReferCode_shouldReturnDonationsForReferCode() {
        // Add donation with refer
        Donation referDonation = donationRepository.save(Donation.builder()
                .tid("REFTID001")
                .project(project)
                .refer(account)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(25000))
                .description("Refer Donation")
                .build());
        
        Integer page = 0;
        Integer size = 10;
        
        PageResponse<?> response = defaultDonationService.viewDonationsByReferCode(
                page, size, account.getCode());
        
        assertNotNull(response);
        assertEquals(size, response.getLimit());
        assertEquals(page, response.getOffset());
        assertTrue(response.getTotal() > 0);
        assertFalse(response.getContent().isEmpty());
        assertNotNull(response.getSummary());
        assertTrue(response.getSummary().containsKey("total_donation_by_refer"));
    }

    @Test
    @DisplayName("DS_viewDonationsByReferCode_02")
    void viewDonationsByReferCode_shouldThrowExceptionForInvalidReferCode() {
        Integer page = 0;
        Integer size = 10;
        
        Exception exception = assertThrows(AppException.class, () -> {
            defaultDonationService.viewDonationsByReferCode(page, size, "INVALID_CODE");
        });
        
        AppException appException = (AppException) exception;
        assertEquals(ErrorCode.ACCOUNT_NO_CONTENT, appException.getErrorCode());
    }

    @Test
    @DisplayName("DS_viewAllDonations_01")
    void viewAllDonations_shouldReturnAllDonations() {
        Integer page = 0;
        Integer size = 10;
        String description = "";
        
        PageResponse<?> response = defaultDonationService.viewAllDonations(
                page, size, description);
        
        assertNotNull(response);
        assertEquals(size, response.getLimit());
        assertEquals(page, response.getOffset());
        assertTrue(response.getTotal() > 0);
        assertFalse(response.getContent().isEmpty());
    }

    @Test
    @DisplayName("DS_viewAllDonations_02")
    void viewAllDonations_shouldFilterByDescription() {
        Integer page = 0;
        Integer size = 10;
        String description = "Test";
        
        PageResponse<?> response = defaultDonationService.viewAllDonations(
                page, size, description);
        
        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
        
        // Test with non-matching description
        description = "NonExistent";
        response = defaultDonationService.viewAllDonations(
                page, size, description);
        
        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    @DisplayName("DS_viewListDonations_03")
    void viewListDonations_shouldHandleWrongDonation() {
        // Create a donation with WrongDonation
        wrongDonation = wrongDonationRepository.save(WrongDonation.builder()
                .donation(donation)
                .build());
        donation.setWrongDonation(wrongDonation);
        donation = donationRepository.save(donation);
        
        Integer page = 0;
        Integer size = 10;
        String description = "";
        
        PageResponse<DonationResponseDTO> response = defaultDonationService.viewListDonations(
                page, size, project.getProjectId(), description);
        
        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
        assertEquals("Pending", response.getContent().get(0).getStatus());
    }

    @Test
    @DisplayName("DS_viewListDonationsAdmin_02")
    void viewListDonationsAdmin_shouldHandleWrongDonation() {
        // Create a donation with WrongDonation
        wrongDonation = wrongDonationRepository.save(WrongDonation.builder()
                .donation(donation)
                .build());
        donation.setWrongDonation(wrongDonation);
        donation = donationRepository.save(donation);
        
        Integer page = 0;
        Integer size = 10;
        String description = "";
        
        PageResponse<DonationResponseDTO> response = defaultDonationService.viewListDonationsAdmin(
                page, size, project.getProjectId(), description);
        
        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
        assertEquals("Pending", response.getContent().get(0).getStatus());
    }
} 
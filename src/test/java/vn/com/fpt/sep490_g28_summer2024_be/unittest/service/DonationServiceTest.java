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
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

        // Xóa bảng con trước
        entityManager.createNativeQuery("DELETE FROM challenge_project").executeUpdate();

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
    @DisplayName("DS_viewListDonations_04")
    void viewListDonations_shouldHandleEmptyProject() {
        // Test case khi truyền vào project ID không có donation nào
        Project emptyProject = projectRepository.save(Project.builder()
                .title("Empty Project")
                .code("EMPTY001")
                .status(2)
                .campaign(campaign)
                .ward("Test Ward")
                .district("Test District")
                .province("Test Province")
                .slug("empty-project")
                .amountNeededToRaise(BigDecimal.valueOf(100000))
                .totalBudget(BigDecimal.valueOf(50000))
                .createdAt(LocalDateTime.now())
                .build());

        PageResponse<DonationResponseDTO> response = defaultDonationService.viewListDonations(
                0, 10, emptyProject.getProjectId(), "");

        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotal());
    }

    @Test
    @DisplayName("DS_viewListDonations_05")
    void viewListDonations_shouldHandlePagination() {
        // Tạo nhiều donations để test phân trang
        for (int i = 0; i < 15; i++) {
            donationRepository.save(Donation.builder()
                    .tid("TID" + i)
                    .project(project)
                    .createdBy(account)
                    .createdAt(LocalDateTime.now())
                    .value(BigDecimal.valueOf(1000 * (i + 1)))
                    .description("Donation " + i)
                    .build());
        }

        // Test trang đầu tiên
        PageResponse<DonationResponseDTO> page1 = defaultDonationService.viewListDonations(
                0, 10, project.getProjectId(), "");

        assertEquals(10, page1.getContent().size());
        assertTrue(page1.getTotal() > 10);

        // Test trang thứ hai
        PageResponse<DonationResponseDTO> page2 = defaultDonationService.viewListDonations(
                1, 10, project.getProjectId(), "");

        assertFalse(page2.getContent().isEmpty());
        assertTrue(page2.getContent().size() > 0);
    }

    @Test
    @DisplayName("DS_viewListDonations_06")
    void viewListDonations_shouldHandleProjectNotFound() {
        // Mock repositories
        ProjectRepository mockProjectRepo = mock(ProjectRepository.class);
        DonationRepository mockDonationRepo = mock(DonationRepository.class);

        DefaultDonationService mockedService = new DefaultDonationService(
                mockDonationRepo, accountRepository, mockProjectRepo);

        // Giả lập getDonationInformationTotal trả về null
        when(mockProjectRepo.getDonationInformationTotal(any()))
                .thenReturn(null);

        // Giả lập một trang trống
        Page<Donation> emptyPage = new PageImpl<>(Collections.emptyList());
        when(mockDonationRepo.findDonationsByFilters(any(), any(), any()))
                .thenReturn(emptyPage);

        // Thực thi test
        PageResponse<DonationResponseDTO> response = mockedService.viewListDonations(
                0, 10, BigInteger.valueOf(999999), "");

        // Kiểm tra
        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertNotNull(response.getSummary());
        assertTrue(response.getSummary().isEmpty() || response.getSummary().containsKey("target"));
    }

    @Test
    @DisplayName("DS_viewListDonations_07")
    void viewListDonations_shouldHandleAllNullReferences() {
        // Tạo donation với tất cả reference là null
        Donation nullRefDonation = Donation.builder()
                .tid("NULLREF001")
                .project(null)  // null reference
                .transferredProject(null)  // null reference
                .refer(null)  // null reference
                .challenge(null)  // null reference
                .createdBy(null)  // null reference
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(3000))
                .description("Null References")
                .build();

        // Mock repositories
        DonationRepository mockDonationRepo = mock(DonationRepository.class);
        ProjectRepository mockProjectRepo = mock(ProjectRepository.class);

        DefaultDonationService mockedService = new DefaultDonationService(
                mockDonationRepo, accountRepository, mockProjectRepo);

        // Mock project info
        ProjectDonationInformattionDTO mockInfo = mock(ProjectDonationInformattionDTO.class);
        when(mockInfo.getTarget()).thenReturn(BigDecimal.valueOf(10000));
        when(mockInfo.getTotalDonation()).thenReturn(BigDecimal.valueOf(3000));

        when(mockProjectRepo.getDonationInformationTotal(any()))
                .thenReturn(mockInfo);

        // Mock donations
        Page<Donation> donationPage = new PageImpl<>(List.of(nullRefDonation));
        when(mockDonationRepo.findDonationsByFilters(any(), any(), any()))
                .thenReturn(donationPage);

        // Thực thi test
        PageResponse<DonationResponseDTO> response = mockedService.viewListDonations(
                0, 10, BigInteger.valueOf(1), "");

        // Kiểm tra
        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
        assertEquals(1, response.getContent().size());

        DonationResponseDTO dto = response.getContent().get(0);
        assertNull(dto.getProject());
        assertNull(dto.getTransferredProject());
        assertNull(dto.getRefer());
        assertNull(dto.getChallenge());
        assertNull(dto.getCreatedBy());
    }

    @Test
    @DisplayName("DS_viewListDonations_08")
    void viewListDonations_shouldHandleVariousStatuses() {
        // Tạo donation với WrongDonation
        Donation pendingDonation = donationRepository.save(Donation.builder()
                .tid("STATUS001")
                .project(project)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(2000))
                .description("Status Test")
                .build());

        // Tạo WrongDonation
        WrongDonation wrongDonation = wrongDonationRepository.save(WrongDonation.builder()
                .donation(pendingDonation)
                .build());
        pendingDonation.setWrongDonation(wrongDonation);
        pendingDonation = donationRepository.save(pendingDonation);

        // Thực thi test
        PageResponse<DonationResponseDTO> response = defaultDonationService.viewListDonations(
                0, 10, project.getProjectId(), "Status Test");

        // Kiểm tra
        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
        assertEquals("Pending", response.getContent().get(0).getStatus());

        // Xóa WrongDonation và kiểm tra lại status
        wrongDonationRepository.delete(wrongDonation);
        pendingDonation.setWrongDonation(null);
        donationRepository.save(pendingDonation);

        response = defaultDonationService.viewListDonations(
                0, 10, project.getProjectId(), "Status Test");

        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
        assertEquals("", response.getContent().get(0).getStatus());
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

    @Test
    @DisplayName("DS_viewListDonationsAdmin_03")
    void viewListDonationsAdmin_shouldFilterByDescription() {
        // Tạo donations với các mô tả khác nhau
        donationRepository.save(Donation.builder()
                .tid("ADMIN001")
                .project(project)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(5000))
                .description("Admin Test")
                .build());

        // Kiểm tra lọc theo description
        PageResponse<DonationResponseDTO> response = defaultDonationService.viewListDonationsAdmin(
                0, 10, project.getProjectId(), "Admin");

        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
        assertTrue(response.getContent().stream()
                .anyMatch(dto -> dto.getDescription().contains("Admin")));

        // Với description không tồn tại
        PageResponse<DonationResponseDTO> emptyResponse = defaultDonationService.viewListDonationsAdmin(
                0, 10, project.getProjectId(), "Not Exist");

        assertNotNull(emptyResponse);
        assertTrue(emptyResponse.getContent().isEmpty());
    }

    @Test
    @DisplayName("DS_viewListDonationsAdmin_04")
    void viewListDonationsAdmin_shouldHandleNonExistentProject() {
        BigInteger nonExistentProjectId = BigInteger.valueOf(999999);

        PageResponse<DonationResponseDTO> response = defaultDonationService.viewListDonationsAdmin(
                0, 10, nonExistentProjectId, "");

        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotal());
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
    @DisplayName("DS_viewDonationsByChallengeId_03")
    void viewDonationsByChallengeId_shouldHandleNonExistentChallenge() {
        BigInteger nonExistentChallengeId = BigInteger.valueOf(999999);

        PageResponse<DonationResponseDTO> response = defaultDonationService.viewDonationsByChallengeId(
                0, 10, nonExistentChallengeId, "");

        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotal());
    }

    @Test
    @DisplayName("DS_viewDonationsByChallengeId_04")
    void viewDonationsByChallengeId_shouldCalculateTotalCorrectly() {
        // Tạo nhiều donations với challenge và giá trị khác nhau
        BigDecimal expected = BigDecimal.ZERO;

        for (int i = 0; i < 5; i++) {
            BigDecimal value = BigDecimal.valueOf(1000 * (i + 1));
            expected = expected.add(value);

            donationRepository.save(Donation.builder()
                    .tid("CHTOTAL" + i)
                    .project(project)
                    .challenge(challenge)
                    .createdBy(account)
                    .createdAt(LocalDateTime.now())
                    .value(value)
                    .description("Challenge Total " + i)
                    .build());
        }

        PageResponse<DonationResponseDTO> response = defaultDonationService.viewDonationsByChallengeId(
                0, 10, challenge.getChallengeId(), "");

        assertNotNull(response);
        assertNotNull(response.getSummary());
        BigDecimal actual = (BigDecimal) response.getSummary().get("total_donation");
        assertEquals(0, expected.compareTo(actual));
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
    @DisplayName("DS_viewDonationsByAccount_03")
    void viewDonationsByAccount_shouldFilterByDescription() {
        // Tạo các donations với mô tả khác nhau
        donationRepository.save(Donation.builder()
                .tid("ACCDESC001")
                .project(project)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(1500))
                .description("Account Test Description")
                .build());

        PageResponse<DonationResponseDTO> response = defaultDonationService.viewDonationsByAccount(
                0, 10, account.getEmail(), "Account Test");

        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
        assertTrue(response.getContent().stream()
                .anyMatch(dto -> dto.getDescription().contains("Account Test")));

        // Kiểm tra với description không tồn tại
        PageResponse<DonationResponseDTO> emptyResponse = defaultDonationService.viewDonationsByAccount(
                0, 10, account.getEmail(), "Non Existent");

        assertNotNull(emptyResponse);
        assertTrue(emptyResponse.getContent().isEmpty());
    }

    @Test
    @DisplayName("DS_viewDonationsByAccount_04")
    void viewDonationsByAccount_shouldHandleAccountWithNoDonations() {
        // Tạo một account không có donation
        Account emptyAccount = accountRepository.save(Account.builder()
                .email("empty@example.com")
                .fullname("Empty User")
                .password("password")
                .code("EMPTY001")
                .build());

        PageResponse<DonationResponseDTO> response = defaultDonationService.viewDonationsByAccount(
                0, 10, emptyAccount.getEmail(), "");

        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotal());
    }

    @Test
    @DisplayName("DS_viewDonationsByReferCode_01")
    void viewDonationsByReferCode_shouldReturnDonationsForReferCode() {
        // Mock account repository
        AccountRepository mockAccountRepo = mock(AccountRepository.class);
        DonationRepository mockDonationRepo = mock(DonationRepository.class);

        // Setup mocked service with mocked repositories
        DefaultDonationService mockedService = new DefaultDonationService(
                mockDonationRepo, mockAccountRepo, projectRepository);

        // Set up behavior for mocked repositories
        when(mockAccountRepo.findSystemUserAccountByAccountCode(account.getCode()))
                .thenReturn(Optional.of(account));

        // Setup mocked donations list and behavior
        Donation referDonation = Donation.builder()
                .donationId(BigInteger.valueOf(999))
                .tid("REFTID001")
                .project(project)
                .refer(account)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(25000))
                .description("Refer Donation")
                .build();

        List<Donation> donations = List.of(referDonation);
        Page<Donation> page = new PageImpl<>(donations);

        when(mockDonationRepo.getDonationsByReferId(any(), eq(account.getAccountId()), any()))
                .thenReturn(page);
        when(mockDonationRepo.getTotalDonationByReferId(account.getAccountId()))
                .thenReturn(BigDecimal.valueOf(25000));

        // Execute test
        PageResponse<?> response = mockedService.viewDonationsByReferCode(0, 10, account.getCode());

        // Assertions
        assertNotNull(response);
        assertEquals(10, response.getLimit());
        assertEquals(0, response.getOffset());
        assertEquals(1, response.getTotal());
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
    @DisplayName("DS_viewDonationsByReferCode_03")
    void viewDonationsByReferCode_shouldHandleReferWithNoDonations() {
        // Mock account repository và donation repository
        AccountRepository mockAccountRepo = mock(AccountRepository.class);
        DonationRepository mockDonationRepo = mock(DonationRepository.class);

        // Tạo account giả lập không có donation
        Account emptyReferAccount = Account.builder()
                .accountId(BigInteger.valueOf(123))
                .email("emptyrefer@example.com")
                .fullname("Empty Refer User")
                .password("password")
                .code("EMPTYREF001")
                .build();

        // Setup mocked service với repositories đã mock
        DefaultDonationService mockedService = new DefaultDonationService(
                mockDonationRepo, mockAccountRepo, projectRepository);

        // Setup behavior cho mockAccountRepo
        when(mockAccountRepo.findSystemUserAccountByAccountCode(emptyReferAccount.getCode()))
                .thenReturn(Optional.of(emptyReferAccount));

        // Trả về trang rỗng cho donations
        Page<Donation> emptyPage = new PageImpl<>(new ArrayList<>());
        when(mockDonationRepo.getDonationsByReferId(any(), eq(emptyReferAccount.getAccountId()), any()))
                .thenReturn(emptyPage);
        when(mockDonationRepo.getTotalDonationByReferId(emptyReferAccount.getAccountId()))
                .thenReturn(BigDecimal.ZERO);

        // Thực thi test
        PageResponse<?> response = mockedService.viewDonationsByReferCode(
                0, 10, emptyReferAccount.getCode());

        // Kiểm tra kết quả
        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotal());
        assertNotNull(response.getSummary());
        assertEquals(BigDecimal.ZERO, response.getSummary().get("total_donation_by_refer"));
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
    @DisplayName("DS_viewAllDonations_03")
    void viewAllDonations_shouldHandlePagination() {
        // Tạo nhiều donations để test phân trang
        for (int i = 0; i < 25; i++) {
            donationRepository.save(Donation.builder()
                    .tid("ALLTID" + i)
                    .project(project)
                    .createdBy(account)
                    .createdAt(LocalDateTime.now())
                    .value(BigDecimal.valueOf(100 * (i + 1)))
                    .description("All Donation " + i)
                    .build());
        }

        // Test với size nhỏ
        PageResponse<?> smallPage = defaultDonationService.viewAllDonations(0, 5, "");
        assertEquals(5, smallPage.getContent().size());

        // Test với page khác 0
        PageResponse<?> nextPage = defaultDonationService.viewAllDonations(1, 10, "");
        assertNotNull(nextPage);
        assertFalse(nextPage.getContent().isEmpty());

        // Test với size lớn hơn tổng số donations
        PageResponse<?> largePage = defaultDonationService.viewAllDonations(0, 100, "");
        assertTrue(largePage.getContent().size() >= 25);
    }

    @Test
    @DisplayName("DS_viewListDonations_09")
    void viewListDonations_shouldHandleProjectWithMultipleReferences() {
        // Tạo một donation có refer
        Account referrer = accountRepository.save(Account.builder()
                .email("referrer@test.com")
                .fullname("Referrer")
                .password("password")
                .code("REF001")
                .build());

        // Tạo một donation có challenge
        Challenge newChallenge = challengeRepository.save(Challenge.builder()
                .title("Another Challenge")
                .challengeCode("CHAL002")
                .content("Another Challenge Description")
                .createdAt(LocalDateTime.now())
                .goal(BigDecimal.valueOf(50000))
                .finishedAt(LocalDate.now().plusDays(30))
                .build());

        // Tạo một project khác để test transferred project
        Project transferredProject = projectRepository.save(Project.builder()
                .title("Transferred Project")
                .code("TRANS001")
                .status(2)
                .campaign(campaign)
                .ward("Test Ward")
                .district("Test District")
                .province("Test Province")
                .slug("transferred-project")
                .amountNeededToRaise(BigDecimal.valueOf(100000))
                .totalBudget(BigDecimal.valueOf(50000))
                .createdAt(LocalDateTime.now())
                .build());

        // Tạo donation với đầy đủ references
        Donation complexDonation = donationRepository.save(Donation.builder()
                .tid("COMPLEX001")
                .project(project)
                .transferredProject(transferredProject)
                .refer(referrer)
                .challenge(newChallenge)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(10000))
                .description("Complex Donation")
                .build());

        // Test view donations
        PageResponse<DonationResponseDTO> response = defaultDonationService.viewListDonations(
                0, 10, project.getProjectId(), "Complex");

        // Verify response
        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
        DonationResponseDTO dto = response.getContent().get(0);
        
        // Verify all references are correctly mapped
        assertEquals(referrer.getEmail(), dto.getRefer().getEmail());
        assertEquals(newChallenge.getTitle(), dto.getChallenge().getTitle());
        assertEquals(transferredProject.getTitle(), dto.getTransferredProject().getTitle());
        assertEquals(account.getEmail(), dto.getCreatedBy().getEmail());
    }

    @Test
    @DisplayName("DS_viewListDonationsAdmin_05")
    void viewListDonationsAdmin_shouldHandleComplexDonationData() {
        // Tạo donation với các trường phức tạp
        Donation complexDonation = donationRepository.save(Donation.builder()
                .tid("ADMIN_COMPLEX001")
                .project(project)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(15000))
                .description("Complex Admin Test")
                .bankSubAccId("SUB123")
                .bankName("Test Bank")
                .corresponsiveName("John Doe")
                .corresponsiveAccount("ACC123456")
                .corresponsiveBankId("BANK001")
                .corresponsiveBankName("Bank of Test")
                .note("Special note for testing")
                .build());

        // Test view với role admin
        PageResponse<DonationResponseDTO> response = defaultDonationService.viewListDonationsAdmin(
                0, 10, project.getProjectId(), "Complex Admin");

        // Verify response
        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
        DonationResponseDTO dto = response.getContent().get(0);

        // Verify all fields are correctly mapped
        assertEquals("SUB123", dto.getBankSubAccId());
        assertEquals("Test Bank", dto.getBankName());
        assertEquals("John Doe", dto.getCorresponsiveName());
        assertEquals("ACC123456", dto.getCorresponsiveAccount());
        assertEquals("BANK001", dto.getCorresponsiveBankId());
        assertEquals("Bank of Test", dto.getCorresponsiveBankName());
        assertEquals("Special note for testing", dto.getNote());
    }

    @Test
    @DisplayName("DS_viewDonationsByChallengeId_05")
    void viewDonationsByChallengeId_shouldHandleMultipleProjects() {
        // Tạo project thứ hai
        Project project2 = projectRepository.save(Project.builder()
                .title("Second Project")
                .code("PROJ002")
                .status(2)
                .campaign(campaign)
                .ward("Test Ward 2")
                .district("Test District 2")
                .province("Test Province 2")
                .slug("second-project")
                .amountNeededToRaise(BigDecimal.valueOf(200000))
                .totalBudget(BigDecimal.valueOf(100000))
                .createdAt(LocalDateTime.now())
                .build());

        // Tạo ChallengeProject cho project thứ hai
        ChallengeProject challengeProject2 = new ChallengeProject();
        challengeProject2.setChallenge(challenge);
        challengeProject2.setProject(project2);
        entityManager.persist(challengeProject2);

        // Tạo donations cho cả hai project trong cùng một challenge
        Donation donation1 = donationRepository.save(Donation.builder()
                .tid("MULTI_CH001")
                .project(project)
                .challenge(challenge)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(5000))
                .description("Multi Project Challenge 1")
                .build());

        Donation donation2 = donationRepository.save(Donation.builder()
                .tid("MULTI_CH002")
                .project(project2)
                .challenge(challenge)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(7000))
                .description("Multi Project Challenge 2")
                .build());

        // Test view donations by challenge
        PageResponse<DonationResponseDTO> response = defaultDonationService.viewDonationsByChallengeId(
                0, 10, challenge.getChallengeId(), "");

        // Verify response
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        
        // Verify total donation calculation
        BigDecimal expectedTotal = BigDecimal.valueOf(12000); // 5000 + 7000
        BigDecimal actualTotal = (BigDecimal) response.getSummary().get("total_donation");
        assertEquals(0, expectedTotal.compareTo(actualTotal));

        // Verify donations are from different projects
        Set<String> projectTitles = response.getContent().stream()
                .map(dto -> dto.getProject().getTitle())
                .collect(Collectors.toSet());
        assertEquals(2, projectTitles.size());
        assertTrue(projectTitles.contains("Test Project"));
        assertTrue(projectTitles.contains("Second Project"));
    }

    @Test
    @DisplayName("DS_viewDonationsByAccount_05")
    void viewDonationsByAccount_shouldHandleMultipleProjectDonations() {
        // Tạo project thứ hai
        Project project2 = projectRepository.save(Project.builder()
                .title("Another Project")
                .code("PROJ003")
                .status(2)
                .campaign(campaign)
                .ward("Another Ward")
                .district("Another District")
                .province("Another Province")
                .slug("another-project")
                .amountNeededToRaise(BigDecimal.valueOf(150000))
                .totalBudget(BigDecimal.valueOf(75000))
                .createdAt(LocalDateTime.now())
                .build());

        // Tạo donations cho nhiều project khác nhau
        Donation donation1 = donationRepository.save(Donation.builder()
                .tid("MULTI_ACC001")
                .project(project)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(3000))
                .description("Multi Account Project 1")
                .build());

        Donation donation2 = donationRepository.save(Donation.builder()
                .tid("MULTI_ACC002")
                .project(project2)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(4000))
                .description("Multi Account Project 2")
                .build());

        // Test view donations by account
        PageResponse<DonationResponseDTO> response = defaultDonationService.viewDonationsByAccount(
                0, 10, account.getEmail(), "");

        // Verify response
        assertNotNull(response);
        assertTrue(response.getTotal() >= 2);

        // Verify donations are from different projects
        Set<String> projectTitles = response.getContent().stream()
                .map(dto -> dto.getProject().getTitle())
                .collect(Collectors.toSet());
        assertTrue(projectTitles.contains("Test Project"));
        assertTrue(projectTitles.contains("Another Project"));

        // Verify all donations have correct creator
        assertTrue(response.getContent().stream()
                .allMatch(dto -> dto.getCreatedBy().getEmail().equals(account.getEmail())));
    }

    @Test
    @DisplayName("DS_viewAllDonations_04")
    void viewAllDonations_shouldHandleComplexFiltering() {
        // Tạo một số donations với các trường khác nhau để test filtering
        Account anotherAccount = accountRepository.save(Account.builder()
                .email("another@test.com")
                .fullname("Another User")
                .password("password")
                .code("TEST002")
                .build());

        Project anotherProject = projectRepository.save(Project.builder()
                .title("Filter Test Project")
                .code("FILTER001")
                .status(2)
                .campaign(campaign)
                .ward("Filter Ward")
                .district("Filter District")
                .province("Filter Province")
                .slug("filter-test-project")
                .amountNeededToRaise(BigDecimal.valueOf(80000))
                .totalBudget(BigDecimal.valueOf(40000))
                .createdAt(LocalDateTime.now())
                .build());

        // Tạo các donations với các pattern khác nhau
        donationRepository.save(Donation.builder()
                .tid("FILTER001")
                .project(project)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(2000))
                .description("Filter Test 1")
                .build());

        donationRepository.save(Donation.builder()
                .tid("FILTER002")
                .project(anotherProject)
                .createdBy(anotherAccount)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(3000))
                .description("Different Filter Test")
                .build());

        // Test với các pattern filtering khác nhau
        PageResponse<?> response1 = defaultDonationService.viewAllDonations(0, 10, "Filter Test");
        assertNotNull(response1);
        assertTrue(response1.getTotal() >= 2);

        PageResponse<?> response2 = defaultDonationService.viewAllDonations(0, 10, "Different");
        assertNotNull(response2);
        assertEquals(1, response2.getTotal());

        PageResponse<?> response3 = defaultDonationService.viewAllDonations(0, 10, "NonExistent");
        assertNotNull(response3);
        assertEquals(0, response3.getTotal());
    }

    @Test
    @DisplayName("DS_viewAllDonations_05")
    void viewAllDonations_shouldHandleDifferentDonationTypes() {
        // Tạo donation thông thường
        Donation normalDonation = donationRepository.save(Donation.builder()
                .tid("TYPE001")
                .project(project)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(1000))
                .description("Normal Donation")
                .build());

        // Tạo donation với wrong donation
        Donation wrongDonation = donationRepository.save(Donation.builder()
                .tid("TYPE002")
                .project(project)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(2000))
                .description("Wrong Donation")
                .build());

        WrongDonation wrong = wrongDonationRepository.save(WrongDonation.builder()
                .donation(wrongDonation)
                .build());
        wrongDonation.setWrongDonation(wrong);
        wrongDonation = donationRepository.save(wrongDonation);

        // Tạo donation với challenge
        Donation challengeDonation = donationRepository.save(Donation.builder()
                .tid("TYPE003")
                .project(project)
                .challenge(challenge)
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .value(BigDecimal.valueOf(3000))
                .description("Challenge Donation")
                .build());

        // Test view all donations
        PageResponse<?> response = defaultDonationService.viewAllDonations(0, 10, "");

        // Verify response
        assertNotNull(response);
        assertTrue(response.getTotal() >= 3);

        // Verify different donation types
        List<DonationResponseDTO> donations = (List<DonationResponseDTO>) response.getContent();
        
        // Find and verify wrong donation
        Optional<DonationResponseDTO> foundWrong = donations.stream()
                .filter(d -> d.getTid().equals("TYPE002"))
                .findFirst();
        assertTrue(foundWrong.isPresent());
        assertEquals("Pending", foundWrong.get().getStatus());

        // Find and verify challenge donation
        Optional<DonationResponseDTO> foundChallenge = donations.stream()
                .filter(d -> d.getTid().equals("TYPE003"))
                .findFirst();
        assertTrue(foundChallenge.isPresent());
        assertNotNull(foundChallenge.get().getChallenge());
        assertEquals(challenge.getTitle(), foundChallenge.get().getChallenge().getTitle());
    }
}
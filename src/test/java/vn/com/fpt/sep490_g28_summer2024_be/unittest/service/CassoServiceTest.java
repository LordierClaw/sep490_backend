package vn.com.fpt.sep490_g28_summer2024_be.unittest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fpt.sep490_g28_summer2024_be.common.AppConfig;
import vn.com.fpt.sep490_g28_summer2024_be.dto.casso.TransactionDataDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.donation.DonationResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Campaign;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Challenge;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Donation;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Project;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Role;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.CampaignRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ChallengeRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.DonationRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.RoleRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.WrongDonationRepository;
import vn.com.fpt.sep490_g28_summer2024_be.service.casso.DefaultCassoService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CassoServiceTest {

    // Khai báo các dependency injection
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private WrongDonationRepository wrongDonationRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("initExecutor")
    private Executor executor;

    @Autowired
    private DefaultCassoService defaultCassoService;

    // Khai báo các đối tượng dữ liệu test
    private Project project;
    private Challenge challenge;
    private Account account;
    private Campaign campaign;
    private TransactionDataDTO transactionDataDTO;
    private Donation existingDonation;
    @Autowired
    private CampaignRepository campaignRepository;
    @Autowired
    private RoleRepository roleRepository;

    // Khởi tạo dữ liệu test trước mỗi test case
    @BeforeEach
    void setUp() {

        // Khởi tạo dữ liệu test cho Account
        Role adminRole = roleRepository.save(Role.builder().roleName("admin").roleDescription("").build());
        account = accountRepository.save(Account.builder()
                .email("test@example.com")
                .password("123456")
                .code("ACC001")
                .role(adminRole)
                .fullname("Test User")
                .build());

        // Khởi tạo dữ liệu test cho Campaign
        campaign = Campaign.builder()
                .title("Test Campaign")
                .description("Test Campaign Description")
                .build();

        // Khởi tạo dữ liệu test cho Project
        project = Project.builder()
                .title("Test Project")
                .code("PRJ001")
                .status(2)
                .ward("wardtest")
                .district("districttest")
                .province("provincetest")
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(BigDecimal.valueOf(1000))
                .totalBudget(BigDecimal.valueOf(2000))
                .campaign(campaign)
                .build();

        // Khởi tạo dữ liệu test cho Challenge
        challenge = Challenge.builder()
                .title("Test Challenge")
                .challengeCode("CHL001")
                .build();

        // Khởi tạo dữ liệu test cho TransactionDataDTO
        transactionDataDTO = TransactionDataDTO.builder()
                .tid("TID002")
                .description("Test Transaction")
                .amount(BigDecimal.valueOf(100000))
                .when(LocalDateTime.now())
                .bankSubAccId("BANK001")
                .corresponsiveName("Test Sender")
                .corresponsiveAccount("ACC123456")
                .corresponsiveBankId("BANK123")
                .corresponsiveBankName("Test Bank")
                .build();

        // Khởi tạo dữ liệu test cho Donation
        existingDonation = Donation.builder()
                .tid("TID001")
                .value(BigDecimal.valueOf(100000))
                .description("Original donation")
                .project(project)
                .challenge(challenge)
                .refer(account)
                .build();
    }

    void saveTestData() {
        campaign = campaignRepository.save(campaign);
        project = projectRepository.save(project);
        challenge = challengeRepository.save(challenge);
        existingDonation = donationRepository.save(existingDonation);
        transactionDataDTO.setId(1L);
    }

    @Test
    @DisplayName("CS_handleInPayment_01")
    void handleInPayment_shouldReturnDonationWithRefer_whenDescriptionContainsReferPrefix() {
        saveTestData();

        // Chuẩn bị dữ liệu test
        transactionDataDTO.setDescription(AppConfig.REFER_PREFIX + " " + account.getCode());

        // Thực thi phương thức test
        DonationResponseDTO result = defaultCassoService.handleInPayment(transactionDataDTO);

        // Kiểm tra kết quả
        assertNotNull(result);
        Donation savedDonation = donationRepository.findById(result.getDonationId()).orElse(null);
        assertNotNull(savedDonation);
    }

    @Test
    @DisplayName("CS_handleInPayment_02")
    void handleInPayment_shouldReturnDonationWithChallenge_whenDescriptionContainsChallengePrefix() {
        saveTestData();

        // Chuẩn bị dữ liệu test
        transactionDataDTO.setDescription(AppConfig.CHALLENGE_PREFIX + " " + challenge.getChallengeCode());

        // Thực thi phương thức test
        DonationResponseDTO result = defaultCassoService.handleInPayment(transactionDataDTO);

        // Kiểm tra kết quả
        assertNotNull(result);
        Donation savedDonation = donationRepository.findById(result.getDonationId()).orElse(null);
        assertNotNull(savedDonation);
    }

    @Test
    @DisplayName("CS_handleInPayment_03")
    void handleInPayment_shouldReturnDonationWithProject_whenDescriptionContainsProjectCode() {
        saveTestData();

        // Chuẩn bị dữ liệu test
        transactionDataDTO.setDescription(AppConfig.PROJECT_PREFIX + " " + project.getCode());

        // Thực thi phương thức test
        DonationResponseDTO result = defaultCassoService.handleInPayment(transactionDataDTO);

        // Kiểm tra kết quả
        assertNotNull(result);
        Donation savedDonation = donationRepository.findById(result.getDonationId()).orElse(null);
        assertNotNull(savedDonation);
    }

    @Test
    @DisplayName("CS_handleInPayment_04")
    void handleInPayment_shouldReturnDonationWithCreatedBy_whenDescriptionContainsAccountPrefix() {
        saveTestData();

        // Chuẩn bị dữ liệu test
        transactionDataDTO.setDescription(AppConfig.ACCOUNT_PREFIX + " " + account.getCode());

        // Thực thi phương thức test
        DonationResponseDTO result = defaultCassoService.handleInPayment(transactionDataDTO);

        // Kiểm tra kết quả
        assertNotNull(result);
        Donation savedDonation = donationRepository.findById(result.getDonationId()).orElse(null);
        assertNotNull(savedDonation);
    }

    @Test
    @DisplayName("CS_handleOutPayment_01")
    void handleOutPayment_shouldReturnDonationWithoutRefer_whenReferNotExist() {
        // Chuẩn bị dữ liệu test
        donationRepository.deleteAll();
        wrongDonationRepository.deleteAll();
        transactionDataDTO.setId(1L);
        transactionDataDTO.setDescription("TID001");

        // Thực thi phương thức test
        DonationResponseDTO result = defaultCassoService.handleOutPayment(transactionDataDTO);

        // Kiểm tra kết quả
        assertNotNull(result);
        assertNull(result.getDonationId());
        assertEquals(0, donationRepository.count());
        assertEquals(0, wrongDonationRepository.count());
    }

    @Test
    @DisplayName("CS_handleOutPayment_02")
    void handleOutPayment_shouldReturnDonationWithoutProject_whenProjectNotExist() {
        saveTestData();

        // Chuẩn bị dữ liệu test
        donationRepository.save(existingDonation);
        existingDonation.setProject(null);
        transactionDataDTO.setDescription(existingDonation.getTid());

        // Thực thi phương thức test
        DonationResponseDTO result = defaultCassoService.handleOutPayment(transactionDataDTO);

        // Kiểm tra kết quả
        assertNotNull(result);
        Long count = entityManager.createQuery(
                        "SELECT COUNT(d) FROM Donation d WHERE d.donationId = :donationId AND d.project IS NULL", Long.class)
                .setParameter("donationId", result.getDonationId())
                .getSingleResult();
        assertTrue(count > 0);
    }

    @Test
    @DisplayName("CS_handleOutPayment_03")
    void handleOutPayment_shouldReturnDonationWithoutChallenge_whenChallengeNotExist() {
        saveTestData();

        // Chuẩn bị dữ liệu test
        donationRepository.save(existingDonation);
        existingDonation.setChallenge(null);
        transactionDataDTO.setDescription(existingDonation.getTid());

        // Thực thi phương thức test
        DonationResponseDTO result = defaultCassoService.handleOutPayment(transactionDataDTO);

        // Kiểm tra kết quả
        assertNotNull(result);
        Donation savedDonation = donationRepository.findById(result.getDonationId()).orElse(null);
        assertNotNull(savedDonation);
        assertNull(savedDonation.getChallenge());
    }

    @Test
    @DisplayName("CS_handleOutPayment_04")
    void handleOutPayment_shouldReturnDonationWithoutTransferredProject_whenTransferredProjectNotExist() {
        saveTestData();

        // Chuẩn bị dữ liệu test
        donationRepository.save(existingDonation);
        existingDonation.setTransferredProject(null);
        transactionDataDTO.setDescription(existingDonation.getTid());

        // Thực thi phương thức test
        DonationResponseDTO result = defaultCassoService.handleOutPayment(transactionDataDTO);

        // Kiểm tra kết quả
        assertNotNull(result);
        Donation savedDonation = donationRepository.findById(result.getDonationId()).orElse(null);
        assertNotNull(savedDonation);
        assertNull(savedDonation.getTransferredProject());
    }

    @Test
    @DisplayName("CS_handleOutPayment_05")
    void handleOutPayment_shouldReturnDonationWithoutReferAccount_whenReferAccountNotExist() {
        saveTestData();

        // Chuẩn bị dữ liệu test
        donationRepository.save(existingDonation);
        existingDonation.setRefer(null);
        transactionDataDTO.setDescription(existingDonation.getTid());

        // Thực thi phương thức test
        DonationResponseDTO result = defaultCassoService.handleOutPayment(transactionDataDTO);

        // Kiểm tra kết quả
        assertNotNull(result);
        Donation savedDonation = donationRepository.findById(result.getDonationId()).orElse(null);
        assertNotNull(savedDonation);
        assertNull(savedDonation.getRefer());
    }

    @Test
    @DisplayName("CS_handleOutPayment_06")
    void handleOutPayment_shouldReturnDonationWithoutWrongDonation_whenWrongDonationNotExist() {
        saveTestData();

        // Chuẩn bị dữ liệu test
        donationRepository.save(existingDonation);
        transactionDataDTO.setDescription(existingDonation.getTid());

        // Thực thi phương thức test
        DonationResponseDTO result = defaultCassoService.handleOutPayment(transactionDataDTO);

        // Kiểm tra kết quả
        assertNotNull(result);
        Donation savedDonation = donationRepository.findById(result.getDonationId()).orElse(null);
        assertNotNull(savedDonation);
    }

    @Test
    @DisplayName("CS_handleOutPayment_07")
    void handleOutPayment_shouldReturnDonationWithAllInformation_whenReferExists() {
        saveTestData();

        // Chuẩn bị dữ liệu test
        donationRepository.save(existingDonation);
        transactionDataDTO.setDescription(existingDonation.getTid());

        // Thực thi phương thức test
        DonationResponseDTO result = defaultCassoService.handleOutPayment(transactionDataDTO);

        // Kiểm tra kết quả
        assertNotNull(result);
        Donation savedDonation = donationRepository.findById(result.getDonationId()).orElse(null);
        assertNotNull(savedDonation);
        assertNotNull(savedDonation.getProject());
        assertNotNull(savedDonation.getChallenge());
        assertNotNull(savedDonation.getRefer());
    }
}

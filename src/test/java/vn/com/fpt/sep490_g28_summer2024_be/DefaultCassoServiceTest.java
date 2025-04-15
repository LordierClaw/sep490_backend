package vn.com.fpt.sep490_g28_summer2024_be;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fpt.sep490_g28_summer2024_be.common.AppConfig;
import vn.com.fpt.sep490_g28_summer2024_be.dto.casso.TransactionDataDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.donation.DonationResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.*;
import vn.com.fpt.sep490_g28_summer2024_be.repository.*;
import vn.com.fpt.sep490_g28_summer2024_be.service.casso.DefaultCassoService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class DefaultCassoServiceTest {

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

    // Test data objects
    private Project project;
    private Challenge challenge;
    private Account account;
    private Campaign campaign;
    private TransactionDataDTO transactionDataDTO;

    @BeforeEach
    void setUp() {
        // Khởi tạo dữ liệu test cho Campaign
        campaign = Campaign.builder()
                .title("Test Campaign")
                .description("Test Campaign Description")
                .build();

        // Khởi tạo dữ liệu test cho Project
        project = Project.builder()
                .title("Test Project")
                .code("PRJ001")
                .status(2) // Active status
                .campaign(campaign)
                .build();

        // Khởi tạo dữ liệu test cho Account
        account = Account.builder()
                .email("test@example.com")
                .code("ACC001")
                .fullname("Test User")
                .build();

        // Khởi tạo dữ liệu test cho Challenge
        challenge = Challenge.builder()
                .title("Test Challenge")
                .challengeCode("CHL001")
                .build();

        // Khởi tạo dữ liệu test cho TransactionDataDTO
        transactionDataDTO = TransactionDataDTO.builder()
                .id(1L)
                .tid("TID001")
                .description("Test Transaction")
                .amount(BigDecimal.valueOf(100000))
                .when(LocalDateTime.now())
                .bankSubAccId("BANK001")
                .corresponsiveName("Test Sender")
                .corresponsiveAccount("ACC123456")
                .corresponsiveBankId("BANK123")
                .corresponsiveBankName("Test Bank")
                .build();
    }

    @Test
    @DisplayName("CS_handleInPayment_01")
    void handleInPayment_shouldReturnDonationWithRefer_whenDescriptionContainsReferPrefix() {
        // Chuẩn bị dữ liệu test
        transactionDataDTO.setDescription(AppConfig.REFER_PREFIX + " " + account.getCode());

        // Thực thi phương thức test
        DonationResponseDTO result = defaultCassoService.handleInPayment(transactionDataDTO);

        // Kiểm tra kết quả
        assertNotNull(result);
        
        // Kiểm tra dữ liệu được lưu trong database
        Donation savedDonation = donationRepository.findById(result.getDonationId()).orElse(null);
        assertNotNull(savedDonation);
    }

    @Test
    @DisplayName("CS_handleInPayment_02")
    void handleInPayment_shouldReturnDonationWithChallenge_whenDescriptionContainsChallengePrefix() {
        // Chuẩn bị dữ liệu test
        transactionDataDTO.setDescription(AppConfig.CHALLENGE_PREFIX + " " + challenge.getChallengeCode());

        // Thực thi phương thức test
        DonationResponseDTO result = defaultCassoService.handleInPayment(transactionDataDTO);

        // Kiểm tra kết quả
        assertNotNull(result);
        
        // Kiểm tra dữ liệu được lưu trong database
        Donation savedDonation = donationRepository.findById(result.getDonationId()).orElse(null);
        assertNotNull(savedDonation);
    }

    @Test
    @DisplayName("CS_handleInPayment_03")
    void handleInPayment_shouldReturnDonationWithProject_whenDescriptionContainsProjectCode() {
        // Chuẩn bị dữ liệu test
        transactionDataDTO.setDescription(project.getCode());

        // Thực thi phương thức test
        DonationResponseDTO result = defaultCassoService.handleInPayment(transactionDataDTO);

        // Kiểm tra kết quả
        assertNotNull(result);
        
        // Kiểm tra dữ liệu được lưu trong database
        Donation savedDonation = donationRepository.findById(result.getDonationId()).orElse(null);
        assertNotNull(savedDonation);
    }

    @Test
    @DisplayName("CS_handleInPayment_04")
    void handleInPayment_shouldReturnDonationWithCreatedBy_whenDescriptionContainsAccountPrefix() {
        // Chuẩn bị dữ liệu test
        transactionDataDTO.setDescription(AppConfig.ACCOUNT_PREFIX + " " + account.getCode());

        // Thực thi phương thức test
        DonationResponseDTO result = defaultCassoService.handleInPayment(transactionDataDTO);

        // Kiểm tra kết quả
        assertNotNull(result);
        
        // Kiểm tra dữ liệu được lưu trong database
        Donation savedDonation = donationRepository.findById(result.getDonationId()).orElse(null);
        assertNotNull(savedDonation);
    }
}

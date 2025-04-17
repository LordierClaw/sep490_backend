package vn.com.fpt.sep490_g28_summer2024_be.unittest.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Campaign;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Donation;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Project;
import vn.com.fpt.sep490_g28_summer2024_be.entity.WrongDonation;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.CampaignRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ChallengeRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.DonationRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.WrongDonationRepository;
import vn.com.fpt.sep490_g28_summer2024_be.service.wrongdonation.DefaultWrongDonationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DefaultWrongDonationTest {
    @Autowired
    private WrongDonationRepository wrongDonationRepository;
    @Autowired
    private DonationRepository donationRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private DefaultWrongDonationService defaultWrongDonationService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ChallengeRepository challengeRepository;
    @Autowired
    private CampaignRepository campaignRepository;

    private Account account;
    private Campaign campaign;

    @BeforeEach
    void setUp() {
        // Xóa sạch dữ liệu trước mỗi test
        wrongDonationRepository.deleteAll();
        donationRepository.deleteAll();
        projectRepository.deleteAll();
        challengeRepository.deleteAll();
        campaignRepository.deleteAll();
        accountRepository.deleteAll();
        account = accountRepository.save(Account.builder()
                .email("test@example.com").fullname("Test User").password("pass").build());
        campaign = campaignRepository.save(Campaign.builder()
                .title("Chiến dịch chung").description("desc").isActive(true)
                .createdAt(LocalDate.now()).build());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_01")
    void updateWrongDonation_shouldNotUpdateOrDeleteWrongDonationWhenNoValidDonation() throws Exception {
        // Tạo dữ liệu test: khoản chi nhầm, không có khoản hợp lệ chung mã giao dịch
        Project project = projectRepository.save(Project.builder()
                .title("Dự án 1").code("PRJ01").status(2).campaign(campaign)
                .ward("ward1").district("district1").province("province1")
                .createdAt(LocalDateTime.now()).build());
        Donation wrongDonation = donationRepository.save(Donation.builder()
                .id("1").tid("TID001").project(project)
                .createdBy(account)
                .value(BigDecimal.valueOf(-50000)) // khoản chi
                .description("NO_MATCH") // không trùng với khoản nào khác
                .createdAt(LocalDateTime.now()).build());
        WrongDonation wrong = wrongDonationRepository.save(WrongDonation.builder().donation(wrongDonation).build());

        // Gọi hàm updateWrongDonation
        defaultWrongDonationService.updateWrongDonationInAsync();

        // Kiểm tra lại DB: Không cập nhật gì, WrongDonation vẫn còn
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(1, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(wrongDonation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertEquals("NO_MATCH", updatedDonation.getDescription());
        assertEquals(-50000, updatedDonation.getValue().intValue());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_02")
    void updateWrongDonation_shouldUpdateInfoAndDeleteWrongDonationWhenValidDonationExists() throws Exception {
        // Tạo dữ liệu test: khoản chi nhầm, có khoản hợp lệ chung mã giao dịch
        Project project = projectRepository.save(Project.builder()
                .title("Dự án 2").code("PRJ02").status(2).campaign(campaign)
                .ward("ward2").district("district2").province("province2")
                .createdAt(LocalDateTime.now()).build());
        // Khoản chi nhầm
        Donation wrongDonation = donationRepository.save(Donation.builder()
                .id("2").tid("TID002")
                .value(BigDecimal.valueOf(-100000))
                .description("TID_REF")
                .build());
        WrongDonation wrong = wrongDonationRepository.save(WrongDonation.builder().donation(wrongDonation).build());
        wrongDonation.setWrongDonation(wrong);
        wrongDonation = donationRepository.save(wrongDonation);
        // Khoản hợp lệ chung mã giao dịch
        Donation rightDonation = donationRepository.save(Donation.builder()
                .id("3").tid("TID_REF")
                .createdBy(account)
                .project(project)
                .value(BigDecimal.valueOf(100000))
                .description("TID_REF")
                .createdAt(LocalDateTime.now())
                .build());
        // Gọi hàm updateWrongDonation
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation bị xóa, khoản chi nhầm đã được cập nhật thông tin từ khoản hợp lệ
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(0, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(wrongDonation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertEquals(rightDonation.getProject().getProjectId(), updatedDonation.getProject().getProjectId());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_03")
    void updateWrongDonation_shouldNotDeleteWrongDonationWhenOnlyOneWayLink() {
        // Tạo dữ liệu: khoản chi nhầm, có khoản hợp lệ chung mã giao dịch, WrongDonation chỉ liên kết 1 chiều (Donation không set wrongDonation)
        Project project = projectRepository.save(Project.builder()
                .title("Dự án 3").code("PRJ03").status(2).campaign(campaign)
                .ward("ward3").district("district3").province("province3")
                .createdAt(LocalDateTime.now()).build());
        Donation wrongDonation = donationRepository.save(Donation.builder()
                .id("4").tid("TID003")
                .value(BigDecimal.valueOf(-150000))
                .description("TID_MATCH_3")
                .build());
        // Không set wrongDonation field cho Donation (liên kết 1 chiều)
        wrongDonationRepository.save(WrongDonation.builder().donation(wrongDonation).build());
        Donation rightDonation = donationRepository.save(Donation.builder()
                .id("5").tid("TID_MATCH_3")
                .createdBy(account)
                .project(project)
                .value(BigDecimal.valueOf(150000))
                .description("TID_MATCH_3")
                .createdAt(LocalDateTime.now())
                .build());
        // Gọi hàm updateWrongDonation
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation không bị xóa
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(1, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(wrongDonation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertEquals(rightDonation.getProject().getProjectId(), updatedDonation.getProject() != null ? updatedDonation.getProject().getProjectId() : null);
    }

    @Test
    @DisplayName("WD_updateWrongDonation_04")
    void updateWrongDonation_shouldDeleteWrongDonationWhenValidProjectExistsAndTwoWayLink() {
        // Tạo dữ liệu: khoản thu cho 1 dự án, có dự án đúng code/campaign/status, WrongDonation liên kết 2 chiều
        Project project = projectRepository.save(Project.builder()
                .title("Dự án 4").code("PRJ04").status(2).campaign(campaign)
                .ward("ward4").district("district4").province("province4")
                .amountNeededToRaise(BigDecimal.valueOf(100000))
                .totalBudget(BigDecimal.valueOf(50000))
                .createdAt(LocalDateTime.now()).build());
        Donation donation = donationRepository.save(Donation.builder()
                .id("6").tid("TID004")
                .project(project)
                .createdBy(account)
                .value(BigDecimal.valueOf(200000))
                .description("PRJ04")
                .createdAt(LocalDateTime.now())
                .build());
        WrongDonation wrong = wrongDonationRepository.save(WrongDonation.builder().donation(donation).build());
        donation.setWrongDonation(wrong); // Liên kết 2 chiều
        donationRepository.save(donation);

        // Gọi hàm updateWrongDonation
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation bị xóa
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(0, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(donation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertEquals(project.getProjectId(), updatedDonation.getProject().getProjectId());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_05")
    void updateWrongDonation_shouldNotDeleteWrongDonationWhenValidProjectExistsAndOneWayLink() {
        // Tạo dữ liệu: khoản thu cho 1 dự án, có dự án đúng code/campaign/status, WrongDonation chỉ liên kết 1 chiều
        Project project = projectRepository.save(Project.builder()
                .title("Dự án 5").code("PRJ05").status(2).campaign(campaign)
                .ward("ward5").district("district5").province("province5")
                .amountNeededToRaise(BigDecimal.valueOf(100000))
                .totalBudget(BigDecimal.valueOf(50000))
                .createdAt(LocalDateTime.now()).build());
        Donation donation = donationRepository.save(Donation.builder()
                .id("7").tid("TID005")
                .project(project)
                .createdBy(account)
                .value(BigDecimal.valueOf(250000))
                .description("PRJ05")
                .createdAt(LocalDateTime.now())
                .build());
        wrongDonationRepository.save(WrongDonation.builder().donation(donation).build()); // Không set wrongDonation cho Donation
        // Gọi hàm updateWrongDonation
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation không bị xóa
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(1, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(donation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertEquals(project.getProjectId(), updatedDonation.getProject().getProjectId());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_06")
    void updateWrongDonation_shouldDeleteWrongDonationWhenCampaignAndStatusMatchAndTwoWayLink() {
        // Tạo dữ liệu: khoản thu cho 1 dự án, tìm thấy 1 dự án với campaignId, status tương ứng, WrongDonation liên kết 2 chiều
        Project project = projectRepository.save(Project.builder()
                .title("Dự án 6").code("PRJ06").status(2).campaign(campaign)
                .ward("ward6").district("district6").province("province6")
                .amountNeededToRaise(BigDecimal.valueOf(100000))
                .totalBudget(BigDecimal.valueOf(50000))
                .createdAt(LocalDateTime.now()).build());
        Donation donation = donationRepository.save(Donation.builder()
                .id("8").tid("TID006")
                .value(BigDecimal.valueOf(300000))
                .project(project)
                .description("desc6")
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .build());
        Project validProject = projectRepository.save(Project.builder()
                .title("Dự án 6 hợp lệ").code("PRJ06VALID").status(2).campaign(campaign)
                .ward("ward6").district("district6").province("province6")
                .amountNeededToRaise(BigDecimal.valueOf(100000))
                .totalBudget(BigDecimal.valueOf(50000))
                .createdAt(LocalDateTime.now()).build());

        WrongDonation wrong = wrongDonationRepository.save(WrongDonation.builder().donation(donation).build());
        donation.setWrongDonation(wrong); // Liên kết 2 chiều
        donationRepository.save(donation);
        // Gọi hàm updateWrongDonation
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation bị xóa, donation chuyển sang project mới
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(0, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(donation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertNotNull(updatedDonation.getTransferredProject());
        assertEquals(validProject.getProjectId(), updatedDonation.getTransferredProject().getProjectId());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_07")
    void updateWrongDonation_shouldNotDeleteWrongDonationWhenCampaignAndStatusMatchAndOneWayLink() {
        // Tạo dữ liệu: khoản thu cho 1 dự án, tìm thấy 1 dự án với campaignId, status tương ứng, WrongDonation liên kết 1 chiều
        Project project = projectRepository.save(Project.builder()
                .title("Dự án 6").code("PRJ06").status(2).campaign(campaign)
                .ward("ward6").district("district6").province("province6")
                .amountNeededToRaise(BigDecimal.valueOf(100000))
                .totalBudget(BigDecimal.valueOf(50000))
                .createdAt(LocalDateTime.now()).build());
        Donation donation = donationRepository.save(Donation.builder()
                .id("8").tid("TID006")
                .value(BigDecimal.valueOf(300000))
                .project(project)
                .description("desc6")
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .build());
        Project validProject = projectRepository.save(Project.builder()
                .title("Dự án 6 hợp lệ").code("PRJ06VALID").status(2).campaign(campaign)
                .ward("ward6").district("district6").province("province6")
                .amountNeededToRaise(BigDecimal.valueOf(100000))
                .totalBudget(BigDecimal.valueOf(50000))
                .createdAt(LocalDateTime.now()).build());
        wrongDonationRepository.save(WrongDonation.builder().donation(donation).build()); // Không set wrongDonation cho Donation
        // Gọi hàm updateWrongDonation
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation không bị xóa, donation chuyển sang project mới
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(1, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(donation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertNotNull(updatedDonation.getTransferredProject());
        assertEquals(validProject.getProjectId(), updatedDonation.getTransferredProject().getProjectId());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_08")
    void updateWrongDonation_shouldDeleteWrongDonationWhenStatusMatchAndTwoWayLink() {
        // Tạo dữ liệu: khoản thu cho 1 dự án, tìm thấy 1 dự án với status tương ứng, WrongDonation liên kết 2 chiều
        Project project = projectRepository.save(Project.builder()
                .title("Dự án 6").code("PRJ06").status(2).campaign(campaign)
                .ward("ward6").district("district6").province("province6")
                .amountNeededToRaise(BigDecimal.valueOf(100000))
                .totalBudget(BigDecimal.valueOf(50000))
                .createdAt(LocalDateTime.now()).build());
        Donation donation = donationRepository.save(Donation.builder()
                .id("8").tid("TID006")
                .value(BigDecimal.valueOf(300000))
                .project(project)
                .description("desc6")
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .build());
        Campaign anotherCampaign = campaignRepository.save(Campaign.builder()
                .title("Chiến dịch chung").description("desc").isActive(true)
                .build());
        Project validProject = projectRepository.save(Project.builder()
                .title("Dự án 6 hợp lệ").code("PRJ06VALID").status(2)
                .campaign(anotherCampaign)
                .ward("ward6").district("district6").province("province6")
                .amountNeededToRaise(BigDecimal.valueOf(100000))
                .totalBudget(BigDecimal.valueOf(50000))
                .createdAt(LocalDateTime.now()).build());

        WrongDonation wrong = wrongDonationRepository.save(WrongDonation.builder().donation(donation).build());
        donation.setWrongDonation(wrong); // Liên kết 2 chiều
        donationRepository.save(donation);
        // Gọi hàm updateWrongDonation
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation bị xóa, donation chuyển sang project mới
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(0, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(donation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertNotNull(updatedDonation.getTransferredProject());
        assertEquals(validProject.getProjectId(), updatedDonation.getTransferredProject().getProjectId());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_09")
    void updateWrongDonation_shouldNotDeleteWrongDonationWhenStatusMatchAndOneWayLink() {
        // Tạo dữ liệu: khoản thu cho 1 dự án, tìm thấy 1 dự án với status tương ứng, WrongDonation liên kết 1 chiều
        Project project = projectRepository.save(Project.builder()
                .title("Dự án 6").code("PRJ06").status(2).campaign(campaign)
                .ward("ward6").district("district6").province("province6")
                .amountNeededToRaise(BigDecimal.valueOf(100000))
                .totalBudget(BigDecimal.valueOf(50000))
                .createdAt(LocalDateTime.now()).build());
        Donation donation = donationRepository.save(Donation.builder()
                .id("8").tid("TID006")
                .value(BigDecimal.valueOf(300000))
                .project(project)
                .description("desc6")
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .build());
        Campaign anotherCampaign = campaignRepository.save(Campaign.builder()
                .title("Chiến dịch chung").description("desc").isActive(true)
                .build());
        Project validProject = projectRepository.save(Project.builder()
                .title("Dự án 6 hợp lệ").code("PRJ06VALID").status(2)
                .campaign(anotherCampaign)
                .ward("ward6").district("district6").province("province6")
                .amountNeededToRaise(BigDecimal.valueOf(100000))
                .totalBudget(BigDecimal.valueOf(50000))
                .createdAt(LocalDateTime.now()).build());

        wrongDonationRepository.save(WrongDonation.builder().donation(donation).build()); // Không set wrongDonation cho Donation
        // Gọi hàm updateWrongDonation
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation không bị xóa, donation chuyển sang project mới
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(1, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(donation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertNotNull(updatedDonation.getTransferredProject());
        assertEquals(validProject.getProjectId(), updatedDonation.getTransferredProject().getProjectId());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_10")
    void updateWrongDonation_shouldNotUpdateOrDeleteWhenNoProjectToTransfer() {
        // Tạo dữ liệu: khoản thu cho 1 dự án, vẫn có thông tin dự án trong khoản thu nhưng không tìm thấy dự án tương tự để chuyển
        Project project = projectRepository.save(Project.builder()
                .title("Dự án 10").code("PRJ10").status(1).campaign(campaign)
                .ward("ward10").district("district10").province("province10")
                .amountNeededToRaise(BigDecimal.valueOf(100000))
                .totalBudget(BigDecimal.valueOf(50000))
                .createdAt(LocalDateTime.now()).build());
        Donation donation = donationRepository.save(Donation.builder()
                .id("12").tid("TID010")
                .project(project)
                .value(BigDecimal.valueOf(500000))
                .description("desc10")
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .build());
        WrongDonation wrong = wrongDonationRepository.save(WrongDonation.builder().donation(donation).build());
        donation.setWrongDonation(wrong);
        donationRepository.save(donation);
        // Không tạo project nào khác thỏa mãn điều kiện chuyển
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: Không cập nhật gì, WrongDonation vẫn còn
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(1, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(donation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertEquals(project.getProjectId(), updatedDonation.getProject().getProjectId());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_11")
    void updateWrongDonation_shouldNotUpdateOrDeleteWhenNoProjectToTransfer_OneWayLink() {
        // Tạo dữ liệu: khoản thu cho 1 dự án, không có thông tin dự án trong khoản thu, không tìm thấy dự án tương tự để chuyển, WrongDonation liên kết 1 chiều
        Donation donation = donationRepository.save(Donation.builder()
                .id("13").tid("TID011")
                .value(BigDecimal.valueOf(600000))
                .description("desc11")
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .build());
        wrongDonationRepository.save(WrongDonation.builder().donation(donation).build());
        // Không tạo project nào thỏa mãn điều kiện chuyển
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: Không cập nhật gì, WrongDonation vẫn còn
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(1, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(donation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertNull(updatedDonation.getProject());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_12")
    void updateWrongDonation_shouldUpdateTransferProjectAndDeleteWrongDonationWhenStatusMatchAndTwoWayLink() {
        // Tạo dữ liệu: khoản thu cho 1 dự án, không có thông tin dự án trong khoản thu, tìm thấy 1 dự án với status tương ứng, WrongDonation liên kết 2 chiều
        Donation donation = donationRepository.save(Donation.builder()
                .id("14").tid("TID012")
                .value(BigDecimal.valueOf(700000))
                .description("desc12")
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .build());
        Project validProject = projectRepository.save(Project.builder()
                .title("Dự án 12 hợp lệ").code("PRJ12VALID").status(2)
                .campaign(campaign)
                .ward("ward12").district("district12").province("province12")
                .amountNeededToRaise(BigDecimal.valueOf(100000))
                .totalBudget(BigDecimal.valueOf(50000))
                .createdAt(LocalDateTime.now()).build());
        WrongDonation wrong = wrongDonationRepository.save(WrongDonation.builder().donation(donation).build());
        donation.setWrongDonation(wrong);
        donationRepository.save(donation);
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation bị xóa, donation chuyển sang project mới
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(0, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(donation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertNotNull(updatedDonation.getTransferredProject());
        assertEquals(validProject.getProjectId(), updatedDonation.getTransferredProject().getProjectId());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_13")
    void updateWrongDonation_shouldNotDeleteWrongDonationWhenStatusMatchAndOneWayLinkAndHasNoProjectInDonation() {
        // Tạo dữ liệu: khoản thu cho 1 dự án, không có thông tin dự án trong khoản thu, tìm thấy 1 dự án với status tương ứng, WrongDonation liên kết 1 chiều
        Donation donation = donationRepository.save(Donation.builder()
                .id("15").tid("TID013")
                .value(BigDecimal.valueOf(800000))
                .description("desc13")
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .build());
        Project validProject = projectRepository.save(Project.builder()
                .title("Dự án 13 hợp lệ").code("PRJ13VALID").status(2)
                .campaign(campaign)
                .ward("ward13").district("district13").province("province13")
                .amountNeededToRaise(BigDecimal.valueOf(100000))
                .totalBudget(BigDecimal.valueOf(50000))
                .createdAt(LocalDateTime.now()).build());
        wrongDonationRepository.save(WrongDonation.builder().donation(donation).build());
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation không bị xóa, donation chuyển sang project mới
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(1, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(donation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertNull(updatedDonation.getProject());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_14")
    void updateWrongDonation_shouldDoNothingWhenNoProjectMatch() {
        // Tạo dữ liệu: khoản thu cho 1 dự án, không có thông tin dự án trong khoản thu, không tìm thấy 1 dự án với status tương ứng
        Donation donation = donationRepository.save(Donation.builder()
                .id("15").tid("TID013")
                .value(BigDecimal.valueOf(800000))
                .description("desc14")
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .build());
        wrongDonationRepository.save(WrongDonation.builder().donation(donation).build());
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation không bị xóa, donation chuyển sang project mới
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(1, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(donation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertNull(updatedDonation.getProject());
    }
}




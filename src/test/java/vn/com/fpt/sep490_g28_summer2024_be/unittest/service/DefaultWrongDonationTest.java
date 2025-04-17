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
    void updateWrongDonation_shouldDeleteWrongDonationIfProjectDoesNotExist() throws Exception {
        // Tạo dữ liệu test: khoản chi nhầm, không còn tồn tại dự án
        Project project = projectRepository.save(Project.builder()
                .title("Dự án 3").code("PRJ03").status(2).campaign(campaign)
                .ward("ward3").district("district3").province("province3")
                .createdAt(LocalDateTime.now()).build());
        Donation wrongDonation = donationRepository.save(Donation.builder()
                .id("4").tid("TID003").project(project)
                .createdBy(account)
                .value(BigDecimal.valueOf(-200000))
                .description("NO_PROJECT")
                .createdAt(LocalDateTime.now()).build());
        WrongDonation wrong = wrongDonationRepository.save(WrongDonation.builder().donation(wrongDonation).build());
        // Xóa dự án liên quan
        projectRepository.delete(project);
        // Gọi hàm updateWrongDonation
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation bị xóa
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(0, wrongDonations.size());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_04")
    void updateWrongDonation_shouldUpdateInfoAndDeleteWrongDonationByProjectStatus() throws Exception {
        // Tạo dữ liệu test: khoản chi nhầm, có 1 dự án với code, campaignId, status tương ứng
        Project project = projectRepository.save(Project.builder()
                .title("Dự án 4").code("PRJ04").status(2).campaign(campaign)
                .ward("ward4").district("district4").province("province4")
                .createdAt(LocalDateTime.now()).build());
        Donation wrongDonation = donationRepository.save(Donation.builder()
                .id("5").tid("TID004").project(project)
                .createdBy(account)
                .value(BigDecimal.valueOf(300000))
                .description("PRJ04")
                .createdAt(LocalDateTime.now()).build());
        WrongDonation wrong = wrongDonationRepository.save(WrongDonation.builder().donation(wrongDonation).build());
        // Gọi hàm updateWrongDonation
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation bị xóa, donation được cập nhật transferredProject
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(0, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(wrongDonation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertNotNull(updatedDonation.getTransferredProject());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_05")
    void updateWrongDonation_shouldUpdateInfoAndDeleteWrongDonationByProjectStatusCase2() throws Exception {
        // Tạo dữ liệu test: khoản chi nhầm, có 1 dự án với status tương ứng
        Project project = projectRepository.save(Project.builder()
                .title("Dự án 5").code("PRJ05").status(2).campaign(campaign)
                .ward("ward5").district("district5").province("province5")
                .createdAt(LocalDateTime.now()).build());
        Donation wrongDonation = donationRepository.save(Donation.builder()
                .id("6").tid("TID005").project(project)
                .createdBy(account)
                .value(BigDecimal.valueOf(400000))
                .description("PRJ05")
                .createdAt(LocalDateTime.now()).build());
        WrongDonation wrong = wrongDonationRepository.save(WrongDonation.builder().donation(wrongDonation).build());
        // Gọi hàm updateWrongDonation
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation bị xóa, donation được cập nhật transferredProject
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(0, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(wrongDonation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertNotNull(updatedDonation.getTransferredProject());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_06")
    void updateWrongDonation_shouldNotUpdateOrDeleteWrongDonationIfNoMatchingProjectFound() throws Exception {
        // Tạo dữ liệu test: khoản chi nhầm, không tìm thấy dự án tương ứng để chuyển
        Project project = projectRepository.save(Project.builder()
                .title("Dự án 6").code("PRJ06").status(2).campaign(campaign)
                .ward("ward6").district("district6").province("province6")
                .createdAt(LocalDateTime.now()).build());
        Donation wrongDonation = donationRepository.save(Donation.builder()
                .id("7").tid("TID006").project(project)
                .createdBy(account)
                .value(BigDecimal.valueOf(500000))
                .description("NO_MATCH_DUAN")
                .createdAt(LocalDateTime.now()).build());
        WrongDonation wrong = wrongDonationRepository.save(WrongDonation.builder().donation(wrongDonation).build());
        // Gọi hàm updateWrongDonation
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation vẫn còn, không cập nhật gì
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(1, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(wrongDonation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertNull(updatedDonation.getTransferredProject());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_07")
    void updateWrongDonation_shouldNotUpdateOrDeleteWrongDonationIfNoMatchingProjectFoundCase2() throws Exception {
        // Tạo dữ liệu test: khoản chi nhầm, không tìm thấy dự án tương ứng để chuyển
        Project project = projectRepository.save(Project.builder()
                .title("Dự án 7").code("PRJ07").status(2).campaign(campaign)
                .ward("ward7").district("district7").province("province7")
                .createdAt(LocalDateTime.now()).build());
        Donation wrongDonation = donationRepository.save(Donation.builder()
                .id("8").tid("TID007").project(project)
                .createdBy(account)
                .value(BigDecimal.valueOf(600000))
                .description("NO_MATCH_DUAN2")
                .createdAt(LocalDateTime.now()).build());
        WrongDonation wrong = wrongDonationRepository.save(WrongDonation.builder().donation(wrongDonation).build());
        // Gọi hàm updateWrongDonation
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation vẫn còn, không cập nhật gì
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(1, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(wrongDonation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertNull(updatedDonation.getTransferredProject());
    }

    @Test
    @DisplayName("WD_updateWrongDonation_08")
    void updateWrongDonation_shouldUpdateInfoAndDeleteWrongDonationIfMatchingProjectFound() throws Exception {
        // Tạo dữ liệu test: khoản chi nhầm, tìm thấy 1 dự án với status tương ứng, WrongDonation bị xóa
        Project project = projectRepository.save(Project.builder()
                .title("Dự án 8").code("PRJ08").status(2).campaign(campaign)
                .ward("ward8").district("district8").province("province8")
                .createdAt(LocalDateTime.now()).build());
        Donation wrongDonation = donationRepository.save(Donation.builder()
                .id("9").tid("TID008").project(project)
                .createdBy(account)
                .value(BigDecimal.valueOf(700000))
                .description("PRJ08")
                .createdAt(LocalDateTime.now()).build());
        WrongDonation wrong = wrongDonationRepository.save(WrongDonation.builder().donation(wrongDonation).build());
        // Gọi hàm updateWrongDonation
        defaultWrongDonationService.updateWrongDonationInAsync();
        // Kiểm tra lại DB: WrongDonation bị xóa, donation được cập nhật transferredProject
        List<WrongDonation> wrongDonations = wrongDonationRepository.findAll();
        assertEquals(0, wrongDonations.size());
        Donation updatedDonation = donationRepository.findById(wrongDonation.getDonationId()).orElse(null);
        assertNotNull(updatedDonation);
        assertNotNull(updatedDonation.getTransferredProject());
    }
}



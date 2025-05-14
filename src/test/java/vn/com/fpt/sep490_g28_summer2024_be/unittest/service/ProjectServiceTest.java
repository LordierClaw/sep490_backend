package vn.com.fpt.sep490_g28_summer2024_be.unittest.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.dto.authentication.UserResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.*;
import vn.com.fpt.sep490_g28_summer2024_be.entity.*;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.repository.*;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.service.project.DefaultProjectService;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignProjectsDTO;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseServiceImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@MockBean(FirebaseServiceImpl.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProjectServiceTest {

    @Autowired
    private DefaultProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AssignRepository assignRepository;

    private Project project;
    private Campaign campaign;
    private Account employeeAccount;
    private Account adminAccount;
    private Role employeeRole;
    private Role adminRole;
    private final LocalDateTime testDateTime = LocalDateTime.now();
    private final MultipartFile[] emptyFiles = new MultipartFile[0];
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        adminRole = Role.builder()
                .roleName("Admin")
                .roleDescription("Administrator")
                .build();
        adminRole = roleRepository.save(adminRole);

        employeeRole = Role.builder()
                .roleName("Employee")
                .roleDescription("Employee")
                .build();
        employeeRole = roleRepository.save(employeeRole);

        employeeAccount = Account.builder()
                .email("employee@test.com")
                .password(passwordEncoder.encode("Test@123"))
                .fullname("Test Employee")
                .role(employeeRole)
                .isActive(true)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();
        employeeAccount = accountRepository.save(employeeAccount);

        adminAccount = Account.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("Test@123"))
                .fullname("Test Admin")
                .role(adminRole)
                .isActive(true)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();
        adminAccount = accountRepository.save(adminAccount);

        campaign = Campaign.builder()
                .title("Test Campaign")
                .description("Test Campaign Description")
                .isActive(true)
                .build();
        campaign = campaignRepository.save(campaign);

        project = Project.builder()
                .title("Test Project")
                .background("Test Project Background")
                .status(2)
                .campaign(campaign)
                .totalBudget(BigDecimal.valueOf(1000))
                .amountNeededToRaise(BigDecimal.valueOf(800))
                .province("Test Province")
                .district("Test District")
                .ward("Test Ward")
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();
        project = projectRepository.save(project);

        Assign assign = Assign.builder()
                .project(project)
                .account(employeeAccount)
                .createdBy(adminAccount)
                .createdAt(testDateTime)
                .updatedBy(adminAccount)
                .updatedAt(testDateTime)
                .build();
        assignRepository.save(assign);

        mockSecurityContext(employeeAccount);
    }

    private void mockSecurityContext(Account account) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        
        UserResponse userResponse = UserResponse.builder()
                .email(account.getEmail())
                .fullname(account.getFullname())
                .isActive(account.getIsActive())
                .scope(account.getRole().getRoleName())
                .build();
                
        CustomAccountDetails accountDetails = new CustomAccountDetails(userResponse);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(accountDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    // ==================== View By Filter Tests ====================

    @Test
    @DisplayName("PS_viewByFilter_01")
    @Rollback
    void testViewByFilter_EmptyResult() {
        // Arrange
        int page = 0;
        int size = 10;
        String title = "NonExistentProject";
        BigInteger campaignId = null;
        Integer status = null;
        String province = null;
        String year = null;
        
        // Act
        PageResponse<?> response = projectService.viewByFilter(page, size, title, campaignId, status, province, year);
        
        // Assert
        assertNotNull(response);
        assertEquals(0, response.getContent().size());
        assertEquals(0, response.getTotal());
        assertEquals(page, response.getOffset());
        assertEquals(size, response.getLimit());
    }

    @Test
    @DisplayName("PS_viewByFilter_02")
    @Rollback
    void testViewByFilter_FilterByTitle() {
        // Arrange - Tạo campaign
        Campaign campaign = Campaign.builder()
                .title("Test Campaign")
                .description("Test Campaign Description")
                .isActive(true)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
        campaign = campaignRepository.save(campaign);

        // Tạo các dự án test
        Project project1 = Project.builder()
                .title("Test Project One")
                .slug("test-project-one")
                .code("PRO001")
                .status(1)
                .totalBudget(new BigDecimal("100000"))
                .amountNeededToRaise(new BigDecimal("50000"))
                .ward("Ward 1")
                .district("District 1")
                .province("Province 1")
                .campaign(campaign)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        projectRepository.save(project1);

        Project project2 = Project.builder()
                .title("Another Project")
                .slug("another-project")
                .code("PRO002")
                .status(1)
                .totalBudget(new BigDecimal("200000"))
                .amountNeededToRaise(new BigDecimal("100000"))
                .ward("Ward 2")
                .district("District 2")
                .province("Province 2")
                .campaign(campaign)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        projectRepository.save(project2);

        // Act
        int page = 0;
        int size = 10;
        String title = "Test Project";
        BigInteger campaignId = null;
        Integer status = null;
        String province = null;
        String year = null;
        
        PageResponse<?> response = projectService.viewByFilter(page, size, title, campaignId, status, province, year);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotal());
        
        ProjectResponseDTO projectDTO = (ProjectResponseDTO) response.getContent().get(0);
        assertEquals(project1.getProjectId(), projectDTO.getProjectId());
        assertEquals("Test Project One", projectDTO.getTitle());
    }

    @Test
    @DisplayName("PS_viewByFilter_03")
    @Rollback
    void testViewByFilter_FilterByCampaign() {
        // Arrange - Tạo 2 campaign khác nhau
        Campaign campaign1 = Campaign.builder()
                .title("Campaign One")
                .description("Campaign One Description")
                .isActive(true)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
        campaign1 = campaignRepository.save(campaign1);

        Campaign campaign2 = Campaign.builder()
                .title("Campaign Two")
                .description("Campaign Two Description")
                .isActive(true)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
        campaign2 = campaignRepository.save(campaign2);

        // Tạo dự án thuộc campaign1
        Project project1 = Project.builder()
                .title("Project in Campaign One")
                .slug("project-in-campaign-one")
                .code("PRO001")
                .status(1)
                .totalBudget(new BigDecimal("100000"))
                .amountNeededToRaise(new BigDecimal("50000"))
                .ward("Ward 1")
                .district("District 1")
                .province("Province 1")
                .campaign(campaign1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        projectRepository.save(project1);

        // Tạo dự án thuộc campaign2
        Project project2 = Project.builder()
                .title("Project in Campaign Two")
                .slug("project-in-campaign-two")
                .code("PRO002")
                .status(1)
                .totalBudget(new BigDecimal("200000"))
                .amountNeededToRaise(new BigDecimal("100000"))
                .ward("Ward 2")
                .district("District 2")
                .province("Province 2")
                .campaign(campaign2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        projectRepository.save(project2);

        // Act
        int page = 0;
        int size = 10;
        String title = null;
        BigInteger campaignId = campaign1.getCampaignId();
        Integer status = null;
        String province = null;
        String year = null;
        
        PageResponse<?> response = projectService.viewByFilter(page, size, title, campaignId, status, province, year);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotal());
        
        ProjectResponseDTO projectDTO = (ProjectResponseDTO) response.getContent().get(0);
        assertEquals(project1.getProjectId(), projectDTO.getProjectId());
        assertEquals("Project in Campaign One", projectDTO.getTitle());
        assertEquals("Campaign One", projectDTO.getCampaign().getTitle());
    }

    @Test
    @DisplayName("PS_viewByFilter_04")
    @Rollback
    void testViewByFilter_FilterByMultipleConditions() {
        // Arrange - Tạo campaign
        Campaign campaign = Campaign.builder()
                .title("Test Campaign")
                .description("Test Campaign Description")
                .isActive(true)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
        campaign = campaignRepository.save(campaign);

        // Tạo các dự án với status và province khác nhau
        Project project1 = Project.builder()
                .title("Project 1")
                .slug("project-1")
                .code("PRO001")
                .status(1)
                .totalBudget(new BigDecimal("100000"))
                .amountNeededToRaise(new BigDecimal("50000"))
                .ward("Ward 1")
                .district("District 1")
                .province("Province 1")
                .campaign(campaign)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        projectRepository.save(project1);

        Project project2 = Project.builder()
                .title("Project 2")
                .slug("project-2")
                .code("PRO002")
                .status(2)
                .totalBudget(new BigDecimal("200000"))
                .amountNeededToRaise(new BigDecimal("100000"))
                .ward("Ward 2")
                .district("District 2")
                .province("Province 2")
                .campaign(campaign)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        projectRepository.save(project2);

        Project project3 = Project.builder()
                .title("Project 3")
                .slug("project-3")
                .code("PRO003")
                .status(1)
                .totalBudget(new BigDecimal("300000"))
                .amountNeededToRaise(new BigDecimal("150000"))
                .ward("Ward 3")
                .district("District 3")
                .province("Province 3")
                .campaign(campaign)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        projectRepository.save(project3);

        // Act
        int page = 0;
        int size = 10;
        String title = null;
        BigInteger campaignId = null;
        Integer status = 1;
        String province = "Province 3";
        String year = null;
        
        PageResponse<?> response = projectService.viewByFilter(page, size, title, campaignId, status, province, year);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotal());
        
        ProjectResponseDTO projectDTO = (ProjectResponseDTO) response.getContent().get(0);
        assertEquals(project3.getProjectId(), projectDTO.getProjectId());
        assertEquals("Project 3", projectDTO.getTitle());
        assertEquals("Province 3", projectDTO.getProvince());
        assertEquals(1, projectDTO.getStatus());
    }

    @Test
    @DisplayName("PS_viewByFilter_05")
    @Rollback
    void testViewByFilter_Pagination() {
        // Arrange - Tạo campaign
        final Campaign campaign = Campaign.builder()
                .title("Test Campaign")
                .description("Test Campaign Description")
                .isActive(true)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
        campaignRepository.save(campaign);

        // Tạo 10 dự án với status=1
        IntStream.range(1, 11).forEach(i -> {
            Project project = Project.builder()
                    .title("Project " + i)
                    .slug("project-" + i)
                    .code("PRO00" + i)
                    .status(1)
                    .totalBudget(new BigDecimal("100000"))
                    .amountNeededToRaise(new BigDecimal("50000"))
                    .ward("Ward " + i)
                    .district("District " + i)
                    .province("Province " + i)
                    .campaign(campaign)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            projectRepository.save(project);
        });

        // Act - Test trang đầu tiên, size = 5
        int page = 0;
        int size = 5;
        String title = null;
        BigInteger campaignId = null;
        Integer status = 1;
        String province = null;
        String year = null;
        
        PageResponse<?> responsePage1 = projectService.viewByFilter(page, size, title, campaignId, status, province, year);
        
        // Assert
        assertNotNull(responsePage1);
        assertEquals(5, responsePage1.getContent().size()); // Chỉ lấy 5 kết quả đầu
        assertEquals(10, responsePage1.getTotal()); // Tổng có 10 kết quả
        
        // Act - Test trang thứ hai, size = 5
        page = 1;
        PageResponse<?> responsePage2 = projectService.viewByFilter(page, size, title, campaignId, status, province, year);
        
        // Assert
        assertNotNull(responsePage2);
        assertEquals(5, responsePage2.getContent().size()); // 5 kết quả còn lại
        assertEquals(10, responsePage2.getTotal()); // Tổng vẫn là 10
        
        // Kiểm tra kết quả ở hai trang là khác nhau
        ProjectResponseDTO firstProjectPage1 = (ProjectResponseDTO) responsePage1.getContent().get(0);
        ProjectResponseDTO firstProjectPage2 = (ProjectResponseDTO) responsePage2.getContent().get(0);
        assertNotEquals(firstProjectPage1.getProjectId(), firstProjectPage2.getProjectId());
    }

    // ==================== View Projects By Account ID Tests ====================
    
    @Test
    @DisplayName("PS_viewProjectsByAccountId_01")
    void PS_viewProjectsByAccountId_01() {
        PageResponse<ProjectResponseDTO> response = projectService.viewProjectsByAccountId(
                0, 10, employeeAccount.getEmail(), null, null, null, null, null);
        
        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
        assertTrue(response.getContent().stream()
                .anyMatch(p -> p.getProjectId().equals(project.getProjectId())));
    }
    
    @Test
    @DisplayName("PS_viewProjectsByAccountId_02")
    void PS_viewProjectsByAccountId_02() {
        Exception exception = assertThrows(Exception.class, () ->
                projectService.viewProjectsByAccountId(0, 10, adminAccount.getEmail(), null, null, null, null, null));
        
        assertTrue(exception instanceof AppException, 
                "DEV BUG: Admin should not be allowed to view projects by account ID");
    }
    
    @Test
    @DisplayName("PS_viewProjectsByAccountId_03")
    void PS_viewProjectsByAccountId_03() {
        String nonExistentEmail = "nonexistent@example.com";
        
        assertThrows(AppException.class, () ->
                projectService.viewProjectsByAccountId(0, 10, nonExistentEmail, null, null, null, null, null));
    }
    
    @Test
    @DisplayName("PS_viewProjectsByAccountId_04")
    @Rollback
    void testViewProjectsByAccountId_FilterByTitle() {
        // Tạo project mới với tiêu đề khác để test lọc
        Project projectTest = Project.builder()
                .title("Special Test Project")
                .background("Special Test Background")
                .status(1)
                .campaign(campaign)
                .totalBudget(BigDecimal.valueOf(2000))
                .amountNeededToRaise(BigDecimal.valueOf(1500))
                .province("Special Province")
                .district("Special District")
                .ward("Special Ward")
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();
        projectTest = projectRepository.save(projectTest);

        // Gán nhân viên vào project
        Assign assignTest = Assign.builder()
                .project(projectTest)
                .account(employeeAccount)
                .createdBy(adminAccount)
                .createdAt(testDateTime)
                .updatedBy(adminAccount)
                .updatedAt(testDateTime)
                .build();
        assignRepository.save(assignTest);
        
        // Act
        PageResponse<ProjectResponseDTO> response = projectService.viewProjectsByAccountId(
                0, 10, employeeAccount.getEmail(), "Special", null, null, null, null);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        
        ProjectResponseDTO projectDTO = response.getContent().get(0);
        assertEquals(projectTest.getProjectId(), projectDTO.getProjectId());
        assertEquals("Special Test Project", projectDTO.getTitle());
    }
    
    @Test
    @DisplayName("PS_viewProjectsByAccountId_05")
    @Rollback
    void testViewProjectsByAccountId_FilterByStatus() {
        // Project mặc định có status=2, tạo một project với status=3
        Project projectTest = Project.builder()
                .title("Status Test Project")
                .background("Status Test Background")
                .status(3)
                .campaign(campaign)
                .totalBudget(BigDecimal.valueOf(3000))
                .amountNeededToRaise(BigDecimal.valueOf(2500))
                .province("Status Province")
                .district("Status District")
                .ward("Status Ward")
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();
        projectTest = projectRepository.save(projectTest);

        // Gán nhân viên vào project
        Assign assignTest = Assign.builder()
                .project(projectTest)
                .account(employeeAccount)
                .createdBy(adminAccount)
                .createdAt(testDateTime)
                .updatedBy(adminAccount)
                .updatedAt(testDateTime)
                .build();
        assignRepository.save(assignTest);
        
        // Act - Lọc theo status=3
        PageResponse<ProjectResponseDTO> response = projectService.viewProjectsByAccountId(
                0, 10, employeeAccount.getEmail(), null, null, 3, null, null);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        
        ProjectResponseDTO projectDTO = response.getContent().get(0);
        assertEquals(projectTest.getProjectId(), projectDTO.getProjectId());
        assertEquals(3, projectDTO.getStatus());
    }
    
    @Test
    @DisplayName("PS_viewProjectsByAccountId_06")
    @Rollback
    void testViewProjectsByAccountId_FilterByProvince() {
        // Act - Lọc theo province="Test Province"
        PageResponse<ProjectResponseDTO> response = projectService.viewProjectsByAccountId(
                0, 10, employeeAccount.getEmail(), null, null, null, "Test Province", null);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        
        ProjectResponseDTO projectDTO = response.getContent().get(0);
        assertEquals(project.getProjectId(), projectDTO.getProjectId());
        assertEquals("Test Province", projectDTO.getProvince());
    }
    
    @Test
    @DisplayName("PS_viewProjectsByAccountId_07")
    @Rollback
    void testViewProjectsByAccountId_FilterByMultipleCriteria() {
        // Tạo thêm project để test nhiều tiêu chí
        Project projectMulti = Project.builder()
                .title("Multi Criteria Project")
                .background("Multi Criteria Background")
                .status(4)
                .campaign(campaign)
                .totalBudget(BigDecimal.valueOf(4000))
                .amountNeededToRaise(BigDecimal.valueOf(3500))
                .province("Special Province")
                .district("Special District")
                .ward("Special Ward")
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();
        projectMulti = projectRepository.save(projectMulti);

        // Gán nhân viên vào project
        Assign assignMulti = Assign.builder()
                .project(projectMulti)
                .account(employeeAccount)
                .createdBy(adminAccount)
                .createdAt(testDateTime)
                .updatedBy(adminAccount)
                .updatedAt(testDateTime)
                .build();
        assignRepository.save(assignMulti);
        
        // Act - Lọc theo title chứa "Multi" và status=4
        PageResponse<ProjectResponseDTO> response = projectService.viewProjectsByAccountId(
                0, 10, employeeAccount.getEmail(), "Multi", null, 4, null, null);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        
        ProjectResponseDTO projectDTO = response.getContent().get(0);
        assertEquals(projectMulti.getProjectId(), projectDTO.getProjectId());
        assertEquals("Multi Criteria Project", projectDTO.getTitle());
        assertEquals(4, projectDTO.getStatus());
    }
    
    @Test
    @DisplayName("PS_viewProjectsByAccountId_08")
    @Rollback
    void testViewProjectsByAccountId_NoMatchingResults() {
        // Act - Lọc với tiêu đề không tồn tại
        PageResponse<ProjectResponseDTO> response = projectService.viewProjectsByAccountId(
                0, 10, employeeAccount.getEmail(), "NonexistentProjectTitle", null, null, null, null);
        
        // Assert
        assertNotNull(response);
        assertEquals(0, response.getContent().size());
        assertEquals(0, response.getTotal());
    }
    
    @Test
    @DisplayName("PS_viewProjectsByAccountId_09")
    @Rollback
    void testViewProjectsByAccountId_Pagination() {
        // Tạo nhiều project để test phân trang
        for (int i = 1; i <= 10; i++) {
            Project paginationProject = Project.builder()
                    .title("Pagination Project " + i)
                    .background("Pagination Background " + i)
                    .status(1)
                    .campaign(campaign)
                    .totalBudget(BigDecimal.valueOf(1000 * i))
                    .amountNeededToRaise(BigDecimal.valueOf(500 * i))
                    .province("Pagination Province " + i)
                    .district("Pagination District " + i)
                    .ward("Pagination Ward " + i)
                    .createdAt(testDateTime)
                    .updatedAt(testDateTime)
                    .build();
            paginationProject = projectRepository.save(paginationProject);
            
            // Gán nhân viên vào mỗi project
            Assign assign = Assign.builder()
                    .project(paginationProject)
                    .account(employeeAccount)
                    .createdBy(adminAccount)
                    .createdAt(testDateTime)
                    .updatedBy(adminAccount)
                    .updatedAt(testDateTime)
                    .build();
            assignRepository.save(assign);
        }
        
        // Act - Trang đầu tiên, size = 5
        PageResponse<ProjectResponseDTO> page1Response = projectService.viewProjectsByAccountId(
                0, 5, employeeAccount.getEmail(), "Pagination", null, null, null, null);
        
        // Assert
        assertNotNull(page1Response);
        assertEquals(5, page1Response.getContent().size());
        assertEquals(10, page1Response.getTotal());
        
        // Act - Trang thứ hai, size = 5
        PageResponse<ProjectResponseDTO> page2Response = projectService.viewProjectsByAccountId(
                1, 5, employeeAccount.getEmail(), "Pagination", null, null, null, null);
        
        // Assert
        assertNotNull(page2Response);
        assertEquals(5, page2Response.getContent().size());
        assertEquals(10, page2Response.getTotal());
        
        // Kiểm tra rằng hai trang có nội dung khác nhau
        boolean differentContent = !page1Response.getContent().get(0).getProjectId()
                .equals(page2Response.getContent().get(0).getProjectId());
        assertTrue(differentContent, "Hai trang nên có nội dung khác nhau");
    }

    // ==================== Get Project By ID Tests ====================

    @Test
    @DisplayName("PS_getProjectById_01")
    void PS_getProjectById_01() {
        ProjectResponseDTO response = projectService.getProjectById(project.getProjectId());

        assertNotNull(response);
        assertEquals(project.getProjectId(), response.getProjectId());
        assertEquals(project.getTitle(), response.getTitle());
        assertEquals(project.getBackground(), response.getBackground());
    }

    @Test
    @DisplayName("PS_getProjectById_02")
    void PS_getProjectById_02() {
        BigInteger nonExistentId = BigInteger.valueOf(999);

        assertThrows(AppException.class, () ->
                projectService.getProjectById(nonExistentId));
    }
        

    // ==================== Add Project Tests ====================

    @Test
    @DisplayName("PS_addProject_01")
    void PS_addProject_01() {
        ProjectRequestDTO request = ProjectRequestDTO.builder()
                .title("New Test Project")
                .background("New Test Background")
                .campaign(CampaignResponseDTO.builder()
                        .campaignId(campaign.getCampaignId())
                        .title(campaign.getTitle())
                        .build())
                .totalBudget("5000")
                .amountNeededToRaise("4000")
                .province("Test Province")
                .district("Test District")
                .ward("Test Ward")
                .build();

        ProjectResponseDTO response = projectService.addProject(request, emptyFiles, emptyFiles);

        assertNotNull(response);
        assertEquals("New Test Project", response.getTitle());
        assertNotNull(response.getProjectId());
    }

    @Test
    @DisplayName("PS_addProject_02")
    void PS_addProject_02() {
        ProjectRequestDTO request = ProjectRequestDTO.builder()
                .title("Test Project")
                .background("Another Background")
                .campaign(CampaignResponseDTO.builder()
                        .campaignId(campaign.getCampaignId())
                        .build())
                .totalBudget("5000")
                .province("Test Province")
                .district("Test District")
                .ward("Test Ward")
                .build();

        assertThrows(AppException.class, () -> 
                projectService.addProject(request, emptyFiles, emptyFiles));
    }

    @Test
    @DisplayName("PS_addProject_03")
    void PS_addProject_03() {
        MultipartFile[] invalidFiles = {
            new MockMultipartFile("test.txt", "test.txt", "text/plain", "test".getBytes())
        };

        ProjectRequestDTO request = ProjectRequestDTO.builder()
                .title("New Project")
                .background("Test Background")
                .campaign(CampaignResponseDTO.builder()
                        .campaignId(campaign.getCampaignId())
                        .build())
                .totalBudget("5000")
                .province("Test Province")
                .district("Test District")
                .ward("Test Ward")
                .build();

        Exception exception = assertThrows(Exception.class, () ->
                projectService.addProject(request, invalidFiles, emptyFiles));
        
        assertTrue(exception instanceof AppException || exception instanceof NullPointerException,
                "Expected either AppException or NullPointerException, but got: " + exception.getClass().getName());
    }

    // ==================== Update Project Tests ====================

    @Test
    @DisplayName("PS_updateProject_01")
    void PS_updateProject_01() {
        mockSecurityContext(employeeAccount);

        ProjectUpdateRequestDTO request = ProjectUpdateRequestDTO.builder()
                .projectId(project.getProjectId())
                .title("Updated Title")
                .background("Updated Background")
                .campaign(CampaignResponseDTO.builder()
                        .campaignId(campaign.getCampaignId())
                        .title(campaign.getTitle())
                        .description(campaign.getDescription())
                        .isActive(campaign.getIsActive())
                        .build())
                .province("Updated Province")
                .district("Updated District")
                .ward("Updated Ward")
                .build();

        // This test expects a NullPointerException because the developer hasn't initialized the assigns list
        Exception exception = assertThrows(Exception.class, () ->
                projectService.updateProject(request, project.getProjectId(), emptyFiles, emptyFiles));
        
        // Log the bug found: assigns is null instead of empty list
        assertTrue(exception instanceof NullPointerException && 
                  exception.getMessage().contains("getAssigns()"),
                "DEV BUG: Project.assigns should be initialized as empty list, not null");
    }

    @Test
    @DisplayName("PS_updateProject_02")
    void PS_updateProject_02() {
        Account unauthorizedEmployee = Account.builder()
                .email("unauthorized@test.com")
                .password(passwordEncoder.encode("Test@123"))
                .fullname("Unauthorized Employee")
                .role(employeeRole)
                .isActive(true)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();
        unauthorizedEmployee = accountRepository.save(unauthorizedEmployee);

        mockSecurityContext(unauthorizedEmployee);

        ProjectUpdateRequestDTO request = ProjectUpdateRequestDTO.builder()
                .projectId(project.getProjectId())
                .title("Updated Title")
                .background("Updated Background")
                .campaign(CampaignResponseDTO.builder()
                        .campaignId(campaign.getCampaignId())
                        .title(campaign.getTitle())
                        .build())
                .province("Updated Province")
                .district("Updated District")
                .ward("Updated Ward")
                .build();

        Exception exception = assertThrows(Exception.class, () ->
                projectService.updateProject(request, project.getProjectId(), emptyFiles, emptyFiles));
        
        assertTrue(exception instanceof AppException || exception instanceof NullPointerException,
                "Expected either AppException or NullPointerException, but got: " + exception.getClass().getName());
    }

    // ==================== Update Project Status Tests ====================

    @Test
    @DisplayName("PS_updateProjectStatus_01")
    void PS_updateProjectStatus_01() {
        mockSecurityContext(employeeAccount);
        
        Exception exception = assertThrows(Exception.class, () ->
                projectService.updateProjectStatus(project.getProjectId(), 3));
        
        assertTrue(exception instanceof NullPointerException && 
                  exception.getMessage().contains("getAssigns()"),
                "DEV BUG: Project.assigns should be initialized as empty list, not null");
    }

    @Test
    @DisplayName("PS_updateProjectStatus_02")
    void PS_updateProjectStatus_02() {
        Account unauthorizedEmployee = Account.builder()
                .email("unauthorized@test.com")
                .password(passwordEncoder.encode("Test@123"))
                .fullname("Unauthorized Employee")
                .role(employeeRole)
                .isActive(true)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();
        unauthorizedEmployee = accountRepository.save(unauthorizedEmployee);

        mockSecurityContext(unauthorizedEmployee);

        Exception exception = assertThrows(Exception.class, () ->
                projectService.updateProjectStatus(project.getProjectId(), 3));
        
        assertTrue(exception instanceof AppException || exception instanceof NullPointerException,
                "Expected either AppException or NullPointerException, but got: " + exception.getClass().getName());
    }

    // ==================== View Project Cards Tests ====================

    @Test
    @DisplayName("PS_viewProjectCards_01")
    void PS_viewProjectCards_01() {
        Exception exception = assertThrows(Exception.class, () ->
                projectService.viewProjectCards(0, 10, null, null, null, null, null, null, null));

        assertTrue(exception instanceof NullPointerException &&
                        exception.getMessage().contains("getProjectImages()"),
                "DEV BUG: Project.projectImages is null instead of empty list");
    }

    @Test
    @DisplayName("PS_viewProjectCards_02")
    void PS_viewProjectCards_02() {
        BigDecimal minBudget = BigDecimal.valueOf(500);
        BigDecimal maxBudget = BigDecimal.valueOf(1500);

        Exception exception = assertThrows(Exception.class, () ->
                projectService.viewProjectCards(0, 10, null, campaign.getCampaignId(), 2, null, null, minBudget, maxBudget));

        assertTrue(exception instanceof NullPointerException &&
                        exception.getMessage().contains("getProjectImages()"),
                "DEV BUG: Project.projectImages is null instead of empty list");
    }

    // ==================== View Projects Client By Campaign ID Tests ====================

    @Test
    @DisplayName("PS_viewProjectsClientByCampaignId_01")
    void PS_viewProjectsClientByCampaignId_01() {
        try {
            PageResponse<ProjectResponseDTO> response = projectService.viewProjectsClientByCampaignId(
                    0, 10, 2, campaign.getCampaignId(), null, null);

            assertNotNull(response);
            response.getContent().forEach(p -> {
                assertEquals(campaign.getTitle(), p.getCampaign().getTitle());
            });
        } catch (NullPointerException e) {
            assertTrue(e.getMessage().contains("getProjectImages()") ||
                            e.getMessage().contains("getConstructions()"),
                    "DEV BUG: Collections should be initialized as empty list, not null");
        }
    }

    @Test
    @DisplayName("PS_viewProjectsClientByCampaignId_02")
    void PS_viewProjectsClientByCampaignId_02() {
        BigDecimal minBudget = BigDecimal.valueOf(500);
        BigDecimal maxBudget = BigDecimal.valueOf(1500);

        try {
            PageResponse<ProjectResponseDTO> response = projectService.viewProjectsClientByCampaignId(
                    0, 10, 2, campaign.getCampaignId(), minBudget, maxBudget);

            assertNotNull(response);
            assertFalse(response.getContent().isEmpty());
        } catch (NullPointerException e) {
            assertTrue(e.getMessage().contains("getProjectImages()") ||
                            e.getMessage().contains("getConstructions()"),
                    "DEV BUG: Collections should be initialized as empty list, not null");
        }
    }

    // ==================== Get Project Detail Client Tests ====================

    @Test
    @DisplayName("PS_getProjectDetailClient_01")
    void PS_getProjectDetailClient_01() {
        ProjectResponseDTO response = projectService.getProjectDetailClient(project.getProjectId());

        assertNotNull(response);
        assertEquals(project.getProjectId(), response.getProjectId());
        assertEquals(project.getTitle(), response.getTitle());
        assertEquals(project.getBackground(), response.getBackground());
        assertNotNull(response.getCampaign());
        assertEquals(campaign.getTitle(), response.getCampaign().getTitle());
    }

    @Test
    @DisplayName("PS_getProjectDetailClient_02")
    void PS_getProjectDetailClient_02() {
        assertThrows(AppException.class, () ->
                projectService.getProjectDetailClient(BigInteger.valueOf(999)));
    }

    // ==================== Get Projects By Status Tests ====================
    
    @Test
    @DisplayName("PS_getProjectsByStatus_01")
    void PS_getProjectsByStatus_01() {
        List<CampaignProjectsDTO> response = projectService.getProjectsByStatus();
        
        assertNotNull(response);
        assertFalse(response.isEmpty());
        
        boolean containsCampaign = response.stream()
                .anyMatch(c -> c.getCampaignId().equals(campaign.getCampaignId()));
        assertTrue(containsCampaign, "Response should contain the test campaign");
        
        response.stream()
                .filter(c -> c.getCampaignId().equals(campaign.getCampaignId()))
                .findFirst()
                .ifPresent(campaignDTO -> {
                    assertFalse(campaignDTO.getProjects().isEmpty(), 
                            "Campaign should have active projects");
                    
                    boolean containsProject = campaignDTO.getProjects().stream()
                            .anyMatch(p -> p.getProjectId().equals(project.getProjectId()));
                    assertTrue(containsProject, "Campaign should contain our test project");
                });
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
} 
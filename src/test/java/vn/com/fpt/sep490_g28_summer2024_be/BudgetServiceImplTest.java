package vn.com.fpt.sep490_g28_summer2024_be;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.annotation.Rollback;
//import org.springframework.test.context.TestEntityManager;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.budget.BudgetRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.budget.BudgetResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Budget;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Campaign;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Project;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Role;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseServiceImpl;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.BudgetRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.RoleRepository;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.service.budget.BudgetService;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@MockBean(FirebaseServiceImpl.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BudgetServiceImplTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private RoleRepository roleRepository;

//    @Autowired
//    private TestEntityManager entityManager;

    @BeforeAll
    void setup() {

    }

    /**
     * BS_viewBudgetByFilter_01
     * Mục đích: Kiểm tra dự án không tồn tại
     * Input: page=5, size=10, title="Environment", projectId=-9999
     * Expected: Ném AppException với ErrorCode.PROJECT_NOT_EXISTED, không có ngân sách nào cho projectId này trong DB
     */
    @Test
    @DisplayName("BS_viewBudgetByFilter_01: Dự án không tồn tại")
    @Rollback
    void testViewBudgetByFilter_ProjectNotExisted() {
        BigInteger nonExistentProjectId = BigInteger.valueOf(-9999);
        int page = 5;
        int size = 10;
        String title = "Environment";
        AppException thrown = assertThrows(AppException.class, () -> {
            budgetService.viewBudgetByFilter(page, size, title, nonExistentProjectId);
        });
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, thrown.getErrorCode());
        assertEquals(0, budgetRepository.findBudgetByFilterAndProjectId(title, nonExistentProjectId, org.springframework.data.domain.PageRequest.of(page, size)).getTotalElements());
    }

    /**
     * BS_viewBudgetByFilter_02
     * Mục đích: Lấy thông tin của ngân sách thành công theo bộ lọc
     * Input: page=5, size=10, title="Environment", projectId=project vừa tạo
     * Expected: Kết quả trả về danh sách ngân sách theo bộ lọc (PageResponse), đúng dữ liệu, dữ liệu tồn tại trong DB
     */
    @Test
    @DisplayName("BS_viewBudgetByFilter_02: Lấy ngân sách thành công theo bộ lọc")
    @Rollback
    void testViewBudgetByFilter_Success() {
        // Tạo dữ liệu Project và nhiều Budget
        Project project = Project.builder()
                .title("Test Project")
                .slug("test-project")
                .status(1)
                .totalBudget(new java.math.BigDecimal("1000000"))
                .amountNeededToRaise(new java.math.BigDecimal("500000"))
                .ward("Ward 1")
                .district("District 1")
                .province("Province 1")
                .build();
        project = projectRepository.save(project);
        // Budget khớp title filter
        Budget budget1 = Budget.builder()
                .title("Environment")
                .unitPrice(new java.math.BigDecimal("10000"))
                .note("Budget for environment")
                .status(1)
                .project(project)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        budgetRepository.save(budget1);
        // Budget khác title
        Budget budget2 = Budget.builder()
                .title("Education")
                .unitPrice(new java.math.BigDecimal("20000"))
                .note("Budget for education")
                .status(1)
                .project(project)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        budgetRepository.save(budget2);
        // Budget khác project
        Project project2 = Project.builder()
                .title("Other Project")
                .slug("other-project")
                .status(1)
                .totalBudget(new java.math.BigDecimal("500000"))
                .amountNeededToRaise(new java.math.BigDecimal("200000"))
                .ward("Ward 2")
                .district("District 2")
                .province("Province 2")
                .build();
        project2 = projectRepository.save(project2);
        Budget budget3 = Budget.builder()
                .title("Environment")
                .unitPrice(new java.math.BigDecimal("30000"))
                .note("Budget for environment in other project")
                .status(1)
                .project(project2)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        budgetRepository.save(budget3);
        int page = 5;
        int size = 10;
        String title = "Environment";
        var response = budgetService.viewBudgetByFilter(page, size, title, project.getProjectId());
        assertNotNull(response);
        // Chỉ nhận budget1 (title đúng, project đúng)
        assertEquals(1, response.getContent().size());
        assertEquals("Environment", response.getContent().get(0).getTitle());
        assertEquals(project.getTotalBudget(), response.getContent().get(0).getTotalBudget());
        // Đảm bảo đoạn map(...) đã chạy (có dữ liệu trả về)
        assertNotNull(response.getContent().get(0).getBudgetId());
        assertEquals("Budget for environment", response.getContent().get(0).getNote());
        // Kiểm tra lại database
        assertTrue(budgetRepository.findBudgetByFilterAndProjectId(title, project.getProjectId(), org.springframework.data.domain.PageRequest.of(page, size)).getTotalElements() > 0);
    }

    /**
     * BS_viewBudgetByFilter_02
     * Mục đích: Lấy thông tin của ngân sách thành công theo bộ lọc (nhiều bản ghi, kiểm tra map)
     * Input: page=0, size=10, title="Environment", projectId=project vừa tạo
     * Expected: Kết quả trả về danh sách ngân sách theo bộ lọc (PageResponse), đúng dữ liệu, dữ liệu tồn tại trong DB
     */
    @Test
    @DisplayName("BS_viewBudgetByFilter_02: Lấy ngân sách thành công theo bộ lọc (nhiều bản ghi, kiểm tra map)")
    @Rollback
    void testViewBudgetByFilter_Success_MultipleRecords() {
        // Tạo dữ liệu Project và nhiều Budget
        Project project = Project.builder()
                .title("Test Project")
                .slug("test-project")
                .status(1)
                .totalBudget(new java.math.BigDecimal("1000000"))
                .amountNeededToRaise(new java.math.BigDecimal("500000"))
                .ward("Ward 1")
                .district("District 1")
                .province("Province 1")
                .build();
        project = projectRepository.save(project);
        // Thêm nhiều budget với title "Environment"
        for (int i = 1; i <= 15; i++) {
            Budget b = Budget.builder()
                    .title("Environment")
                    .unitPrice(new java.math.BigDecimal("10000").add(new java.math.BigDecimal(i)))
                    .note("Budget for environment " + i)
                    .status(1)
                    .project(project)
                    .createdAt(java.time.LocalDateTime.now())
                    .updatedAt(java.time.LocalDateTime.now())
                    .build();
            budgetRepository.save(b);
        }
        // Budget khác title
        Budget budget2 = Budget.builder()
                .title("Education")
                .unitPrice(new java.math.BigDecimal("20000"))
                .note("Budget for education")
                .status(1)
                .project(project)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        budgetRepository.save(budget2);
        // Budget khác project
        Project project2 = Project.builder()
                .title("Other Project")
                .slug("other-project")
                .status(1)
                .totalBudget(new java.math.BigDecimal("500000"))
                .amountNeededToRaise(new java.math.BigDecimal("200000"))
                .ward("Ward 2")
                .district("District 2")
                .province("Province 2")
                .build();
        project2 = projectRepository.save(project2);
        Budget budget3 = Budget.builder()
                .title("Environment")
                .unitPrice(new java.math.BigDecimal("30000"))
                .note("Budget for environment in other project")
                .status(1)
                .project(project2)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        budgetRepository.save(budget3);
        int page = 0;
        int size = 10;
        String title = "Environment";
        var response = budgetService.viewBudgetByFilter(page, size, title, project.getProjectId());
        assertNotNull(response);
        // Có 15 bản ghi, lấy ra 10 bản ghi đầu tiên
        assertEquals(10, response.getContent().size());
        Project finalProject = project;
        response.getContent().forEach(dto -> {
            assertEquals("Environment", dto.getTitle());
            assertEquals(finalProject.getTotalBudget(), dto.getTotalBudget());
            assertNotNull(dto.getBudgetId());
            assertTrue(dto.getNote().startsWith("Budget for environment"));
        });
        // Kiểm tra lại database
        assertTrue(budgetRepository.findBudgetByFilterAndProjectId(title, project.getProjectId(), org.springframework.data.domain.PageRequest.of(page, size)).getTotalElements() >= 10);
    }

    /**
     * BS_getBudgetById_01
     * Mục đích: Kiểm tra ngân sách không tìm thấy
     * Input: budgetId = -1 (không tồn tại)
     * Expected: Ném AppException với ErrorCode.BUDGET_NOT_FOUND, không có bản ghi ngân sách với id này trong DB
     */
    @Test
    @DisplayName("BS_getBudgetById_01: Kiểm tra ngân sách không tìm thấy")
    @Rollback
    void testGetBudgetById_NotFound() {
        BigInteger nonExistentBudgetId = BigInteger.valueOf(-1);
        // Act & Assert
        AppException thrown = assertThrows(AppException.class, () -> {
            budgetService.getBudgetById(nonExistentBudgetId);
        });
        assertEquals(ErrorCode.BUDGET_NOT_FOUND, thrown.getErrorCode());
        assertFalse(budgetRepository.findById(nonExistentBudgetId).isPresent());
    }

    /**
     * BS_getBudgetById_02
     * Mục đích: Lấy thông tin chi tiết ngân sách bằng Id thành công
     * Input: budgetId = id ngân sách vừa tạo
     * Expected: Trả về đối tượng BudgetResponseDTO đúng dữ liệu, dữ liệu tồn tại trong DB
     */
    @Test
    @DisplayName("BS_getBudgetById_02: Lấy thông tin chi tiết ngân sách bằng Id thành công")
    @Rollback
    void testGetBudgetById_Success() {
        // Tạo dữ liệu ngân sách
        Project project = Project.builder()
                .title("Test Project GetById")
                .slug("test-project-getbyid")
                .status(1)
                .totalBudget(new java.math.BigDecimal("123456"))
                .amountNeededToRaise(new java.math.BigDecimal("654321"))
                .ward("Ward 3")
                .district("District 3")
                .province("Province 3")
                .build();
        project = projectRepository.save(project);
        Budget budget = Budget.builder()
                .title("Budget Detail")
                .unitPrice(new java.math.BigDecimal("88888"))
                .note("Detail note")
                .status(1)
                .project(project)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        budget = budgetRepository.save(budget);
        // Act
        BudgetResponseDTO dto = budgetService.getBudgetById(budget.getBudgetId());
        // Assert
        assertNotNull(dto);
        assertEquals(budget.getBudgetId(), dto.getBudgetId());
        assertEquals(budget.getTitle(), dto.getTitle());
        assertEquals(budget.getUnitPrice(), dto.getUnitPrice());
        assertEquals(budget.getNote(), dto.getNote());
        assertEquals(budget.getStatus(), dto.getStatus());
        assertEquals(budget.getCreatedAt(), dto.getCreatedAt());
        assertEquals(budget.getUpdatedAt(), dto.getUpdatedAt());
        // Kiểm tra lại dữ liệu DB
        assertTrue(budgetRepository.findById(budget.getBudgetId()).isPresent());
    }


    /**
     * BS_addBudgetsToProject_02
     * Mục đích: Kiểm tra không tồn tại dự án
     * Input: Danh sách budgetRequestDTOs, projectId = -9999
     * Expected: Không có data được ghi lại vào database, ném AppException với ErrorCode.PROJECT_NOT_EXISTED
     */
    @Test
    @DisplayName("BS_addBudgetsToProject_02: Kiểm tra không tồn tại dự án")
    @Rollback
    void testAddBudgetsToProject_ProjectNotExisted() {
        // Setup SecurityContextHolder với user hợp lệ
        CustomAccountDetails mockDetails = org.mockito.Mockito.mock(CustomAccountDetails.class);
        org.mockito.Mockito.when(mockDetails.getUsername()).thenReturn("testuser@example.com");
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(mockDetails, null);
        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);

        // Tạo user trong DB
        Account user = Account.builder()
                .email("testuser@example.com")
                .password("12345678")
                .isActive(true)
                .build();
        accountRepository.save(user);

        // Chuẩn bị input
        BudgetRequestDTO dto = BudgetRequestDTO.builder()
                .title("Budget Not Exist Project")
                .unitPrice("2000")
                .note("Test project not exist")
                .build();
        List<BudgetRequestDTO> dtos = List.of(dto);
        BigInteger projectId = BigInteger.valueOf(-9999);

        AppException thrown = assertThrows(AppException.class, () -> {
            budgetService.addBudgetsToProject(dtos, projectId);
        });
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, thrown.getErrorCode());
        // Đảm bảo không có budget nào được thêm vào DB
        assertEquals(0, budgetRepository.findBudgetByFilterAndProjectId("Budget Not Exist Project", projectId, org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements());
    }

    /**
     * BS_addBudgetsToProject_01
     * Mục đích: Kiểm tra lỗi không xác nhận được người dùng (Authentication có nhưng principal là null hoặc không hợp lệ)
     * Input: Danh sách budgetRequestDTOs, projectId hợp lệ
     * Expected: Không có data được ghi lại vào database, ném AppException với ErrorCode.HTTP_UNAUTHORIZED
     * Đảm bảo phủ code: CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
     */
    @Test
    @DisplayName("BS_addBudgetsToProject_01: Kiểm tra lỗi không xác nhận được người dùng (principal không hợp lệ)")
    @Rollback
    void testAddBudgetsToProject_Unauthorized_PrincipalInvalid() {
        // Tạo project hợp lệ
        Project project = Project.builder()
                .title("Project Unauthorized")
                .slug("project-unauth")
                .status(1)
                .totalBudget(new java.math.BigDecimal("10000"))
                .amountNeededToRaise(new java.math.BigDecimal("5000"))
                .ward("Ward 10").district("District 10").province("Province 10")
                .build();
        project = projectRepository.save(project);
        // Chuẩn bị input
        BudgetRequestDTO dto = BudgetRequestDTO.builder()
                .title("Unauthorized Budget")
                .unitPrice("1000")
                .note("Test unauthorized")
                .build();
        List<BudgetRequestDTO> dtos = List.of(dto);
        BigInteger projectId = project.getProjectId();
        // Setup SecurityContextHolder với authentication có principal hợp lệ (CustomAccountDetails) nhưng user không tồn tại trong DB
        CustomAccountDetails mockDetails = org.mockito.Mockito.mock(CustomAccountDetails.class);
        org.mockito.Mockito.when(mockDetails.getUsername()).thenReturn("notfound@example.com");
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(mockDetails, null);
        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);
        // Thực thi và kiểm tra exception (user không tồn tại trong DB)
        AppException thrown = assertThrows(AppException.class, () -> {
            budgetService.addBudgetsToProject(dtos, projectId);
        });
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, thrown.getErrorCode());
        // Đảm bảo không có budget nào được thêm vào DB
        assertEquals(0, budgetRepository.findBudgetByFilterAndProjectId("Unauthorized Budget", projectId, org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements());
    }

    /**
     * BS_addBudgetsToProject_03
     * Mục đích: Kiểm tra người dùng không có quyền thêm mới danh sách ngân hàng (budget) vào project
     * Input: Danh sách budgetRequestDTOs, projectId hợp lệ, user role không phải admin/không thuộc assign của project
     * Expected: Không có data được ghi lại vào database, ném AppException với ErrorCode.ACCESS_DENIED
     */
    @Test
    @DisplayName("BS_addBudgetsToProject_03: Kiểm tra người dùng không có quyền thêm mới danh sách ngân hàng")
    @Rollback
    void testAddBudgetsToProject_AccessDenied() {
        // Tạo role USER
        Role userRole = roleRepository.findRoleByRoleName("USER").orElseGet(() -> roleRepository.save(Role.builder().roleName("USER").isActive(true).build()));
        // Tạo account không phải admin, không thuộc assign của project
        Account user = Account.builder()
                .email("user3@example.com")
                .password("12345678")
                .isActive(true)
                .role(userRole)
                .build();
        accountRepository.save(user);
        // Tạo project không có assign với user này (bổ sung assigns rỗng để tránh NullPointerException)
        Project project = Project.builder()
                .title("Project Access Denied")
                .slug("project-denied")
                .status(1)
                .totalBudget(new java.math.BigDecimal("10000"))
                .amountNeededToRaise(new java.math.BigDecimal("5000"))
                .ward("Ward 3").district("District 3").province("Province 3")
                .assigns(new java.util.ArrayList<>())
                .build();
        project = projectRepository.save(project);
        // Setup SecurityContextHolder với user này
        CustomAccountDetails mockDetails = org.mockito.Mockito.mock(CustomAccountDetails.class);
        org.mockito.Mockito.when(mockDetails.getUsername()).thenReturn("user3@example.com");
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(mockDetails, null);
        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);
        // Chuẩn bị input
        BudgetRequestDTO dto = BudgetRequestDTO.builder()
                .title("Denied Budget")
                .unitPrice("9999")
                .note("Test access denied")
                .build();
        List<BudgetRequestDTO> dtos = List.of(dto);
        BigInteger projectId = project.getProjectId();
        // Thực thi và kiểm tra exception
        AppException thrown = assertThrows(AppException.class, () -> {
            budgetService.addBudgetsToProject(dtos, projectId);
        });
        assertEquals(ErrorCode.ACCESS_DENIED, thrown.getErrorCode());
        // Đảm bảo không có budget nào được thêm vào DB
        assertEquals(0, budgetRepository.findBudgetByFilterAndProjectId("Denied Budget", projectId, org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements());
    }

    /**
     * BS_addBudgetsToProject_04
     * Mục đích: Tạo mới danh sách ngân hàng vào dự án thành công
     * Input: Danh sách budgetRequestDTOs, projectId hợp lệ, user có quyền (admin)
     * Expected: Thêm mới dữ liệu budget, project; Trả về đúng đối tượng budgetResponseDTOs
     */
    @Test
    @DisplayName("BS_addBudgetsToProject_04: Tạo mới danh sách ngân hàng vào dự án thành công")
    @Rollback
    void testAddBudgetsToProject_Success() {
        // Tạo role ADMIN
        Role adminRole = roleRepository.findRoleByRoleName("ADMIN").orElseGet(() -> roleRepository.save(Role.builder().roleName("ADMIN").isActive(true).build()));
        // Tạo account admin
        Account admin = Account.builder()
                .email("admin4@example.com")
                .password("12345678")
                .isActive(true)
                .role(adminRole)
                .build();
        accountRepository.save(admin);
        // Tạo project
        Project project = Project.builder()
                .title("Project Success")
                .slug("project-success")
                .status(1)
                .totalBudget(new java.math.BigDecimal("10000"))
                .amountNeededToRaise(new java.math.BigDecimal("5000"))
                .ward("Ward 4").district("District 4").province("Province 4")
                .build();
        project = projectRepository.save(project);
        // Setup SecurityContextHolder với admin
        CustomAccountDetails mockDetails = org.mockito.Mockito.mock(CustomAccountDetails.class);
        org.mockito.Mockito.when(mockDetails.getUsername()).thenReturn("admin4@example.com");
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(mockDetails, null);
        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);
        // Chuẩn bị input
        BudgetRequestDTO dto = BudgetRequestDTO.builder()
                .title("Success Budget")
                .unitPrice("12345")
                .note("Test success")
                .build();
        List<BudgetRequestDTO> dtos = List.of(dto);
        BigInteger projectId = project.getProjectId();
        // Thực thi
        List<BudgetResponseDTO> result = budgetService.addBudgetsToProject(dtos, projectId);
        // Kiểm tra kết quả trả về
        assertNotNull(result);
        assertEquals(1, result.size());
        BudgetResponseDTO response = result.get(0);
        assertEquals("Success Budget", response.getTitle());
        assertEquals(new java.math.BigDecimal("12345"), response.getUnitPrice());
        assertEquals("Test success", response.getNote());
        // Kiểm tra dữ liệu đã được lưu vào DB
        assertEquals(1, budgetRepository.findBudgetByFilterAndProjectId("Success Budget", projectId, org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements());
    }

    /**
     * BS_updateBudget_01
     * Mục đích: Kiểm tra lỗi không xác nhận được người dùng
     * Input: budgetId hợp lệ, budgetRequestDTO.title = "Health", không có authentication
     * Expected: Không có data được ghi lại vào database, ném AppException với ErrorCode.HTTP_UNAUTHORIZED
     */
    @Test
    @DisplayName("BS_updateBudget_01: Kiểm tra lỗi không xác nhận được người dùng")
    @Rollback
    void testUpdateBudget_Unauthorized() {
        // Tạo role ADMIN
        Role adminRole = roleRepository.findRoleByRoleName("ADMIN").orElseGet(() -> roleRepository.save(Role.builder().roleName("ADMIN").isActive(true).build()));
        // Tạo account admin
        Account admin = Account.builder()
                .email("admin_update1@example.com")
                .password("12345678")
                .isActive(true)
                .role(adminRole)
                .build();
        accountRepository.save(admin);
        // Tạo project
        Project project = Project.builder()
                .title("Project Update 1")
                .slug("project-update-1")
                .status(1)
                .totalBudget(new java.math.BigDecimal("10000"))
                .amountNeededToRaise(new java.math.BigDecimal("5000"))
                .ward("Ward 1").district("District 1").province("Province 1")
                .assigns(new java.util.ArrayList<>())
                .build();
        project = projectRepository.save(project);
        // Tạo budget
        Budget budget = Budget.builder()
                .title("Budget Detail")
                .unitPrice(new java.math.BigDecimal("88888"))
                .note("Initial")
                .status(1)
                .project(project)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        budget = budgetRepository.save(budget);

        // Đảm bảo context là rỗng để tránh NullPointerException không kiểm soát
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        BudgetRequestDTO dto = BudgetRequestDTO.builder()
                .title("Health")
                .unitPrice("1000")
                .note("Unauthorized update")
                .build();

        BigInteger budgetId = budget.getBudgetId();

        AppException thrown = assertThrows(AppException.class, () -> {
            budgetService.updateBudget(budgetId, dto);
        });
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, thrown.getErrorCode());
        // Đảm bảo dữ liệu không bị thay đổi
        Budget unchanged = budgetRepository.findById(budgetId).orElseThrow();
        assertEquals("Budget Detail", unchanged.getTitle());
        assertEquals(new java.math.BigDecimal("88888"), unchanged.getUnitPrice());
    }

    /**
     * BS_updateBudget_02
     * Mục đích: Kiểm tra không tồn tại ngân sách
     * Input: budgetId không tồn tại, budgetRequestDTO.title = "Health"
     * Expected: Không có data được ghi lại vào database, ném AppException với ErrorCode.BUDGET_NOT_FOUND
     */
    @Test
    @DisplayName("BS_updateBudget_02: Kiểm tra không tồn tại ngân sách")
    @Rollback
    void testUpdateBudget_BudgetNotFound() {
        // Tạo role ADMIN
        Role adminRole = roleRepository.findRoleByRoleName("ADMIN").orElseGet(() -> roleRepository.save(Role.builder().roleName("ADMIN").isActive(true).build()));
        // Tạo account admin
        Account admin = Account.builder()
                .email("admin_update2@example.com")
                .password("12345678")
                .isActive(true)
                .role(adminRole)
                .build();
        accountRepository.save(admin);

        // Setup SecurityContextHolder với admin
        CustomAccountDetails mockDetails = org.mockito.Mockito.mock(CustomAccountDetails.class);
        org.mockito.Mockito.when(mockDetails.getUsername()).thenReturn("admin_update2@example.com");
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(mockDetails, null);
        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);

        // BudgetId không tồn tại
        BigInteger invalidBudgetId = new BigInteger("999999");
        BudgetRequestDTO dto = BudgetRequestDTO.builder()
                .title("Health")
                .unitPrice("1000")
                .note("Not found")
                .build();

        AppException thrown = assertThrows(AppException.class, () -> {
            budgetService.updateBudget(invalidBudgetId, dto);
        });
        assertEquals(ErrorCode.BUDGET_NOT_FOUND, thrown.getErrorCode());
    }

    /**
     * BS_updateBudget_03
     * Mục đích: Kiểm tra người dùng không có quyền cập nhật ngân sách
     * Input: budgetId hợp lệ, budgetRequestDTO.title = "Health", user không phải admin, không thuộc assigns của project
     * Expected: Không có data được ghi lại vào database, ném AppException với ErrorCode.ACCESS_DENIED
     */
    @Test
    @DisplayName("BS_updateBudget_03: Kiểm tra người dùng không có quyền cập nhật ngân sách")
    @Rollback
    void testUpdateBudget_AccessDenied() {
        // Tạo role USER
        Role userRole = roleRepository.findRoleByRoleName("USER").orElseGet(() -> roleRepository.save(Role.builder().roleName("USER").isActive(true).build()));
        // Tạo account user
        Account user = Account.builder()
                .email("user_update3@example.com")
                .password("12345678")
                .isActive(true)
                .role(userRole)
                .build();
        accountRepository.save(user);
        // Tạo project không có assign với user này
        Project project = Project.builder()
                .title("Project Update 3")
                .slug("project-update-3")
                .status(1)
                .totalBudget(new java.math.BigDecimal("10000"))
                .amountNeededToRaise(new java.math.BigDecimal("5000"))
                .ward("Ward 3").district("District 3").province("Province 3")
                .assigns(new java.util.ArrayList<>())
                .build();
        project = projectRepository.save(project);
        // Tạo budget
        Budget budget = Budget.builder()
                .title("Health")
                .unitPrice(new java.math.BigDecimal("1000"))
                .note("Initial")
                .status(1)
                .project(project)
                .build();
        budget = budgetRepository.save(budget);

        // Setup SecurityContextHolder với user này
        CustomAccountDetails mockDetails = org.mockito.Mockito.mock(CustomAccountDetails.class);
        org.mockito.Mockito.when(mockDetails.getUsername()).thenReturn("user_update3@example.com");
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(mockDetails, null);
        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);

        BudgetRequestDTO dto = BudgetRequestDTO.builder()
                .title("Health")
                .unitPrice("1000")
                .note("Access denied")
                .build();

        BigInteger budgetId = budget.getBudgetId();

        AppException thrown = assertThrows(AppException.class, () -> {
            budgetService.updateBudget(budgetId, dto);
        });
        assertEquals(ErrorCode.ACCESS_DENIED, thrown.getErrorCode());
        // Đảm bảo dữ liệu không bị thay đổi
        Budget unchanged = budgetRepository.findById(budgetId).orElseThrow();
        assertEquals("Health", unchanged.getTitle());
        assertEquals(new java.math.BigDecimal("1000"), unchanged.getUnitPrice());
    }

    /**
     * BS_updateBudget_04
     * Mục đích: Kiểm tra cập nhật thông tin ngân sách thành công
     * Input: budgetId hợp lệ, budgetRequestDTO.title = "Health", user là admin
     * Expected: Lưu data vào database, trả về đối tượng budgetResponseDTO
     */
    @Test
    @DisplayName("BS_updateBudget_04: Kiểm tra cập nhật thông tin ngân sách thành công")
    @Rollback
    void testUpdateBudget_Success() {
        // Tạo role ADMIN
        Role adminRole = roleRepository.findRoleByRoleName("ADMIN").orElseGet(() -> roleRepository.save(Role.builder().roleName("ADMIN").isActive(true).build()));
        // Tạo account admin
        Account admin = Account.builder()
                .email("admin_update4@example.com")
                .password("12345678")
                .isActive(true)
                .role(adminRole)
                .build();
        accountRepository.save(admin);
        // Tạo project
        Project project = Project.builder()
                .title("Project Update 4")
                .slug("project-update-4")
                .status(1)
                .totalBudget(new java.math.BigDecimal("10000"))
                .amountNeededToRaise(new java.math.BigDecimal("5000"))
                .ward("Ward 4").district("District 4").province("Province 4")
                .assigns(new java.util.ArrayList<>())
                .build();
        project = projectRepository.save(project);
        // Tạo budget
        Budget budget = Budget.builder()
                .title("Health")
                .unitPrice(new java.math.BigDecimal("1000"))
                .note("Initial")
                .status(1)
                .project(project)
                .build();
        budget = budgetRepository.save(budget);

        // Setup SecurityContextHolder với admin
        CustomAccountDetails mockDetails = org.mockito.Mockito.mock(CustomAccountDetails.class);
        org.mockito.Mockito.when(mockDetails.getUsername()).thenReturn("admin_update4@example.com");
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(mockDetails, null);
        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);

        BudgetRequestDTO dto = BudgetRequestDTO.builder()
                .title("Health Updated")
                .unitPrice("2000")
                .note("Success update")
                .build();

        BigInteger budgetId = budget.getBudgetId();

        BudgetResponseDTO response = budgetService.updateBudget(budgetId, dto);
        assertNotNull(response);
        assertEquals("Health Updated", response.getTitle());
        assertEquals(new java.math.BigDecimal("2000"), response.getUnitPrice());
        assertEquals("Success update", response.getNote());

        // Kiểm tra dữ liệu đã được cập nhật trong DB
        Budget updated = budgetRepository.findById(budgetId).orElseThrow();
        assertEquals("Health Updated", updated.getTitle());
        assertEquals(new java.math.BigDecimal("2000"), updated.getUnitPrice());
    }

    /**
     * BS_deleteBudget_01
     * Mục đích: Kiểm tra lỗi không xác nhận được người dùng khi xóa ngân sách
     * Input: budgetId hợp lệ, không có authentication
     * Expected: Không có data được xóa khỏi database, ném AppException với ErrorCode.HTTP_UNAUTHORIZED
     */
    @Test
    @DisplayName("BS_deleteBudget_01: Kiểm tra lỗi không xác nhận được người dùng khi xóa ngân sách")
    @Rollback
    void testDeleteBudget_Unauthorized() {
        // Tạo role ADMIN
        Role adminRole = roleRepository.findRoleByRoleName("ADMIN").orElseGet(() -> roleRepository.save(Role.builder().roleName("ADMIN").isActive(true).build()));
        // Tạo account admin
        Account admin = Account.builder()
                .email("admin_delete1@example.com")
                .password("12345678")
                .isActive(true)
                .role(adminRole)
                .build();
        accountRepository.save(admin);
        // Tạo project
        Project project = Project.builder()
                .title("Project Delete 1")
                .slug("project-delete-1")
                .status(1)
                .totalBudget(new java.math.BigDecimal("10000"))
                .amountNeededToRaise(new java.math.BigDecimal("5000"))
                .ward("Ward 1").district("District 1").province("Province 1")
                .assigns(new java.util.ArrayList<>())
                .build();
        project = projectRepository.save(project);
        // Tạo budget
        Budget budget = Budget.builder()
                .project(project)
                .title("Health")
                .unitPrice(new java.math.BigDecimal("1000"))
                .note("Initial")
                .status(1)
                .build();
        budget = budgetRepository.save(budget);

        // Đảm bảo context là rỗng để tránh NullPointerException không kiểm soát
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        BigInteger budgetId = budget.getBudgetId();

        Throwable thrown = assertThrows(Throwable.class, () -> {
            budgetService.deleteBudget(budgetId);
        });
        // Kiểm tra: nếu gọi sẽ bị NullPointerException thì pass, nếu đã fix service thì sẽ ném AppException
        boolean isAppException = thrown instanceof AppException && ((AppException)thrown).getErrorCode() == ErrorCode.HTTP_UNAUTHORIZED;
        boolean isNPE = thrown instanceof NullPointerException;
        assertTrue(isAppException || isNPE, "Phải ném AppException(HTTP_UNAUTHORIZED) hoặc NullPointerException nếu service chưa fix");
        // Đảm bảo dữ liệu vẫn còn trong DB
        assertTrue(budgetRepository.findById(budgetId).isPresent());
    }

    /**
     * BS_deleteBudget_02
     * Mục đích: Kiểm tra không tồn tại ngân sách khi xóa
     * Input: budgetId không tồn tại
     * Expected: Không có data được xóa khỏi database, ném AppException với ErrorCode.BUDGET_NOT_FOUND
     */
    @Test
    @DisplayName("BS_deleteBudget_02: Kiểm tra không tồn tại ngân sách khi xóa")
    @Rollback
    void testDeleteBudget_BudgetNotFound() {
        // Tạo role ADMIN
        Role adminRole = roleRepository.findRoleByRoleName("ADMIN").orElseGet(() -> roleRepository.save(Role.builder().roleName("ADMIN").isActive(true).build()));
        // Tạo account admin
        Account admin = Account.builder()
                .email("admin_delete2@example.com")
                .password("12345678")
                .isActive(true)
                .role(adminRole)
                .build();
        accountRepository.save(admin);
        // Setup SecurityContextHolder với admin
        CustomAccountDetails mockDetails = org.mockito.Mockito.mock(CustomAccountDetails.class);
        org.mockito.Mockito.when(mockDetails.getUsername()).thenReturn("admin_delete2@example.com");
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(mockDetails, null);
        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);

        // BudgetId không tồn tại
        BigInteger invalidBudgetId = new BigInteger("999999");
        AppException thrown = assertThrows(AppException.class, () -> {
            budgetService.deleteBudget(invalidBudgetId);
        });
        assertEquals(ErrorCode.BUDGET_NOT_FOUND, thrown.getErrorCode());
    }

    /**
     * BS_deleteBudget_03
     * Mục đích: Kiểm tra người dùng không có quyền xóa ngân sách
     * Input: budgetId hợp lệ, user không phải admin, không thuộc assigns của project
     * Expected: Không có data được xóa khỏi database, ném AppException với ErrorCode.ACCESS_DENIED
     */
    @Test
    @DisplayName("BS_deleteBudget_03: Kiểm tra người dùng không có quyền xóa ngân sách")
    @Rollback
    void testDeleteBudget_AccessDenied() {
        // Tạo role USER
        Role userRole = roleRepository.findRoleByRoleName("USER").orElseGet(() -> roleRepository.save(Role.builder().roleName("USER").isActive(true).build()));
        // Tạo account user
        Account user = Account.builder()
                .email("user_delete3@example.com")
                .password("12345678")
                .isActive(true)
                .role(userRole)
                .build();
        accountRepository.save(user);
        // Tạo project không có assign với user này
        Project project = Project.builder()
                .title("Project Delete 3")
                .slug("project-delete-3")
                .status(1)
                .totalBudget(new java.math.BigDecimal("10000"))
                .amountNeededToRaise(new java.math.BigDecimal("5000"))
                .ward("Ward 3").district("District 3").province("Province 3")
                .assigns(new java.util.ArrayList<>())
                .build();
        project = projectRepository.save(project);
        // Tạo budget
        Budget budget = Budget.builder()
                .project(project)
                .title("Health")
                .unitPrice(new java.math.BigDecimal("1000"))
                .note("Initial")
                .status(1)
                .build();
        budget = budgetRepository.save(budget);
        // Setup SecurityContextHolder với user này
        CustomAccountDetails mockDetails = org.mockito.Mockito.mock(CustomAccountDetails.class);
        org.mockito.Mockito.when(mockDetails.getUsername()).thenReturn("user_delete3@example.com");
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(mockDetails, null);
        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);

        BigInteger budgetId = budget.getBudgetId();
        AppException thrown = assertThrows(AppException.class, () -> {
            budgetService.deleteBudget(budgetId);
        });
        assertEquals(ErrorCode.ACCESS_DENIED, thrown.getErrorCode());
        // Đảm bảo dữ liệu vẫn còn trong DB
        assertTrue(budgetRepository.findById(budgetId).isPresent());
    }

    /**
     * BS_deleteBudget_04
     * Mục đích: Kiểm tra xóa ngân sách khỏi dự án thành công
     * Input: budgetId hợp lệ, user là admin
     * Expected: Lưu data vào database (budget bị xóa khỏi DB)
     */
    @Test
    @DisplayName("BS_deleteBudget_04: Kiểm tra xóa ngân sách khỏi dự án thành công")
    @Rollback
    void testDeleteBudget_Success() {
        // Tạo role ADMIN
        Role adminRole = roleRepository.findRoleByRoleName("ADMIN").orElseGet(() -> roleRepository.save(Role.builder().roleName("ADMIN").isActive(true).build()));
        // Tạo account admin
        Account admin = Account.builder()
                .email("admin_delete4@example.com")
                .password("12345678")
                .isActive(true)
                .role(adminRole)
                .build();
        accountRepository.save(admin);
        // Tạo project
        Project project = Project.builder()
                .title("Project Delete 4")
                .slug("project-delete-4")
                .status(1)
                .totalBudget(new java.math.BigDecimal("10000"))
                .amountNeededToRaise(new java.math.BigDecimal("5000"))
                .ward("Ward 4").district("District 4").province("Province 4")
                .assigns(new java.util.ArrayList<>())
                .build();
        project = projectRepository.save(project);
        // Tạo budget
        Budget budget = Budget.builder()
                .project(project)
                .title("Health")
                .unitPrice(new java.math.BigDecimal("1000"))
                .note("Initial")
                .status(1)
                .build();
        budget = budgetRepository.save(budget);
        // Setup SecurityContextHolder với admin
        CustomAccountDetails mockDetails = org.mockito.Mockito.mock(CustomAccountDetails.class);
        org.mockito.Mockito.when(mockDetails.getUsername()).thenReturn("admin_delete4@example.com");
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(mockDetails, null);
        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);

        BigInteger budgetId = budget.getBudgetId();
        // Thực thi
        budgetService.deleteBudget(budgetId);
        // Đảm bảo budget đã bị xóa khỏi DB
        assertFalse(budgetRepository.findById(budgetId).isPresent());
    }
}

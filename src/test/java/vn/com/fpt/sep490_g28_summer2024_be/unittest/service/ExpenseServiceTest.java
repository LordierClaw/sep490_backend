package vn.com.fpt.sep490_g28_summer2024_be.unittest.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.authentication.UserResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.expense.ExpenseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.expense.ExpenseFileDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Assign;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Campaign;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Expense;
import vn.com.fpt.sep490_g28_summer2024_be.entity.ExpenseFile;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Project;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Role;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseService;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseServiceImpl;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AssignRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.CampaignRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ExpenseFileRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ExpenseRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.RoleRepository;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.service.expense.ExpenseService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@MockBean(FirebaseServiceImpl.class)
@Transactional
public class ExpenseServiceTest {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private FirebaseService firebaseService;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private CampaignRepository campaignRepository;
    @Autowired
    private AssignRepository assignRepository;
    @Autowired
    private ExpenseFileRepository expenseFileRepository;
    @Autowired
    private ExpenseService expenseService;

    private Campaign campaign;

    private Account adminAccount;

    private Account userAccount;

    @BeforeEach
    public void setUp() {
        // Dữ liệu cho account
        Role userRole = roleRepository.save(Role.builder().roleName("user").roleDescription("").build());

        userAccount = new Account();
        userAccount.setEmail("user@example.com");
        userAccount.setFullname("USER");
        userAccount.setPassword("password");
        userAccount.setRole(userRole);
        userAccount = accountRepository.save(userAccount);

        Role adminRole = roleRepository.save(Role.builder().roleName("admin").roleDescription("").build());
        adminAccount = new Account();
        adminAccount.setEmail("admin@example.com");
        adminAccount.setFullname("ADMIN");
        adminAccount.setPassword("password");
        adminAccount.setRole(adminRole);
        adminAccount = accountRepository.save(adminAccount);

        // Dữ liệu cho project và campaign
        campaign = campaignRepository.save(Campaign.builder()
                .title("Chiến dịch chung").description("desc").isActive(true)
                .createdAt(LocalDate.now()).build());
    }

    void setUpAsNonExistUser() {
        // Mock CustomAccountDetails
        CustomAccountDetails mockDetails = mock(CustomAccountDetails.class);
        when(mockDetails.getUsername()).thenReturn("nonexistent@example.com");

        // Đưa vào SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken(mockDetails, null);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    void setUpAsAdmin() {
        // Mock CustomAccountDetails và UserResponse
        CustomAccountDetails mockDetails = mock(CustomAccountDetails.class);
        UserResponse mockUserResponse = mock(UserResponse.class);

        when(mockDetails.getUsername()).thenReturn("admin@example.com");
        when(mockDetails.getUserResponse()).thenReturn(mockUserResponse);
        when(mockUserResponse.getScope()).thenReturn("admin");

        // Mock SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken(mockDetails, null);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    void setUpAsNotAdmin() {
        // Mock CustomAccountDetails và UserResponse
        CustomAccountDetails mockDetails = mock(CustomAccountDetails.class);
        UserResponse mockUserResponse = mock(UserResponse.class);

        when(mockDetails.getUsername()).thenReturn("user@example.com");
        when(mockDetails.getUserResponse()).thenReturn(mockUserResponse);
        when(mockUserResponse.getScope()).thenReturn("user");

        // Mock SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken(mockDetails, null);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("ES_viewByFilter_01")
    void viewByFilter_shouldThrowProjectNotExisted() {
        // 1. Chuẩn bị dữ liệu
        setUpAsAdmin();
        java.math.BigInteger notExistProjectId = java.math.BigInteger.valueOf(999999L);

        // 2. Thực thi
        AppException ex = assertThrows(AppException.class,
                () -> expenseService.viewByFilter(0, 5, null, notExistProjectId)
        );

        // 3. Kiểm tra dữ liệu trả về
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, ex.getErrorCode());

        // 4. Kiểm tra trong DB
        assertEquals(0, expenseRepository.count());
    }

    @Test
    @DisplayName("ES_viewByFilter_02")
    void viewByFilter_shouldReturnEmpty() {
        // 1. Chuẩn bị dữ liệu
        setUpAsAdmin();
        Project project = Project.builder()
                .title("Test Project")
                .ward("Ward 1").district("District 1").province("Province 1")
                .campaign(campaign)
                .status(1)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(java.math.BigDecimal.valueOf(1000))
                .totalBudget(java.math.BigDecimal.valueOf(2000))
                .build();
        project = projectRepository.save(project);

        // 2. Thực thi
        var response = expenseService.viewByFilter(0, 5, null, project.getProjectId());

        // 3. Kiểm tra dữ liệu trả về
        assertNotNull(response);
        assertEquals(0, response.getTotal());
        assertTrue(response.getContent().isEmpty());

        // 4. Kiểm tra trong DB
        assertEquals(0, expenseRepository.count());
    }

    @Test
    @DisplayName("ES_viewByFilter_03")
    void viewByFilter_shouldReturnList() {
        // 1. Chuẩn bị dữ liệu
        setUpAsAdmin();
        Project project = Project.builder()
                .title("Test Project 2")
                .ward("Ward 2").district("District 2").province("Province 2")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(java.math.BigDecimal.valueOf(1000))
                .totalBudget(java.math.BigDecimal.valueOf(2000))
                .build();
        project = projectRepository.save(project);
        for (int i = 0; i < 3; i++) {
            Expense expense = Expense.builder()
                    .title("Expense " + i)
                    .unitPrice(java.math.BigDecimal.valueOf(100 * (i + 1)))
                    .createdAt(java.time.LocalDateTime.now())
                    .updatedAt(java.time.LocalDateTime.now())
                    .project(project)
                    .build();
            expenseRepository.save(expense);
        }

        // 2. Thực thi
        var response = expenseService.viewByFilter(0, 5, null, project.getProjectId());

        // 3. Kiểm tra dữ liệu trả về
        assertNotNull(response);
        assertEquals(3, response.getTotal());
        assertEquals(3, response.getContent().size());

        // 4. Kiểm tra trong DB
        assertEquals(3, expenseRepository.count());
    }

    @Test
    @DisplayName("ES_addExpense_01")
    void addExpense_shouldThrowProjectNotExisted() {
        // 1. Chuẩn bị dữ liệu
        setUpAsAdmin();
        ExpenseDTO dto = new ExpenseDTO();
        ProjectResponseDTO prjDto = new ProjectResponseDTO();
        prjDto.setProjectId(java.math.BigInteger.valueOf(999999L));
        dto.setProject(prjDto);

        // 2. Thực thi
        AppException ex = assertThrows(
                AppException.class,
                () -> expenseService.addExpense(dto, null)
        );

        // 3. Kiểm tra dữ liệu trả về
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, ex.getErrorCode());

        // 4. Kiểm tra trong DB
        assertEquals(0, expenseRepository.count());
    }

    @Test
    @DisplayName("ES_addExpense_02")
    void addExpense_shouldThrowUnauthorized() {
        // 1. Chuẩn bị dữ liệu
        setUpAsNonExistUser();
        Project project = Project.builder()
                .title("Test Project 3")
                .ward("Ward 3").district("District 3").province("Province 3")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(java.math.BigDecimal.valueOf(1000))
                .totalBudget(java.math.BigDecimal.valueOf(2000))
                .build();
        project = projectRepository.save(project);
        ExpenseDTO dto = new ExpenseDTO();
        ProjectResponseDTO prjDto = new ProjectResponseDTO();
        prjDto.setProjectId(project.getProjectId());
        dto.setProject(prjDto);

        // 2. Thực thi
        AppException ex = assertThrows(AppException.class,
                () -> expenseService.addExpense(dto, null)
        );

        // 3. Kiểm tra dữ liệu trả về
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, ex.getErrorCode());

        // 4. Kiểm tra trong DB
        assertEquals(0, expenseRepository.count());
    }

    @Test
    @DisplayName("ES_addExpense_03")
    void addExpense_shouldThrowAccessDenied() {
        // 1. Chuẩn bị dữ liệu
        setUpAsNotAdmin();
        Project project = Project.builder()
                .title("Test Project 4")
                .ward("Ward 4").district("District 4").province("Province 4")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(java.math.BigDecimal.valueOf(1000))
                .totalBudget(java.math.BigDecimal.valueOf(2000))
                .build();
        Assign assign = assignRepository.save(Assign.builder().account(adminAccount).project(project).build());
        project.setAssigns(List.of(assign));
        project = projectRepository.save(project);

        ExpenseDTO dto = new ExpenseDTO();
        ProjectResponseDTO prjDto = new ProjectResponseDTO();
        prjDto.setProjectId(project.getProjectId());
        dto.setProject(prjDto);

        // 2. Thực thi
        AppException ex = assertThrows(AppException.class,
                () -> expenseService.addExpense(dto, null)
        );

        // 3. Kiểm tra dữ liệu trả về
        assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());

        // 4. Kiểm tra trong DB
        assertEquals(0, expenseRepository.count());
    }

    @Test
    @DisplayName("ES_addExpense_04")
    void addExpense_shouldAddExpenseSuccessfully() {
        // 1. Chuẩn bị dữ liệu
        setUpAsNotAdmin();
        Project project = Project.builder()
                .title("Test Project 5")
                .ward("Ward 5").district("District 5").province("Province 5")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(java.math.BigDecimal.valueOf(1000))
                .totalBudget(java.math.BigDecimal.valueOf(2000))
                .build();
        Assign assign = assignRepository.save(Assign.builder().account(userAccount).project(project).build());
        project.setAssigns(List.of(assign));
        project = projectRepository.save(project);

        ExpenseDTO dto = new ExpenseDTO();
        dto.setTitle("Expense Success");
        dto.setUnitPrice(java.math.BigDecimal.valueOf(500));
        ProjectResponseDTO prjDto = new ProjectResponseDTO();
        prjDto.setProjectId(project.getProjectId());
        dto.setProject(prjDto);

        // 2. Thực thi
        var result = expenseService.addExpense(dto, null);

        // 3. Kiểm tra dữ liệu trả về
        assertNotNull(result);
        assertEquals("Expense Success", result.getTitle());
        assertEquals(java.math.BigDecimal.valueOf(500), result.getUnitPrice());
        assertNotNull(result.getExpenseId());

        // 4. Kiểm tra trong DB
        var dbExpense = expenseRepository.findById(result.getExpenseId());
        assertTrue(dbExpense.isPresent());
        assertEquals("Expense Success", dbExpense.get().getTitle());
        assertEquals(java.math.BigDecimal.valueOf(500), dbExpense.get().getUnitPrice());
    }

    @Test
    @DisplayName("ES_addExpense_05")
    void addExpense_shouldThrowUploadFailedWhenFirebaseCannotUpload() throws IOException {
        // 1. Chuẩn bị dữ liệu
        setUpAsNotAdmin();
        Project project = Project.builder()
                .title("Test Project 6")
                .ward("Ward 6").district("District 6").province("Province 6")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(java.math.BigDecimal.valueOf(1000))
                .totalBudget(java.math.BigDecimal.valueOf(2000))
                .build();
        Assign assign = assignRepository.save(Assign.builder().account(userAccount).project(project).build());
        project.setAssigns(List.of(assign));
        project = projectRepository.save(project);

        ExpenseDTO dto = new ExpenseDTO();
        dto.setTitle("Expense Upload Failed");
        dto.setUnitPrice(java.math.BigDecimal.valueOf(500));
        ProjectResponseDTO prjDto = new ProjectResponseDTO();
        prjDto.setProjectId(project.getProjectId());
        dto.setProject(prjDto);

        // Chuẩn bị mock file upload
        MockMultipartFile file1 = new MockMultipartFile(
                "file", "test1.txt", "text/plain", "test content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "file", "test2.txt", "text/plain", "test content 2".getBytes());
        MultipartFile[] files = {file1, file2};

        // Mock Firebase service để throw IOException
        when(firebaseService.uploadMultipleFile(any(), any(), anyString())).thenThrow(new IOException("Upload failed"));

        // 2. Thực thi
        AppException ex = assertThrows(AppException.class,
                () -> expenseService.addExpense(dto, files)
        );

        // 3. Kiểm tra dữ liệu trả về
        assertEquals(ErrorCode.UPLOAD_FAILED, ex.getErrorCode());

        // 4. Kiểm tra trong DB
        assertEquals(0, expenseRepository.count());
    }

    @Test
    @DisplayName("ES_addExpense_06")
    void addExpense_shouldAddExpenseWithFilesSuccessfully() throws IOException {
        // 1. Chuẩn bị dữ liệu
        setUpAsNotAdmin();
        Project project = Project.builder()
                .title("Test Project 7")
                .ward("Ward 7").district("District 7").province("Province 7")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(java.math.BigDecimal.valueOf(1000))
                .totalBudget(java.math.BigDecimal.valueOf(2000))
                .build();
        Assign assign = assignRepository.save(Assign.builder().account(userAccount).project(project).build());
        project.setAssigns(List.of(assign));
        project = projectRepository.save(project);

        ExpenseDTO dto = new ExpenseDTO();
        dto.setTitle("Expense With Files");
        dto.setUnitPrice(java.math.BigDecimal.valueOf(600));
        ProjectResponseDTO prjDto = new ProjectResponseDTO();
        prjDto.setProjectId(project.getProjectId());
        dto.setProject(prjDto);

        // Chuẩn bị mock file upload
        MockMultipartFile file1 = new MockMultipartFile(
                "file", "test1.txt", "text/plain", "test content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "file", "test2.txt", "text/plain", "test content 2".getBytes());
        MultipartFile[] files = {file1, file2};

        // Mock Firebase service để trả về đường dẫn file
        List<String> fileUrls = List.of("https://firebase/file1.txt", "https://firebase/file2.txt");
        when(firebaseService.uploadMultipleFile(any(), any(), anyString())).thenReturn(fileUrls);

        // 2. Thực thi
        var result = expenseService.addExpense(dto, files);

        // 3. Kiểm tra dữ liệu trả về
        assertNotNull(result);
        assertEquals("Expense With Files", result.getTitle());
        assertEquals(BigDecimal.valueOf(600), result.getUnitPrice());
        assertNotNull(result.getExpenseId());

        // 4. Kiểm tra trong DB
        var dbExpense = expenseRepository.findById(result.getExpenseId());
        assertTrue(dbExpense.isPresent());
        assertEquals("Expense With Files", dbExpense.get().getTitle());
        assertEquals(BigDecimal.valueOf(600), dbExpense.get().getUnitPrice());

        // Kiểm tra files trong DB
        var expenseFiles = dbExpense.get().getExpenseFiles();
        assertNotNull(expenseFiles);
        assertEquals(2, expenseFiles.size());
        assertTrue(expenseFiles.stream()
                .anyMatch(ef -> ef.getFile().equals("https://firebase/file1.txt")));
        assertTrue(expenseFiles.stream()
                .anyMatch(ef -> ef.getFile().equals("https://firebase/file2.txt")));
    }

    @Test
    @DisplayName("ES_getExpenseById_01")
    void getExpenseById_shouldThrowExpenseNotFound() {
        // 1. Chuẩn bị dữ liệu
        setUpAsAdmin();
        BigInteger nonExistingExpenseId = BigInteger.valueOf(999999);

        // 2. Thực thi
        AppException ex = assertThrows(AppException.class,
                () -> expenseService.getExpenseById(nonExistingExpenseId));

        // 3. Kiểm tra dữ liệu trả về
        assertEquals(ErrorCode.EXPENSE_NOT_FOUND, ex.getErrorCode());

        // 4. Kiểm tra trong DB
        assertEquals(0, expenseRepository.count());
    }

    @Test
    @DisplayName("ES_getExpenseById_02")
    void getExpenseById_shouldReturnExpenseWithoutFiles() {
        // 1. Chuẩn bị dữ liệu
        setUpAsAdmin();
        Project project = Project.builder()
                .title("Test Project For Get")
                .ward("Ward 8").district("District 8").province("Province 8")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(BigDecimal.valueOf(1000))
                .totalBudget(BigDecimal.valueOf(2000))
                .build();
        project = projectRepository.save(project);

        Expense expense = Expense.builder()
                .title("Expense Without Files")
                .unitPrice(BigDecimal.valueOf(400))
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .project(project)
                .build();
        expense = expenseRepository.save(expense);

        // 2. Thực thi
        var result = expenseService.getExpenseById(expense.getExpenseId());

        // 3. Kiểm tra dữ liệu trả về
        assertNotNull(result);
        assertEquals("Expense Without Files", result.getTitle());
        assertEquals(BigDecimal.valueOf(400), result.getUnitPrice());
        assertEquals(expense.getExpenseId(), result.getExpenseId());
        assertTrue(result.getExpenseFiles() == null || result.getExpenseFiles().isEmpty());

        // 4. Kiểm tra trong DB
        var dbExpense = expenseRepository.findById(expense.getExpenseId());
        assertTrue(dbExpense.isPresent());
        assertEquals("Expense Without Files", dbExpense.get().getTitle());
        assertEquals(BigDecimal.valueOf(400), dbExpense.get().getUnitPrice());
        assertTrue(dbExpense.get().getExpenseFiles() == null || dbExpense.get().getExpenseFiles().isEmpty());
    }

    @Test
    @DisplayName("ES_getExpenseById_03")
    void getExpenseById_shouldReturnExpenseWithFiles() throws IOException {
        // 1. Chuẩn bị dữ liệu
        setUpAsAdmin();
        Project project = Project.builder()
                .title("Test Project For Get With Files")
                .ward("Ward 9").district("District 9").province("Province 9")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(BigDecimal.valueOf(1000))
                .totalBudget(BigDecimal.valueOf(2000))
                .build();
        project = projectRepository.save(project);

        Expense expense = Expense.builder()
                .title("Expense With Files")
                .unitPrice(BigDecimal.valueOf(450))
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .project(project)
                .build();


        // Chuẩn bị mock file URLs
        List<String> fileUrls = List.of("https://firebase/file-get1.txt", "https://firebase/file-get2.txt");

        // Tạo expense files và liên kết với expense
        ExpenseFile expenseFile1 = ExpenseFile.builder()
                .expense(expense)
                .file(fileUrls.get(0))
                .build();
        ExpenseFile expenseFile2 = ExpenseFile.builder()
                .expense(expense)
                .file(fileUrls.get(1))
                .build();
        
        expenseFileRepository.save(expenseFile1);
        expenseFileRepository.save(expenseFile2);

        expense.setExpenseFiles(new ArrayList<>(List.of(expenseFile1, expenseFile2)));
        expense = expenseRepository.save(expense);

        // 2. Thực thi
        var result = expenseService.getExpenseById(expense.getExpenseId());

        // 3. Kiểm tra dữ liệu trả về
        assertNotNull(result);
        assertEquals("Expense With Files", result.getTitle());
        assertEquals(BigDecimal.valueOf(450), result.getUnitPrice());
        assertEquals(expense.getExpenseId(), result.getExpenseId());
        assertNotNull(result.getExpenseFiles());
        assertEquals(2, result.getExpenseFiles().size());
        assertTrue(result.getExpenseFiles().stream()
                .anyMatch(ef -> ef.getFile().equals("https://firebase/file-get1.txt")));
        assertTrue(result.getExpenseFiles().stream()
                .anyMatch(ef -> ef.getFile().equals("https://firebase/file-get2.txt")));

        // 4. Kiểm tra trong DB
        var dbExpense = expenseRepository.findById(expense.getExpenseId());
        assertTrue(dbExpense.isPresent());
        assertEquals("Expense With Files", dbExpense.get().getTitle());
        assertEquals(BigDecimal.valueOf(450), dbExpense.get().getUnitPrice());
        
        var dbExpenseFiles = dbExpense.get().getExpenseFiles();
        assertNotNull(dbExpenseFiles);
        assertEquals(2, dbExpenseFiles.size());
        assertTrue(dbExpenseFiles.stream()
                .anyMatch(ef -> ef.getFile().equals("https://firebase/file-get1.txt")));
        assertTrue(dbExpenseFiles.stream()
                .anyMatch(ef -> ef.getFile().equals("https://firebase/file-get2.txt")));
    }

    @Test
    @DisplayName("ES_updateExpense_01")
    void updateExpense_shouldThrowExpenseNotFound() {
        // 1. Chuẩn bị dữ liệu
        setUpAsAdmin();
        BigInteger nonExistingExpenseId = BigInteger.valueOf(999999);
        ExpenseDTO dto = new ExpenseDTO();
        dto.setTitle("Updated Expense");
        dto.setUnitPrice(BigDecimal.valueOf(700));
        ProjectResponseDTO projectDTO = new ProjectResponseDTO();
        projectDTO.setProjectId(BigInteger.valueOf(1));
        dto.setProject(projectDTO);

        // 2. Thực thi
        AppException ex = assertThrows(AppException.class,
                () -> expenseService.updateExpense(dto, nonExistingExpenseId, null));

        // 3. Kiểm tra dữ liệu trả về
        assertEquals(ErrorCode.EXPENSE_NOT_FOUND, ex.getErrorCode());

        // 4. Kiểm tra trong DB
        assertEquals(0, expenseRepository.count());
    }

    @Test
    @DisplayName("ES_updateExpense_02")
    void updateExpense_shouldThrowUnauthorized() {
        // 1. Chuẩn bị dữ liệu
        setUpAsNonExistUser();
        Project project = Project.builder()
                .title("Test Project Update")
                .ward("Ward 10").district("District 10").province("Province 10")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(BigDecimal.valueOf(1000))
                .totalBudget(BigDecimal.valueOf(2000))
                .build();
        project = projectRepository.save(project);

        Expense expense = Expense.builder()
                .title("Expense To Update")
                .unitPrice(BigDecimal.valueOf(500))
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .project(project)
                .build();
        expense = expenseRepository.save(expense);

        ExpenseDTO dto = new ExpenseDTO();
        dto.setTitle("Updated Expense");
        dto.setUnitPrice(BigDecimal.valueOf(700));
        ProjectResponseDTO projectDTO = new ProjectResponseDTO();
        projectDTO.setProjectId(project.getProjectId());
        dto.setProject(projectDTO);

        // 2. Thực thi
        Expense finalExpense = expense;
        AppException ex = assertThrows(AppException.class,
                () -> expenseService.updateExpense(dto, finalExpense.getExpenseId(), null));

        // 3. Kiểm tra dữ liệu trả về
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, ex.getErrorCode());

        // 4. Kiểm tra trong DB
        var dbExpense = expenseRepository.findById(expense.getExpenseId());
        assertTrue(dbExpense.isPresent());
        assertEquals("Expense To Update", dbExpense.get().getTitle()); // Kiểm tra không thay đổi
    }

    @Test
    @DisplayName("ES_updateExpense_03")
    void updateExpense_shouldThrowAccessDenied() {
        // 1. Chuẩn bị dữ liệu
        setUpAsNotAdmin();
        Project project = Project.builder()
                .title("Test Project Update Access")
                .ward("Ward 11").district("District 11").province("Province 11")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(BigDecimal.valueOf(1000))
                .totalBudget(BigDecimal.valueOf(2000))
                .build();
        // Assign to admin account, not user account
        Assign assign = assignRepository.save(Assign.builder().account(adminAccount).project(project).build());
        project.setAssigns(List.of(assign));
        project = projectRepository.save(project);

        Expense expense = Expense.builder()
                .title("Expense Access Denied")
                .unitPrice(BigDecimal.valueOf(500))
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .project(project)
                .build();
        expense = expenseRepository.save(expense);

        ExpenseDTO dto = new ExpenseDTO();
        dto.setTitle("Try Access Denied");
        dto.setUnitPrice(BigDecimal.valueOf(700));
        ProjectResponseDTO projectDTO = new ProjectResponseDTO();
        projectDTO.setProjectId(project.getProjectId());
        dto.setProject(projectDTO);

        // 2. Thực thi
        Expense finalExpense = expense;
        AppException ex = assertThrows(AppException.class,
                () -> expenseService.updateExpense(dto, finalExpense.getExpenseId(), null));

        // 3. Kiểm tra dữ liệu trả về
        assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());

        // 4. Kiểm tra trong DB
        var dbExpense = expenseRepository.findById(expense.getExpenseId());
        assertTrue(dbExpense.isPresent());
        assertEquals("Expense Access Denied", dbExpense.get().getTitle()); // Kiểm tra không thay đổi
    }

    @Test
    @DisplayName("ES_updateExpense_04")
    void updateExpense_shouldUpdateWithExistingFiles() throws IOException {
        // 1. Chuẩn bị dữ liệu
        setUpAsAdmin();
        Project project = Project.builder()
                .title("Test Project Update With Files")
                .ward("Ward 12").district("District 12").province("Province 12")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(BigDecimal.valueOf(1000))
                .totalBudget(BigDecimal.valueOf(2000))
                .build();
        project = projectRepository.save(project);

        Expense expense = Expense.builder()
                .title("Expense With Existing Files")
                .unitPrice(BigDecimal.valueOf(500))
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .project(project)
                .build();

        // Tạo expense files và liên kết với expense
        List<String> fileUrls = List.of("https://firebase/file-update1.txt", "https://firebase/file-update2.txt");
        
        ExpenseFile expenseFile1 = ExpenseFile.builder()
                .expense(expense)
                .file(fileUrls.get(0))
                .build();
        ExpenseFile expenseFile2 = ExpenseFile.builder()
                .expense(expense)
                .file(fileUrls.get(1))
                .build();
        
        // Lưu expense files
        expenseFileRepository.save(expenseFile1);
        expenseFileRepository.save(expenseFile2);

        // Set expense files vào expense và lưu lại
        expense.setExpenseFiles(new ArrayList<>(List.of(expenseFile1, expenseFile2)));
        expense = expenseRepository.save(expense);

        // Chuẩn bị dto để update
        ExpenseDTO dto = new ExpenseDTO();
        dto.setTitle("Updated With Existing Files");
        dto.setUnitPrice(BigDecimal.valueOf(750));
        ProjectResponseDTO projectDTO = new ProjectResponseDTO();
        projectDTO.setProjectId(project.getProjectId());
        dto.setProject(projectDTO);
        
        // Dữ liệu files hiện tại (chỉ giữ lại file-update1.txt)
        ExpenseFileDTO fileDTO = new ExpenseFileDTO();
        fileDTO.setFile(fileUrls.get(0)); // Chỉ giữ lại 1 file
        dto.setExpenseFiles(fileUrls.stream().map(url -> ExpenseFileDTO.builder().file(url).build())
                .collect(Collectors.toList()));

        // 2. Thực thi
        var result = expenseService.updateExpense(dto, expense.getExpenseId(), null);

        // 3. Kiểm tra dữ liệu trả về
        assertNotNull(result);
        assertEquals("Updated With Existing Files", result.getTitle());
        assertEquals(BigDecimal.valueOf(750), result.getUnitPrice());
        assertNotNull(result.getExpenseFiles());
        assertEquals(1, result.getExpenseFiles().size());
        assertEquals(fileUrls.get(0), result.getExpenseFiles().get(0).getFile());

        // 4. Kiểm tra trong DB
        var dbExpense = expenseRepository.findById(expense.getExpenseId());
        assertTrue(dbExpense.isPresent());
        assertEquals("Updated With Existing Files", dbExpense.get().getTitle());
        assertEquals(BigDecimal.valueOf(750), dbExpense.get().getUnitPrice());
        
        // Kiểm tra chỉ còn 1 file trong DB
        var dbExpenseFiles = dbExpense.get().getExpenseFiles();
        assertNotNull(dbExpenseFiles);
        assertEquals(1, dbExpenseFiles.size());
        assertEquals(fileUrls.get(0), dbExpenseFiles.get(0).getFile());
    }

    @Test
    @DisplayName("ES_updateExpense_05")
    void updateExpense_shouldUpdateAndRemoveAllFiles() throws IOException {
        // 1. Chuẩn bị dữ liệu
        setUpAsAdmin();
        Project project = Project.builder()
                .title("Test Project Remove All Files")
                .ward("Ward 13").district("District 13").province("Province 13")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(BigDecimal.valueOf(1000))
                .totalBudget(BigDecimal.valueOf(2000))
                .build();
        project = projectRepository.save(project);

        Expense expense = Expense.builder()
                .title("Expense To Remove All Files")
                .unitPrice(BigDecimal.valueOf(500))
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .project(project)
                .build();

        // Tạo expense files và liên kết với expense
        List<String> fileUrls = List.of("https://firebase/file-remove1.txt", "https://firebase/file-remove2.txt");
        
        ExpenseFile expenseFile1 = ExpenseFile.builder()
                .expense(expense)
                .file(fileUrls.get(0))
                .build();
        ExpenseFile expenseFile2 = ExpenseFile.builder()
                .expense(expense)
                .file(fileUrls.get(1))
                .build();

        // Lưu expense files
        expenseFileRepository.save(expenseFile1);
        expenseFileRepository.save(expenseFile2);

        // Set expense files vào expense và lưu lại
        expense.setExpenseFiles(new ArrayList<>(List.of(expenseFile1, expenseFile2)));
        expense = expenseRepository.save(expense);

        // Chuẩn bị dto để update nhưng không bao gồm file
        ExpenseDTO dto = new ExpenseDTO();
        dto.setTitle("Updated Without Files");
        dto.setUnitPrice(BigDecimal.valueOf(800));
        ProjectResponseDTO projectDTO = new ProjectResponseDTO();
        projectDTO.setProjectId(project.getProjectId());
        dto.setProject(projectDTO);
        dto.setExpenseFiles(null); // Xóa tất cả files

        // 2. Thực thi
        var result = expenseService.updateExpense(dto, expense.getExpenseId(), null);

        // 3. Kiểm tra dữ liệu trả về
        assertNotNull(result);
        assertEquals("Updated Without Files", result.getTitle());
        assertEquals(BigDecimal.valueOf(800), result.getUnitPrice());
        assertTrue(result.getExpenseFiles() == null || result.getExpenseFiles().isEmpty());

        // 4. Kiểm tra trong DB
        var dbExpense = expenseRepository.findById(expense.getExpenseId());
        assertTrue(dbExpense.isPresent());
        assertEquals("Updated Without Files", dbExpense.get().getTitle());
        assertEquals(BigDecimal.valueOf(800), dbExpense.get().getUnitPrice());
        
        // Kiểm tra files đã bị xóa
        var dbExpenseFiles = dbExpense.get().getExpenseFiles();
        assertTrue(dbExpenseFiles == null || dbExpenseFiles.isEmpty());
    }

    @Test
    @DisplayName("ES_updateExpense_06")
    void updateExpense_shouldUpdateWhenEmptyFileList() throws IOException {
        // 1. Chuẩn bị dữ liệu
        setUpAsAdmin();
        Project project = Project.builder()
                .title("Test Project Empty Files")
                .ward("Ward 14").district("District 14").province("Province 14")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(BigDecimal.valueOf(1000))
                .totalBudget(BigDecimal.valueOf(2000))
                .build();
        project = projectRepository.save(project);

        Expense expense = Expense.builder()
                .title("Expense With Empty File List")
                .unitPrice(BigDecimal.valueOf(500))
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .project(project)
                .build();

        // Tạo expense files và liên kết với expense
        List<String> fileUrls = List.of("https://firebase/file-empty1.txt", "https://firebase/file-empty2.txt");
        
        ExpenseFile expenseFile1 = ExpenseFile.builder()
                .expense(expense)
                .file(fileUrls.get(0))
                .build();
        ExpenseFile expenseFile2 = ExpenseFile.builder()
                .expense(expense)
                .file(fileUrls.get(1))
                .build();


        
        // Lưu expense files
        expenseFileRepository.save(expenseFile1);
        expenseFileRepository.save(expenseFile2);

        // Set expense files vào expense và lưu lại
        expense.setExpenseFiles(new ArrayList<>(List.of(expenseFile1, expenseFile2)));
        expense = expenseRepository.save(expense);

        // Chuẩn bị dto để update với ExpenseFiles là danh sách rỗng
        ExpenseDTO dto = new ExpenseDTO();
        dto.setTitle("Updated With Empty File List");
        dto.setUnitPrice(BigDecimal.valueOf(850));
        ProjectResponseDTO projectDTO = new ProjectResponseDTO();
        projectDTO.setProjectId(project.getProjectId());
        dto.setProject(projectDTO);
        dto.setExpenseFiles(List.of()); // Empty list

        // 2. Thực thi
        var result = expenseService.updateExpense(dto, expense.getExpenseId(), null);

        // 3. Kiểm tra dữ liệu trả về
        assertNotNull(result);
        assertEquals("Updated With Empty File List", result.getTitle());
        assertEquals(BigDecimal.valueOf(850), result.getUnitPrice());
        assertTrue(result.getExpenseFiles() == null || result.getExpenseFiles().isEmpty());

        // 4. Kiểm tra trong DB
        var dbExpense = expenseRepository.findById(expense.getExpenseId());
        assertTrue(dbExpense.isPresent());
        assertEquals("Updated With Empty File List", dbExpense.get().getTitle());
        assertEquals(BigDecimal.valueOf(850), dbExpense.get().getUnitPrice());
        
        // Kiểm tra files đã bị xóa
        var dbExpenseFiles = dbExpense.get().getExpenseFiles();
        assertTrue(dbExpenseFiles == null || dbExpenseFiles.isEmpty());
    }

    @Test
    @DisplayName("ES_updateExpense_07")
    void updateExpense_shouldThrowUploadFailedWhenFirebaseCannotUpload() throws IOException {
        // 1. Chuẩn bị dữ liệu
        setUpAsAdmin();
        Project project = Project.builder()
                .title("Test Project Update Upload Fail")
                .ward("Ward 15").district("District 15").province("Province 15")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(BigDecimal.valueOf(1000))
                .totalBudget(BigDecimal.valueOf(2000))
                .build();
        project = projectRepository.save(project);

        Expense expense = Expense.builder()
                .title("Expense Upload Fail")
                .unitPrice(BigDecimal.valueOf(500))
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .project(project)
                .build();
        expense = expenseRepository.save(expense);

        // Chuẩn bị dto để update
        ExpenseDTO dto = new ExpenseDTO();
        dto.setTitle("Updated With Upload Fail");
        dto.setUnitPrice(BigDecimal.valueOf(900));
        ProjectResponseDTO projectDTO = new ProjectResponseDTO();
        projectDTO.setProjectId(project.getProjectId());
        dto.setProject(projectDTO);

        // Chuẩn bị mock file upload
        MockMultipartFile file1 = new MockMultipartFile(
                "file", "update-fail1.txt", "text/plain", "test content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "file", "update-fail2.txt", "text/plain", "test content 2".getBytes());
        MultipartFile[] files = {file1, file2};

        // Mock Firebase service để throw IOException
        when(firebaseService.uploadMultipleFile(any(), any(), anyString())).thenThrow(new IOException("Upload failed"));

        // 2. Thực thi
        Expense finalExpense = expense;
        AppException ex = assertThrows(AppException.class,
                () -> expenseService.updateExpense(dto, finalExpense.getExpenseId(), files));

        // 3. Kiểm tra dữ liệu trả về
        assertEquals(ErrorCode.UPLOAD_FAILED, ex.getErrorCode());

        // 4. Kiểm tra trong DB
        entityManager.clear();
        var dbExpense = expenseRepository.findById(expense.getExpenseId());
        assertTrue(dbExpense.isPresent());
        assertEquals("Expense Upload Fail", dbExpense.get().getTitle()); // Kiểm tra không thay đổi
    }

    @Test
    @DisplayName("ES_updateExpense_08")
    void updateExpense_shouldUpdateWithNewFiles() throws IOException {
        // 1. Chuẩn bị dữ liệu
        setUpAsAdmin();
        Project project = Project.builder()
                .title("Test Project With New Files")
                .ward("Ward 16").district("District 16").province("Province 16")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(BigDecimal.valueOf(1000))
                .totalBudget(BigDecimal.valueOf(2000))
                .build();
        project = projectRepository.save(project);

        Expense expense = Expense.builder()
                .title("Expense Before New Files")
                .unitPrice(BigDecimal.valueOf(500))
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .project(project)
                .build();
        expense = expenseRepository.save(expense);

        // Chuẩn bị dto để update
        ExpenseDTO dto = new ExpenseDTO();
        dto.setTitle("Updated With New Files");
        dto.setUnitPrice(BigDecimal.valueOf(950));
        ProjectResponseDTO projectDTO = new ProjectResponseDTO();
        projectDTO.setProjectId(project.getProjectId());
        dto.setProject(projectDTO);

        // Chuẩn bị mock file upload
        MockMultipartFile file1 = new MockMultipartFile(
                "file", "update-new1.txt", "text/plain", "test content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "file", "update-new2.txt", "text/plain", "test content 2".getBytes());
        MultipartFile[] files = {file1, file2};

        // Mock Firebase service để trả về đường dẫn file
        List<String> fileUrls = List.of("https://firebase/update-new1.txt", "https://firebase/update-new2.txt");
        when(firebaseService.uploadMultipleFile(any(), any(), anyString())).thenReturn(fileUrls);

        // 2. Thực thi
        var result = expenseService.updateExpense(dto, expense.getExpenseId(), files);

        // 3. Kiểm tra dữ liệu trả về
        assertNotNull(result);
        assertEquals("Updated With New Files", result.getTitle());
        assertEquals(BigDecimal.valueOf(950), result.getUnitPrice());

        // 4. Kiểm tra trong DB
        entityManager.clear();
        var dbExpense = expenseRepository.findById(expense.getExpenseId());
        assertTrue(dbExpense.isPresent());
        assertEquals("Updated With New Files", dbExpense.get().getTitle());
        assertTrue(BigDecimal.valueOf(950.00).compareTo(dbExpense.get().getUnitPrice()) == 0);

        // Kiểm tra files đã được thêm mới
        var dbExpenseFiles = dbExpense.get().getExpenseFiles();
        assertNotNull(dbExpenseFiles);
        assertEquals(2, dbExpenseFiles.size());
        assertTrue(dbExpenseFiles.stream()
                .anyMatch(ef -> ef.getFile().equals("https://firebase/update-new1.txt")));
        assertTrue(dbExpenseFiles.stream()
                .anyMatch(ef -> ef.getFile().equals("https://firebase/update-new2.txt")));
    }

    @Test
    @DisplayName("ES_deleteExpense_01")
    void deleteExpense_shouldThrowExpenseNotFound() {
        // 1. Chuẩn bị dữ liệu
        setUpAsAdmin();
        BigInteger nonExistingExpenseId = BigInteger.valueOf(999999);

        // 2. Thực thi
        AppException ex = assertThrows(AppException.class,
                () -> expenseService.deleteExpense(nonExistingExpenseId));

        // 3. Kiểm tra dữ liệu trả về
        assertEquals(ErrorCode.EXPENSE_NOT_FOUND, ex.getErrorCode());

        // 4. Kiểm tra trong DB
        assertEquals(0, expenseRepository.count());
    }

    @Test
    @DisplayName("ES_deleteExpense_02")
    void deleteExpense_shouldThrowUnauthorized() {
        // 1. Chuẩn bị dữ liệu
        setUpAsNonExistUser();
        Project project = Project.builder()
                .title("Test Project Delete")
                .ward("Ward 17").district("District 17").province("Province 17")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(BigDecimal.valueOf(1000))
                .totalBudget(BigDecimal.valueOf(2000))
                .build();
        project = projectRepository.save(project);

        Expense expense = Expense.builder()
                .title("Expense To Delete Unauthorized")
                .unitPrice(BigDecimal.valueOf(500))
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .project(project)
                .build();
        expense = expenseRepository.save(expense);

        // 2. Thực thi
        Expense finalExpense = expense;
        AppException ex = assertThrows(AppException.class,
                () -> expenseService.deleteExpense(finalExpense.getExpenseId()));

        // 3. Kiểm tra dữ liệu trả về
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, ex.getErrorCode());

        // 4. Kiểm tra trong DB
        entityManager.clear();
        var dbExpense = expenseRepository.findById(expense.getExpenseId());
        assertTrue(dbExpense.isPresent()); // Kiểm tra expense vẫn còn trong DB
    }

    @Test
    @DisplayName("ES_deleteExpense_03")
    void deleteExpense_shouldThrowAccessDenied() {
        // 1. Chuẩn bị dữ liệu
        setUpAsNotAdmin();
        Project project = Project.builder()
                .title("Test Project Delete Access")
                .ward("Ward 18").district("District 18").province("Province 18")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(BigDecimal.valueOf(1000))
                .totalBudget(BigDecimal.valueOf(2000))
                .build();
        // Assign to admin account, not user account
        Assign assign = assignRepository.save(Assign.builder().account(adminAccount).project(project).build());
        project.setAssigns(List.of(assign));
        project = projectRepository.save(project);

        Expense expense = Expense.builder()
                .title("Expense Delete Access Denied")
                .unitPrice(BigDecimal.valueOf(500))
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .project(project)
                .build();
        expense = expenseRepository.save(expense);

        // 2. Thực thi
        Expense finalExpense = expense;
        AppException ex = assertThrows(AppException.class,
                () -> expenseService.deleteExpense(finalExpense.getExpenseId()));

        // 3. Kiểm tra dữ liệu trả về
        assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());

        // 4. Kiểm tra trong DB
        entityManager.clear();
        var dbExpense = expenseRepository.findById(expense.getExpenseId());
        assertTrue(dbExpense.isPresent()); // Kiểm tra expense vẫn còn trong DB
    }

    @Test
    @DisplayName("ES_deleteExpense_04")
    void deleteExpense_shouldThrowDeleteFileFailedWhenFirebaseCannotDelete() throws IOException {
        // 1. Chuẩn bị dữ liệu
        setUpAsAdmin();
        Project project = Project.builder()
                .title("Test Project Delete File Failed")
                .ward("Ward 19").district("District 19").province("Province 19")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(BigDecimal.valueOf(1000))
                .totalBudget(BigDecimal.valueOf(2000))
                .build();
        project = projectRepository.save(project);

        Expense expense = Expense.builder()
                .title("Expense Delete File Failed")
                .unitPrice(BigDecimal.valueOf(500))
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .project(project)
                .build();
        expense = expenseRepository.save(expense);

        // Tạo expense files và liên kết với expense
        List<String> fileUrls = List.of("https://firebase/file-delete-fail1.txt", "https://firebase/file-delete-fail2.txt");
        
        ExpenseFile expenseFile1 = ExpenseFile.builder()
                .expense(expense)
                .file(fileUrls.get(0))
                .build();
        ExpenseFile expenseFile2 = ExpenseFile.builder()
                .expense(expense)
                .file(fileUrls.get(1))
                .build();
        
        // Lưu expense files
        expenseFileRepository.save(expenseFile1);
        expenseFileRepository.save(expenseFile2);

        // Set expense files vào expense và lưu lại
        expense.setExpenseFiles(new ArrayList<>(List.of(expenseFile1, expenseFile2)));
        expense = expenseRepository.save(expense);

        // Mock Firebase service để throw IOException
        when(firebaseService.deleteFileByPath(anyString())).thenThrow(new IOException("Delete failed"));

        // 2. Thực thi
        Expense finalExpense = expense;
        AppException ex = assertThrows(AppException.class,
                () -> expenseService.deleteExpense(finalExpense.getExpenseId()));

        // 3. Kiểm tra dữ liệu trả về
        assertEquals(ErrorCode.DELETE_FILE_FAILED, ex.getErrorCode());

        // 4. Kiểm tra trong DB
        entityManager.clear();
        var dbExpense = expenseRepository.findById(expense.getExpenseId());
        assertTrue(dbExpense.isPresent()); // Kiểm tra expense vẫn còn trong DB
        var dbExpenseFiles = dbExpense.get().getExpenseFiles();
        assertNotNull(dbExpenseFiles);
        assertFalse(dbExpenseFiles.isEmpty()); // Kiểm tra các file vẫn tồn tại
    }

    @Test
    @DisplayName("ES_deleteExpense_05")
    void deleteExpense_shouldDeleteExpenseSuccessfully() throws IOException {
        // 1. Chuẩn bị dữ liệu
        setUpAsAdmin();
        Project project = Project.builder()
                .title("Test Project Delete Success")
                .ward("Ward 20").district("District 20").province("Province 20")
                .status(1)
                .campaign(campaign)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .amountNeededToRaise(BigDecimal.valueOf(1000))
                .totalBudget(BigDecimal.valueOf(2000))
                .build();
        project = projectRepository.save(project);

        Expense expense = Expense.builder()
                .title("Expense Delete Success")
                .unitPrice(BigDecimal.valueOf(500))
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .project(project)
                .build();
        expense = expenseRepository.save(expense);

        // Tạo expense files và liên kết với expense
        List<String> fileUrls = List.of("https://firebase/file-delete-success1.txt", "https://firebase/file-delete-success2.txt");
        
        ExpenseFile expenseFile1 = ExpenseFile.builder()
                .expense(expense)
                .file(fileUrls.get(0))
                .build();
        ExpenseFile expenseFile2 = ExpenseFile.builder()
                .expense(expense)
                .file(fileUrls.get(1))
                .build();
        
        // Lưu expense files
        expenseFileRepository.save(expenseFile1);
        expenseFileRepository.save(expenseFile2);

        // Set expense files vào expense và lưu lại
        expense.setExpenseFiles(new ArrayList<>(List.of(expenseFile1, expenseFile2)));
        expense = expenseRepository.save(expense);
        BigInteger expenseId = expense.getExpenseId();

        // 2. Thực thi
        expenseService.deleteExpense(expenseId);

        // 3. Kiểm tra trong DB
        var dbExpense = expenseRepository.findById(expenseId);
        assertFalse(dbExpense.isPresent()); // Kiểm tra expense đã bị xóa
        
        // Kiểm tra các file đã bị xóa
        var expenseFiles = expenseFileRepository.findByExpenseId(expenseId);
        assertTrue(expenseFiles.isEmpty());
    }
}

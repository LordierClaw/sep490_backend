package vn.com.fpt.sep490_g28_summer2024_be;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.authentication.UserResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.sponsor.SponsorRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.sponsor.SponsorResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.sponsor.SponsorUpdateRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.*;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseServiceImpl;
import vn.com.fpt.sep490_g28_summer2024_be.repository.*;
import vn.com.fpt.sep490_g28_summer2024_be.service.sponsor.SponsorService;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@MockBean(FirebaseServiceImpl.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DefaultSponsorServiceTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SponsorRepository sponsorRepository;

    @Autowired
    private AssignRepository assignRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private FirebaseServiceImpl firebaseService;

    @Autowired
    private SponsorService sponsorService;

    @MockBean
    private Authentication authentication;

    @MockBean
    private SecurityContext securityContext;

    private SponsorRequestDTO sponsorRequestDTO;
    private Project project;
    private Account account;
    private CustomAccountDetails customAccountDetails;

    @BeforeEach
    void setUp() {
        // Setup basic sponsor request
        sponsorRequestDTO = new SponsorRequestDTO();
        sponsorRequestDTO.setCompanyName("Test Company");
        sponsorRequestDTO.setBusinessField("Test Field");
        sponsorRequestDTO.setRepresentative("Test Rep");
        sponsorRequestDTO.setRepresentativeEmail("test@email.com");
        sponsorRequestDTO.setPhoneNumber("1234567890");
        sponsorRequestDTO.setValue("1000");
        sponsorRequestDTO.setNote("Test Note");

        // Setup mock authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("DSS2_addSponsorToProject_01: Test when user doesn't exist")
    void testAddSponsorToProject_UserNotFound() {
        // Setup
        UserResponse userResponse = UserResponse.builder()
                .email("nonexistent@email.com")
                .password("password")
                .isActive(true)
                .build();
        customAccountDetails = new CustomAccountDetails(userResponse);
        when(authentication.getPrincipal()).thenReturn(customAccountDetails);

        // Test
        AppException exception = assertThrows(AppException.class, () -> {
            sponsorService.addSponsorToProject(sponsorRequestDTO, BigInteger.ONE, null, null);
        });

        // Verify
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    @DisplayName("DSS2_addSponsorToProject_02: Test when project doesn't exist")
    void testAddSponsorToProject_ProjectNotFound() {
        // Setup
        account = Account.builder()
                .email("test@email.com")
                .password("password")
                .build();
        accountRepository.save(account);

        UserResponse userResponse = UserResponse.builder()
                .email(account.getEmail())
                .password(account.getPassword())
                .isActive(true)
                .build();
        customAccountDetails = new CustomAccountDetails(userResponse);
        when(authentication.getPrincipal()).thenReturn(customAccountDetails);

        // Test
        AppException exception = assertThrows(AppException.class, () -> {
            sponsorService.addSponsorToProject(sponsorRequestDTO, BigInteger.ONE, null, null);
        });

        // Verify
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    @DisplayName("DSS2_addSponsorToProject_03: Test when user doesn't have permission")
    void testAddSponsorToProject_UserNotAuthorized() {
        // Setup
        // Create and save role first
        Role userRole = Role.builder()
                .roleName("user")
                .roleDescription("Normal user role")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRole = roleRepository.save(userRole);

        // Create test account with saved role
        account = Account.builder()
                .email("test@email.com")
                .password("password")
                .role(userRole)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        account = accountRepository.save(account);

        // Create another account that will be assigned to the project
        Account assignedAccount = Account.builder()
                .email("assigned@email.com")
                .password("password")
                .role(userRole)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assignedAccount = accountRepository.save(assignedAccount);

        // Create and save campaign first
        Campaign campaign = Campaign.builder()
                .title("Test Campaign")
                .description("Test Description")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaign = campaignRepository.save(campaign);

        // Create project with saved campaign
        project = Project.builder()
                .title("Test Project")
                .status(1)
                .ward("Test Ward")
                .district("Test District")
                .province("Test Province")
                .campaign(campaign)
                .assigns(new ArrayList<>())
                .totalBudget(new BigDecimal("10000"))
                .amountNeededToRaise(new BigDecimal("10000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        project = projectRepository.save(project);

        // Create and save assign with all required fields
        Assign assign = Assign.builder()
                .project(project)
                .account(assignedAccount)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assign = assignRepository.save(assign);

        // Update project's assigns list
        List<Assign> assigns = new ArrayList<>();
        assigns.add(assign);
        project.setAssigns(assigns);
        project = projectRepository.save(project);

        // Setup user response for authentication
        UserResponse userResponse = UserResponse.builder()
                .email(account.getEmail())
                .password(account.getPassword())
                .isActive(true)
                .scope("user")
                .build();
        customAccountDetails = new CustomAccountDetails(userResponse);
        when(authentication.getPrincipal()).thenReturn(customAccountDetails);

        // Test
        AppException exception = assertThrows(AppException.class, () -> {
            sponsorService.addSponsorToProject(sponsorRequestDTO, project.getProjectId(), null, null);
        });

        // Verify
        assertEquals(ErrorCode.HTTP_FORBIDDEN, exception.getErrorCode());
    }

    private void setupSuccessfulTestEnvironment() throws IOException {
        // Create and save role first
        Role adminRole = Role.builder()
                .roleName("admin")
                .roleDescription("Admin role")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        adminRole = roleRepository.save(adminRole);

        // Create test account with admin role
        account = Account.builder()
                .email("admin@email.com")
                .password("password")
                .role(adminRole)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        account = accountRepository.save(account);

        // Create and save campaign
        Campaign campaign = Campaign.builder()
                .title("Test Campaign")
                .description("Test Description")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaign = campaignRepository.save(campaign);

        // Create project with empty sponsors list
        project = Project.builder()
                .title("Test Project")
                .status(1)
                .ward("Test Ward")
                .district("Test District")
                .province("Test Province")
                .campaign(campaign)
                .sponsors(new ArrayList<>())  // Initialize empty sponsors list
                .totalBudget(new BigDecimal("10000"))
                .amountNeededToRaise(new BigDecimal("10000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        project = projectRepository.save(project);

        // Setup authentication
        UserResponse userResponse = UserResponse.builder()
                .email(account.getEmail())
                .password(account.getPassword())
                .isActive(true)
                .scope("admin")
                .build();
        customAccountDetails = new CustomAccountDetails(userResponse);
        when(authentication.getPrincipal()).thenReturn(customAccountDetails);
    }

    @Test
    @DisplayName("DSS2_addSponsorToProject_04: Test when contract is not null but upload fails")
    void testAddSponsorToProject_ContractUploadFailed() throws IOException {
        // Setup
        setupSuccessfulTestEnvironment();
        
        // Create initial sponsor to test totalSponsor calculation
        Sponsor initialSponsor = Sponsor.builder()
                .companyName("Initial Company")
                .businessField("Initial Field")
                .project(project)
                .representative("Initial Rep")
                .representativeEmail("initial@email.com")
                .phoneNumber("0987654321")
                .value(new BigDecimal("5000"))  // Set value to test amountNeededToRaise calculation
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        sponsorRepository.save(initialSponsor);
        
        // Add sponsor to project's sponsors list
        List<Sponsor> sponsors = new ArrayList<>();
        sponsors.add(initialSponsor);
        project.setSponsors(sponsors);
        project = projectRepository.save(project);
        
        // Create contract file
        MockMultipartFile contract = new MockMultipartFile(
                "contract",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );
        
        // Mock Firebase to throw exception
        when(firebaseService.uploadOneFile(any(), any(), any())).thenThrow(new IOException());

        // Test
        AppException exception = assertThrows(AppException.class, () -> {
            sponsorService.addSponsorToProject(sponsorRequestDTO, project.getProjectId(), contract, null);
        });

        // Verify
        assertEquals(ErrorCode.UPLOAD_FAILED, exception.getErrorCode());
        
        // Verify project's amountNeededToRaise was not changed due to failed sponsor addition
        Project updatedProject = projectRepository.findById(project.getProjectId()).orElseThrow();
        BigDecimal expectedAmountNeeded = new BigDecimal("5000"); // 10000 - 5000
        assertEquals(0, expectedAmountNeeded.compareTo(updatedProject.getAmountNeededToRaise()));
    }

    @Test
    @DisplayName("DSS2_addSponsorToProject_05: Test when logo is not null but upload fails")
    void testAddSponsorToProject_LogoUploadFailed() throws IOException {
        // Setup
        setupSuccessfulTestEnvironment();
        
        // Create initial sponsor to test totalSponsor calculation
        Sponsor initialSponsor = Sponsor.builder()
                .companyName("Initial Company")
                .businessField("Initial Field")
                .project(project)
                .representative("Initial Rep")
                .representativeEmail("initial@email.com")
                .phoneNumber("0987654321")
                .value(new BigDecimal("5000"))  // Set value to test amountNeededToRaise calculation
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        sponsorRepository.save(initialSponsor);
        
        // Add sponsor to project's sponsors list
        List<Sponsor> sponsors = new ArrayList<>();
        sponsors.add(initialSponsor);
        project.setSponsors(sponsors);
        project = projectRepository.save(project);
        
        // Create logo file
        MockMultipartFile logo = new MockMultipartFile(
                "logo",
                "logo.png",
                "image/png",
                "test content".getBytes()
        );
        
        // Mock Firebase to throw exception
        when(firebaseService.uploadOneFile(any(), any(), any())).thenThrow(new IOException());

        // Test
        AppException exception = assertThrows(AppException.class, () -> {
            sponsorService.addSponsorToProject(sponsorRequestDTO, project.getProjectId(), null, logo);
        });

        // Verify
        assertEquals(ErrorCode.UPLOAD_FAILED, exception.getErrorCode());
        
        // Verify project's amountNeededToRaise was not changed due to failed sponsor addition
        Project updatedProject = projectRepository.findById(project.getProjectId()).orElseThrow();
        BigDecimal expectedAmountNeeded = new BigDecimal("5000"); // 10000 - 5000
        assertEquals(0, expectedAmountNeeded.compareTo(updatedProject.getAmountNeededToRaise()));
    }

    @Test
    @DisplayName("DSS2_addSponsorToProject_06: Test successful contract and logo upload with sponsor creation")
    void testAddSponsorToProject_SuccessfulContractAndLogoUpload() throws IOException {
        // Setup
        setupSuccessfulTestEnvironment();
        
        // Create contract file
        MockMultipartFile contract = new MockMultipartFile(
                "contract",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );
        
        // Create logo file
        MockMultipartFile logo = new MockMultipartFile(
                "logo",
                "logo.png",
                "image/png",
                "test content".getBytes()
        );
        
        // Mock Firebase to return different URLs for contract and logo
        when(firebaseService.uploadOneFile(any(), any(), eq("project/sponsors")))
                .thenReturn("test-contract-url");
        when(firebaseService.uploadOneFile(any(), any(), eq("project/sponsors/logo")))
                .thenReturn("test-logo-url");

        // Test
        SponsorResponseDTO response = sponsorService.addSponsorToProject(
                sponsorRequestDTO,
                project.getProjectId(),
                contract,
                logo
        );

        // Verify response
        assertNotNull(response);
        assertNotNull(response.getSponsorId());
        
        // Verify saved sponsor
        Sponsor savedSponsor = sponsorRepository.findById(response.getSponsorId()).orElse(null);
        assertNotNull(savedSponsor);
        assertEquals("test-contract-url", savedSponsor.getContract());
        assertEquals("test-logo-url", savedSponsor.getLogo());
        
        // Verify sponsor data
        assertEquals(sponsorRequestDTO.getCompanyName(), savedSponsor.getCompanyName());
        assertEquals(sponsorRequestDTO.getBusinessField(), savedSponsor.getBusinessField());
        assertEquals(sponsorRequestDTO.getRepresentative(), savedSponsor.getRepresentative());
        assertEquals(sponsorRequestDTO.getRepresentativeEmail(), savedSponsor.getRepresentativeEmail());
        assertEquals(sponsorRequestDTO.getPhoneNumber(), savedSponsor.getPhoneNumber());
        assertEquals(new BigDecimal(sponsorRequestDTO.getValue()), savedSponsor.getValue());
        assertEquals(sponsorRequestDTO.getNote(), savedSponsor.getNote());
        
//        // Verify project's amountNeededToRaise was updated
//        Project updatedProject = projectRepository.findById(project.getProjectId()).orElseThrow();
//        BigDecimal expectedAmountNeeded = new BigDecimal("9000"); // 10000 - 1000 (sponsor value)
//        assertEquals(0, expectedAmountNeeded.compareTo(updatedProject.getAmountNeededToRaise()));
    }

    @Test
    @DisplayName("DSS2_viewDetail_01: Test when sponsor is not found")
    void testViewDetail_SponsorNotFound() {
        // Test
        AppException exception = assertThrows(AppException.class, () -> {
            sponsorService.viewDetail(BigInteger.ONE);
        });

        // Verify
        assertEquals(ErrorCode.SPONSOR_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    @DisplayName("DSS2_viewDetail_02: Test when successfully retrieving sponsor details")
    void testViewDetail_Success() {
        // Setup
        Sponsor sponsor = Sponsor.builder()
                .companyName("Test Company")
                .businessField("Test Field")
                .representative("Test Rep")
                .representativeEmail("test@email.com")
                .phoneNumber("1234567890")
                .value(new BigDecimal("1000"))
                .note("Test Note")
                .logo("test-logo-url")
                .contract("test-contract-url")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        sponsor = sponsorRepository.save(sponsor);

        // Test
        SponsorResponseDTO response = sponsorService.viewDetail(sponsor.getSponsorId());

        // Verify
        assertNotNull(response);
        assertEquals(sponsor.getSponsorId(), response.getSponsorId());
        assertEquals(sponsor.getCompanyName(), response.getCompanyName());
        assertEquals(sponsor.getBusinessField(), response.getBusinessField());
        assertEquals(sponsor.getRepresentative(), response.getRepresentative());
        assertEquals(sponsor.getRepresentativeEmail(), response.getRepresentativeEmail());
        assertEquals(sponsor.getPhoneNumber(), response.getPhoneNumber());
        assertEquals(sponsor.getValue().toString(), response.getValue());
        assertEquals(sponsor.getNote(), response.getNote());
        assertEquals(sponsor.getLogo(), response.getLogo());
        assertEquals(sponsor.getContract(), response.getContract());
    }

    @Test
    @DisplayName("DSS2_viewListSponsorInProject_01: Test when project is not found")
    void testViewListSponsorInProject_ProjectNotFound() {
        // Setup test parameters
        Integer page = 0;
        Integer size = 5;
        String companyName = "Green Future Corp";
        BigInteger projectId = new BigInteger("111");

        // Test
        AppException exception = assertThrows(AppException.class, () -> {
            sponsorService.viewListSponsorInProject(page, size, companyName, projectId);
        });

        // Verify
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    @DisplayName("DSS2_viewListSponsorInProject_02: Test successful retrieval of sponsor list")
    void testViewListSponsorInProject_Success() {
        // Setup
        // Create and save campaign first
        Campaign campaign = Campaign.builder()
                .title("Test Campaign")
                .description("Test Description")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaign = campaignRepository.save(campaign);

        // Create and save project
        Project project = Project.builder()
                .title("Test Project")
                .status(1)
                .ward("Test Ward")
                .district("Test District")
                .province("Test Province")
                .campaign(campaign)
                .totalBudget(new BigDecimal("10000"))
                .amountNeededToRaise(new BigDecimal("10000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        project = projectRepository.save(project);

        // Create and save sponsors
        Sponsor sponsor1 = Sponsor.builder()
                .companyName("Green Future Corp")
                .businessField("Test Field 1")
                .project(project)
                .representative("Test Rep 1")
                .representativeEmail("test1@email.com")
                .phoneNumber("1234567890")
                .value(new BigDecimal("1000"))
                .logo("test-logo-url-1")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        sponsorRepository.save(sponsor1);

        Sponsor sponsor2 = Sponsor.builder()
                .companyName("Green Future Corp Branch")
                .businessField("Test Field 2")
                .project(project)
                .representative("Test Rep 2")
                .representativeEmail("test2@email.com")
                .phoneNumber("0987654321")
                .value(new BigDecimal("2000"))
                .logo("test-logo-url-2")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        sponsorRepository.save(sponsor2);

        // Test parameters
        Integer page = 0;
        Integer size = 5;
        String companyName = "Green Future Corp";

        // Test
        PageResponse<?> response = sponsorService.viewListSponsorInProject(page, size, companyName, project.getProjectId());

        // Verify
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(2, response.getTotal());
        assertEquals(size, response.getLimit());
        assertEquals(page, response.getOffset());

        // Verify content
        @SuppressWarnings("unchecked")
        List<SponsorResponseDTO> sponsors = (List<SponsorResponseDTO>) response.getContent();
        assertEquals(2, sponsors.size());
        
        // Verify sponsors exist in the response without assuming order
        boolean foundSponsor1 = false;
        boolean foundSponsor2 = false;
        
        for (SponsorResponseDTO sponsorResponse : sponsors) {
            if (sponsorResponse.getCompanyName().equals(sponsor1.getCompanyName())) {
                foundSponsor1 = true;
                assertEquals(sponsor1.getRepresentative(), sponsorResponse.getRepresentative());
                assertEquals(sponsor1.getRepresentativeEmail(), sponsorResponse.getRepresentativeEmail());
                assertEquals(sponsor1.getLogo(), sponsorResponse.getLogo());
                assertEquals(sponsor1.getValue().toString(), sponsorResponse.getValue());
            } else if (sponsorResponse.getCompanyName().equals(sponsor2.getCompanyName())) {
                foundSponsor2 = true;
                assertEquals(sponsor2.getRepresentative(), sponsorResponse.getRepresentative());
                assertEquals(sponsor2.getRepresentativeEmail(), sponsorResponse.getRepresentativeEmail());
                assertEquals(sponsor2.getLogo(), sponsorResponse.getLogo());
                assertEquals(sponsor2.getValue().toString(), sponsorResponse.getValue());
            }
        }
        
        assertTrue(foundSponsor1, "First sponsor should be found in the response");
        assertTrue(foundSponsor2, "Second sponsor should be found in the response");
    }

    @Test
    @DisplayName("DSS2_update_01: Test when user is not found")
    void testUpdate_UserNotFound() {
        // Setup
        BigInteger sponsorId = BigInteger.ONE;
        SponsorUpdateRequestDTO requestDTO = new SponsorUpdateRequestDTO();
        requestDTO.setCompanyName("Eco Future Co.");
        
        // Setup authentication with non-existent user
        UserResponse userResponse = UserResponse.builder()
                .email("nonexistent@email.com")
                .password("password")
                .isActive(true)
                .build();
        customAccountDetails = new CustomAccountDetails(userResponse);
        when(authentication.getPrincipal()).thenReturn(customAccountDetails);

        // Create empty files
        MockMultipartFile file = new MockMultipartFile("file", new byte[0]);
        MockMultipartFile logo = new MockMultipartFile("logo", new byte[0]);

        // Test
        AppException exception = assertThrows(AppException.class, () -> {
            sponsorService.update(sponsorId, requestDTO, file, logo);
        });

        // Verify
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    @DisplayName("DSS2_update_02: Test when sponsor is not found")
    void testUpdate_SponsorNotFound() {
        // Setup
        // Create and save account
        account = Account.builder()
                .email("test@email.com")
                .password("password")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        account = accountRepository.save(account);

        // Setup authentication
        UserResponse userResponse = UserResponse.builder()
                .email(account.getEmail())
                .password(account.getPassword())
                .isActive(true)
                .build();
        customAccountDetails = new CustomAccountDetails(userResponse);
        when(authentication.getPrincipal()).thenReturn(customAccountDetails);

        // Setup request
        BigInteger sponsorId = BigInteger.ONE;
        SponsorUpdateRequestDTO requestDTO = new SponsorUpdateRequestDTO();
        requestDTO.setCompanyName("Eco Future Co.");

        // Create empty files
        MockMultipartFile file = new MockMultipartFile("file", new byte[0]);
        MockMultipartFile logo = new MockMultipartFile("logo", new byte[0]);

        // Test
        AppException exception = assertThrows(AppException.class, () -> {
            sponsorService.update(sponsorId, requestDTO, file, logo);
        });

        // Verify
        assertEquals(ErrorCode.SPONSOR_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    @DisplayName("DSS2_update_03: Test when user doesn't have permission")
    void testUpdate_UserNotAuthorized() {
        // Setup
        // Create and save role first
        Role userRole = Role.builder()
                .roleName("user")
                .roleDescription("Normal user role")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRole = roleRepository.save(userRole);

        // Create test account with user role
        account = Account.builder()
                .email("test@email.com")
                .password("password")
                .role(userRole)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        account = accountRepository.save(account);

        // Create another account that will be assigned to the project
        Account assignedAccount = Account.builder()
                .email("assigned@email.com")
                .password("password")
                .role(userRole)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assignedAccount = accountRepository.save(assignedAccount);

        // Create and save campaign
        Campaign campaign = Campaign.builder()
                .title("Test Campaign")
                .description("Test Description")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaign = campaignRepository.save(campaign);

        // Create and save project
        Project project = Project.builder()
                .title("Test Project")
                .status(1)
                .ward("Test Ward")
                .district("Test District")
                .province("Test Province")
                .campaign(campaign)
                .assigns(new ArrayList<>())
                .totalBudget(new BigDecimal("10000"))
                .amountNeededToRaise(new BigDecimal("10000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        project = projectRepository.save(project);

        // Create and save assign for different user
        Assign assign = Assign.builder()
                .project(project)
                .account(assignedAccount)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assign = assignRepository.save(assign);

        // Update project's assigns list
        List<Assign> assigns = new ArrayList<>();
        assigns.add(assign);
        project.setAssigns(assigns);
        project = projectRepository.save(project);

        // Create and save sponsor
        Sponsor sponsor = Sponsor.builder()
                .companyName("Test Company")
                .businessField("Test Field")
                .project(project)
                .representative("Test Rep")
                .representativeEmail("test@email.com")
                .phoneNumber("1234567890")
                .value(new BigDecimal("1000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        sponsor = sponsorRepository.save(sponsor);

        // Setup authentication for non-authorized user
        UserResponse userResponse = UserResponse.builder()
                .email(account.getEmail())
                .password(account.getPassword())
                .isActive(true)
                .scope("user")
                .build();
        customAccountDetails = new CustomAccountDetails(userResponse);
        when(authentication.getPrincipal()).thenReturn(customAccountDetails);

        // Setup request
        SponsorUpdateRequestDTO requestDTO = new SponsorUpdateRequestDTO();
        requestDTO.setCompanyName("Eco Future Co.");

        // Create empty files
        MockMultipartFile file = new MockMultipartFile("file", new byte[0]);
        MockMultipartFile logo = new MockMultipartFile("logo", new byte[0]);

        // Test
        Sponsor finalSponsor = sponsor;
        AppException exception = assertThrows(AppException.class, () -> {
            sponsorService.update(finalSponsor.getSponsorId(), requestDTO, file, logo);
        });

        // Verify
        assertEquals(ErrorCode.HTTP_FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("DSS2_update_04: Test when contract is empty")
    void testUpdate_ContractNotNull() {
        // Setup
        // Create and save role first
        Role adminRole = Role.builder()
                .roleName("admin")
                .roleDescription("Admin role")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        adminRole = roleRepository.save(adminRole);

        // Create test account with admin role
        account = Account.builder()
                .email("admin@email.com")
                .password("password")
                .role(adminRole)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        account = accountRepository.save(account);

        // Create and save campaign
        Campaign campaign = Campaign.builder()
                .title("Test Campaign")
                .description("Test Description")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaign = campaignRepository.save(campaign);

        // Create project
        Project project = Project.builder()
                .title("Test Project")
                .status(1)
                .ward("Test Ward")
                .district("Test District")
                .province("Test Province")
                .campaign(campaign)
                .totalBudget(new BigDecimal("10000"))
                .amountNeededToRaise(new BigDecimal("10000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .sponsors(new ArrayList<>()) // Đảm bảo không null
                .build();
        project = projectRepository.save(project);

        // Create and save sponsor
        Sponsor sponsor = Sponsor.builder()
                .companyName("Test Company")
                .businessField("Test Field")
                .project(project)
                .representative("Test Rep")
                .representativeEmail("test@email.com")
                .phoneNumber("1234567890")
                .value(new BigDecimal("1000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        sponsor = sponsorRepository.save(sponsor);
        project.getSponsors().add(sponsor); // Thêm sponsor vào danh sách
        project = projectRepository.save(project);

        // Setup authentication
        UserResponse userResponse = UserResponse.builder()
                .email(account.getEmail())
                .password(account.getPassword())
                .isActive(true)
                .scope("admin")
                .build();
        customAccountDetails = new CustomAccountDetails(userResponse);
        when(authentication.getPrincipal()).thenReturn(customAccountDetails);

        // Setup request with empty contract
        SponsorUpdateRequestDTO requestDTO = new SponsorUpdateRequestDTO();
        requestDTO.setCompanyName("Eco Future Co.");
        requestDTO.setBusinessField("Environmental Solutions");
        requestDTO.setRepresentative("John Doe");
        requestDTO.setRepresentativeEmail("john@ecofuture.com");
        requestDTO.setPhoneNumber("0987654321");
        requestDTO.setNote("Updated sponsor information");
        requestDTO.setValue("2000");
        requestDTO.setContract("");  // Set contract to a non-empty value

        // Setup mock files
        MockMultipartFile contractFile = new MockMultipartFile("", "some-contract-path.pdf", "application/pdf", "fake pdf content".getBytes());
        MockMultipartFile logo = new MockMultipartFile("logo", new byte[0]);

        // Test
        Sponsor finalSponsor = sponsor;
        AppException exception = assertThrows(AppException.class, () -> {
            sponsorService.update(finalSponsor.getSponsorId(), requestDTO, contractFile, logo);
        });

        // Verify
        assertEquals(ErrorCode.CONTRACT_NOT_NULL, exception.getErrorCode());
    }

    @Test
    @DisplayName("DSS2_update_05: Test when project doesn't exist")
    void testUpdate_ProjectNotExisted() {
        // Setup
        // Create and save role first
        Role adminRole = Role.builder()
                .roleName("admin")
                .roleDescription("Admin role")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        adminRole = roleRepository.save(adminRole);

        // Create test account with admin role
        account = Account.builder()
                .email("admin@email.com")
                .password("password")
                .role(adminRole)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        account = accountRepository.save(account);

        // Create and save campaign
        Campaign campaign = Campaign.builder()
                .title("Test Campaign")
                .description("Test Description")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaign = campaignRepository.save(campaign);

        // Create project
        Project project = Project.builder()
                .title("Test Project")
                .status(1)
                .ward("Test Ward")
                .district("Test District")
                .province("Test Province")
                .campaign(campaign)
                .totalBudget(new BigDecimal("10000"))
                .amountNeededToRaise(new BigDecimal("10000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .sponsors(new ArrayList<>()) // Đảm bảo không null
                .build();
        project = projectRepository.save(project);

        // Create and save sponsor with a project
        Sponsor sponsor = Sponsor.builder()
                .companyName("Test Company")
                .businessField("Test Field")
                .project(project)
                .representative("Test Rep")
                .representativeEmail("test@email.com")
                .phoneNumber("1234567890")
                .value(new BigDecimal("1000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        sponsor = sponsorRepository.save(sponsor);
        project.getSponsors().add(sponsor); // Thêm sponsor vào danh sách
        project = projectRepository.save(project);

        // Delete the project to simulate project not found scenario
        projectRepository.delete(project);

        // Setup authentication
        UserResponse userResponse = UserResponse.builder()
                .email(account.getEmail())
                .password(account.getPassword())
                .isActive(true)
                .scope("admin")
                .build();
        customAccountDetails = new CustomAccountDetails(userResponse);
        when(authentication.getPrincipal()).thenReturn(customAccountDetails);

        // Setup request with all required fields
        SponsorUpdateRequestDTO requestDTO = new SponsorUpdateRequestDTO();
        requestDTO.setCompanyName("Eco Future Co.");
        requestDTO.setBusinessField("Environmental Solutions");
        requestDTO.setRepresentative("John Doe");
        requestDTO.setRepresentativeEmail("john@ecofuture.com");
        requestDTO.setPhoneNumber("0987654321");
        requestDTO.setNote("Test Note");
        requestDTO.setValue("2000");
        requestDTO.setContract("test-contract");

        // Create empty files
        MockMultipartFile file = new MockMultipartFile("file", new byte[0]);
        MockMultipartFile logo = new MockMultipartFile("logo", new byte[0]);

        // Test
        Sponsor finalSponsor = sponsor;
        AppException exception = assertThrows(AppException.class, () -> {
            sponsorService.update(finalSponsor.getSponsorId(), requestDTO, file, logo);
        });

        // Verify
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, exception.getErrorCode());

        // Verify that sponsor was updated before the project not found error
        Sponsor updatedSponsor = sponsorRepository.findById(finalSponsor.getSponsorId()).orElseThrow();
        assertEquals("Eco Future Co.", updatedSponsor.getCompanyName());
        assertEquals("Environmental Solutions", updatedSponsor.getBusinessField());
        assertEquals("John Doe", updatedSponsor.getRepresentative());
        assertEquals("john@ecofuture.com", updatedSponsor.getRepresentativeEmail());
        assertEquals("0987654321", updatedSponsor.getPhoneNumber());
        assertEquals("Test Note", updatedSponsor.getNote());
        assertEquals(new BigDecimal("2000"), updatedSponsor.getValue());
    }

    @Test
    @DisplayName("DSS2_update_06: Test contract file upload failed after project update")
    void testUpdate_ContractUploadFailed() throws IOException {
        // Setup test environment
        setupSuccessfulTestEnvironment();

        // Create project with initial budget and amount
        Project testProject = Project.builder()
                .title("Test Project")
                .status(1)
                .ward("Test Ward")
                .district("Test District")
                .province("Test Province")
                .totalBudget(new BigDecimal("10000"))
                .amountNeededToRaise(new BigDecimal("10000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .sponsors(new ArrayList<>()) // Ensure sponsors list is initialized
                .build();
        testProject = projectRepository.save(testProject);

        // Create initial sponsor with existing contract
        Sponsor sponsor = Sponsor.builder()
                .companyName("Test Company")
                .businessField("Test Field")
                .project(testProject)
                .representative("Test Rep")
                .representativeEmail("test@email.com")
                .phoneNumber("1234567890")
                .value(new BigDecimal("5000"))
                .contract("existing-contract-path")  // Set existing contract
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        sponsor = sponsorRepository.save(sponsor);
        testProject.getSponsors().add(sponsor); // Add sponsor to project's sponsors list
        testProject = projectRepository.save(testProject);

        // Create another sponsor for the same project to test total calculation
        Sponsor sponsor2 = Sponsor.builder()
                .companyName("Test Company 2")
                .businessField("Test Field")
                .project(testProject)
                .representative("Test Rep 2")
                .representativeEmail("test2@email.com")
                .phoneNumber("1234567891")
                .value(new BigDecimal("4000"))  // This plus sponsor1's 5000 makes 9000 total
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        sponsor2 = sponsorRepository.save(sponsor2);
        testProject.getSponsors().add(sponsor2); // Add sponsor2 to project's sponsors list
        testProject = projectRepository.save(testProject);

        // Setup request with contract = null to trigger contract deletion logic
        SponsorUpdateRequestDTO requestDTO = new SponsorUpdateRequestDTO();
        requestDTO.setCompanyName("Eco Future Co.");
        requestDTO.setBusinessField("Environmental Solutions");
        requestDTO.setRepresentative("John Doe");
        requestDTO.setRepresentativeEmail("john@ecofuture.com");
        requestDTO.setPhoneNumber("0987654321");
        requestDTO.setValue("1000");  // This would make total 10000 (5000 + 4000 + 1000)
        requestDTO.setNote("Test Note");
        requestDTO.setContract(null); // Đảm bảo vào nhánh kiểm tra contract rỗng

        // Setup mock files
        MockMultipartFile contractFile = new MockMultipartFile("contract", "some-contract-path.pdf", "application/pdf", "fake pdf content".getBytes());
        MockMultipartFile logoFile = new MockMultipartFile("logo", new byte[0]);

        // Mock Firebase service để ném IOException khi xóa contract cũ
        when(firebaseService.deleteFileByPath(eq("existing-contract-path"))).thenThrow(new IOException());

        // ACT & ASSERT
        Sponsor finalSponsor = sponsor;
        AppException exception = assertThrows(AppException.class, () -> {
            sponsorService.update(finalSponsor.getSponsorId(), requestDTO, contractFile, logoFile);
        });
        assertEquals(ErrorCode.UPLOAD_FAILED, exception.getErrorCode());
    }

    @Test
    @DisplayName("DSS2_update_07: Test file upload failed")
    void testUpdate_FileUploadFailed() throws IOException {
        // Setup test environment
        setupSuccessfulTestEnvironment();

        // Create sponsor
        Sponsor sponsor = Sponsor.builder()
                .companyName("Test Company")
                .businessField("Test Field")
                .project(project)
                .representative("Test Rep")
                .representativeEmail("test@email.com")
                .phoneNumber("1234567890")
                .value(new BigDecimal("1000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        sponsor = sponsorRepository.save(sponsor);

        // Setup request
        SponsorUpdateRequestDTO requestDTO = new SponsorUpdateRequestDTO();
        requestDTO.setCompanyName("Eco Future Co.");
        requestDTO.setBusinessField("Environmental Solutions");
        requestDTO.setRepresentative("John Doe");
        requestDTO.setRepresentativeEmail("john@ecofuture.com");
        requestDTO.setPhoneNumber("0987654321");
        requestDTO.setValue("1000");
        requestDTO.setNote("Test Note");

        // Setup mock files where file is null
        MockMultipartFile file = null;
        MockMultipartFile logo = new MockMultipartFile("logo", new byte[0]);

        // Test and verify
        Sponsor finalSponsor = sponsor;
        AppException exception = assertThrows(AppException.class, () -> {
            sponsorService.update(finalSponsor.getSponsorId(), requestDTO, file, logo);
        });

        assertEquals(ErrorCode.UPLOAD_FAILED, exception.getErrorCode());
    }

    @Test
    @DisplayName("DSS2_update_08: Test logo upload failed")
    void testUpdate_LogoUploadFailed() throws IOException {
        // Setup test environment
        setupSuccessfulTestEnvironment();

        // Create sponsor
        Sponsor sponsor = Sponsor.builder()
                .companyName("Test Company")
                .businessField("Test Field")
                .project(project)
                .representative("Test Rep")
                .representativeEmail("test@email.com")
                .phoneNumber("1234567890")
                .value(new BigDecimal("1000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        sponsor = sponsorRepository.save(sponsor);

        // Setup request
        SponsorUpdateRequestDTO requestDTO = new SponsorUpdateRequestDTO();
        requestDTO.setCompanyName("Eco Future Co.");
        requestDTO.setBusinessField("Environmental Solutions");
        requestDTO.setRepresentative("John Doe");
        requestDTO.setRepresentativeEmail("john@ecofuture.com");
        requestDTO.setPhoneNumber("0987654321");
        requestDTO.setValue("1000");
        requestDTO.setNote("Test Note");

        // Setup mock files
        MockMultipartFile contractFile = new MockMultipartFile("contract", new byte[0]);
        MockMultipartFile logoFile = new MockMultipartFile("logo", "logo.png", "image/png", "test".getBytes());

        // Mock Firebase service to throw IOException for logo upload
        when(firebaseService.uploadOneFile(eq(logoFile), any(), any())).thenThrow(new IOException());

        // Test and verify
        Sponsor finalSponsor = sponsor;
        AppException exception = assertThrows(AppException.class, () -> {
            sponsorService.update(finalSponsor.getSponsorId(), requestDTO, contractFile, logoFile);
        });

        assertEquals(ErrorCode.UPLOAD_FAILED, exception.getErrorCode());
    }

}

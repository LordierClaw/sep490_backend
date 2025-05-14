package vn.com.fpt.sep490_g28_summer2024_be.unittest.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.assign.AssignResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.authentication.UserResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.entity.*;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.repository.*;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.service.assign.AssignServiceImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AssignServiceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AssignRepository assignRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AssignServiceImpl assignService;

    @MockBean
    private Authentication authentication;

    @MockBean
    private SecurityContext securityContext;

    private Account account;
    private Project project;
    private Campaign campaign;
    private Role adminRole;
    private Role pmRole;
    private CustomAccountDetails customAccountDetails;
    private final LocalDateTime testDateTime = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        // Clean up data
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE assign").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE project").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE campaign").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE account").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE role").executeUpdate();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

        // Setup roles
        adminRole = Role.builder()
                .roleName("Admin")
                .roleDescription("Administrator")
                .isActive(true)
                .createdAt(testDateTime)
                .build();
        adminRole = roleRepository.save(adminRole);

        pmRole = Role.builder()
                .roleName("Project Manager")
                .roleDescription("Project Manager")
                .isActive(true)
                .createdAt(testDateTime)
                .build();
        pmRole = roleRepository.save(pmRole);

        // Setup admin account
        account = Account.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("Test@123"))
                .fullname("Test Admin")
                .role(adminRole)
                .isActive(true)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();
        account = accountRepository.save(account);

        // Setup campaign
        campaign = Campaign.builder()
                .title("Test Campaign")
                .description("Test Campaign Description")
                .isActive(true)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
        campaign = campaignRepository.save(campaign);

        // Setup project
        project = Project.builder()
                .title("Test Project")
                .status(1)
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

        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockSecurityContext(Account account) {
        UserResponse userResponse = UserResponse.builder()
                .email(account.getEmail())
                .fullname(account.getFullname())
                .isActive(account.getIsActive())
                .scope(account.getRole().getRoleName())
                .build();
                
        CustomAccountDetails accountDetails = new CustomAccountDetails(userResponse);
        
        when(authentication.getPrincipal()).thenReturn(accountDetails);
    }

    @Test
    @DisplayName("AS_viewMemberInProjectByFilter_01")
    void viewMemberInProjectByFilter_shouldReturnFilteredMembers() {
        // Arrange
        Account pmAccount = Account.builder()
                .email("pm@test.com")
                .password(passwordEncoder.encode("Test@123"))
                .fullname("Test PM")
                .role(pmRole)
                .isActive(true)
                .createdAt(testDateTime)
                .build();
        pmAccount = accountRepository.save(pmAccount);

        Assign assign = Assign.builder()
                .project(project)
                .account(pmAccount)
                .createdBy(account)
                .createdAt(testDateTime)
                .build();
        assign = assignRepository.save(assign);

        mockSecurityContext(account);

        // Act
        PageResponse<AssignResponseDTO> result = assignService.viewMemberInProjectByFilter(
                0, 10, project.getProjectId(), pmRole.getRoleId(), null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(pmAccount.getEmail(), result.getContent().get(0).getAccountDTO().getEmail());
    }

    @Test
    @DisplayName("AS_viewMemberInProjectByFilter_02")
    void viewMemberInProjectByFilter_shouldReturnEmptyForNonExistentRole() {
        // Arrange
        mockSecurityContext(account);

        // Act
        PageResponse<AssignResponseDTO> result = assignService.viewMemberInProjectByFilter(
                0, 10, project.getProjectId(), BigInteger.valueOf(999), null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("AS_viewMembersNotAssignedToProject_01")
    void viewMembersNotAssignedToProject_shouldReturnUnassignedMembers() {
        // Arrange
        final Account unassignedPM = accountRepository.save(Account.builder()
                .email("unassigned@test.com")
                .password(passwordEncoder.encode("Test@123"))
                .fullname("Unassigned PM")
                .role(pmRole)
                .isActive(true)
                .createdAt(testDateTime)
                .build());

        mockSecurityContext(account);

        // Act
        List<AccountDTO> result = assignService.viewMembersNotAssignedToProject(project.getProjectId());

        // Assert
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(dto -> dto.getEmail().equals(unassignedPM.getEmail())));
    }

    @Test
    @DisplayName("AS_viewMembersNotAssignedToProject_02")
    void viewMembersNotAssignedToProject_shouldThrowExceptionForNonExistentProject() {
        // Arrange
        mockSecurityContext(account);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            assignService.viewMembersNotAssignedToProject(BigInteger.valueOf(999));
        });
        
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    @DisplayName("AS_addMembersToProject_01")
    void addMembersToProject_shouldAddMembersSuccessfully() {
        // Arrange
        Account pmAccount = Account.builder()
                .email("pm@test.com")
                .password(passwordEncoder.encode("Test@123"))
                .fullname("Test PM")
                .role(pmRole)
                .isActive(true)
                .createdAt(testDateTime)
                .build();
        pmAccount = accountRepository.save(pmAccount);

        mockSecurityContext(account);

        // Act
        List<Assign> result = assignService.addMembersToProject(
                Collections.singletonList(pmAccount.getAccountId()),
                project.getProjectId()
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(pmAccount.getEmail(), result.get(0).getAccount().getEmail());
    }

    @Test
    @DisplayName("AS_removeMember_01")
    void removeMember_shouldRemoveMemberSuccessfully() {
        // Arrange
        Account pmAccount = Account.builder()
                .email("pm@test.com")
                .password(passwordEncoder.encode("Test@123"))
                .fullname("Test PM")
                .role(pmRole)
                .isActive(true)
                .createdAt(testDateTime)
                .build();
        pmAccount = accountRepository.save(pmAccount);

        Assign assign = Assign.builder()
                .project(project)
                .account(pmAccount)
                .createdBy(account)
                .createdAt(testDateTime)
                .build();
        assign = assignRepository.save(assign);

        mockSecurityContext(account);

        // Act
        assignService.removeMember(assign.getAssignId());

        // Assert
        assertTrue(assignRepository.findById(assign.getAssignId()).isEmpty());
    }

    @Test
    @DisplayName("AS_removeMember_02")
    void removeMember_shouldThrowExceptionForNonExistentAssignment() {
        // Arrange
        mockSecurityContext(account);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            assignService.removeMember(BigInteger.valueOf(999));
        });
        
        assertEquals(ErrorCode.ACCOUNT_NO_CONTENT, exception.getErrorCode());
    }

    @Test
    @DisplayName("AS_viewMemberInProjectByFilter_03")
    void viewMemberInProjectByFilter_shouldFilterByEmailAndName() {
        // Arrange
        final Account pmAccount1 = accountRepository.save(Account.builder()
                .email("pm1@test.com")
                .password(passwordEncoder.encode("Test@123"))
                .fullname("John PM")
                .role(pmRole)
                .isActive(true)
                .createdAt(testDateTime)
                .build());

        final Account pmAccount2 = accountRepository.save(Account.builder()
                .email("pm2@test.com")
                .password(passwordEncoder.encode("Test@123"))
                .fullname("Jane PM")
                .role(pmRole)
                .isActive(true)
                .createdAt(testDateTime)
                .build());

        assignRepository.save(Assign.builder()
                .project(project)
                .account(pmAccount1)
                .createdBy(account)
                .createdAt(testDateTime)
                .build());

        assignRepository.save(Assign.builder()
                .project(project)
                .account(pmAccount2)
                .createdBy(account)
                .createdAt(testDateTime)
                .build());

        mockSecurityContext(account);

        // Act
        PageResponse<AssignResponseDTO> result = assignService.viewMemberInProjectByFilter(
                0, 10, project.getProjectId(), null, "pm1@", "John");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("pm1@test.com", result.getContent().get(0).getAccountDTO().getEmail());
        assertEquals("John PM", result.getContent().get(0).getAccountDTO().getFullname());
    }

    @Test
    @DisplayName("AS_addMembersToProject_02")
    void addMembersToProject_shouldThrowExceptionWhenUserNotAdmin() {
        // Arrange
        final Account pmAccount = accountRepository.save(Account.builder()
                .email("pm@test.com")
                .password(passwordEncoder.encode("Test@123"))
                .fullname("Test PM")
                .role(pmRole)
                .isActive(true)
                .createdAt(testDateTime)
                .build());

        // Mock security context with PM account instead of admin
        mockSecurityContext(pmAccount);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            assignService.addMembersToProject(
                    Collections.singletonList(pmAccount.getAccountId()),
                    project.getProjectId()
            );
        });

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("AS_addMembersToProject_03")
    void addMembersToProject_shouldThrowExceptionWhenAddingNonPMUser() {
        // Arrange
        Role userRole = roleRepository.save(Role.builder()
                .roleName("User")
                .roleDescription("Normal User")
                .isActive(true)
                .createdAt(testDateTime)
                .build());

        final Account normalUser = accountRepository.save(Account.builder()
                .email("user@test.com")
                .password(passwordEncoder.encode("Test@123"))
                .fullname("Test User")
                .role(userRole)
                .isActive(true)
                .createdAt(testDateTime)
                .build());

        mockSecurityContext(account); // Using admin account

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            assignService.addMembersToProject(
                    Collections.singletonList(normalUser.getAccountId()),
                    project.getProjectId()
            );
        });

        assertEquals(ErrorCode.ROLE_MEMBER_NOT_VALID, exception.getErrorCode());
    }

    @Test
    @DisplayName("AS_addMembersToProject_04")
    void addMembersToProject_shouldThrowExceptionWhenMemberAlreadyAssigned() {
        // Arrange
        final Account pmAccount = accountRepository.save(Account.builder()
                .email("pm@test.com")
                .password(passwordEncoder.encode("Test@123"))
                .fullname("Test PM")
                .role(pmRole)
                .isActive(true)
                .createdAt(testDateTime)
                .build());

        // First assignment
        assignRepository.save(Assign.builder()
                .project(project)
                .account(pmAccount)
                .createdBy(account)
                .createdAt(testDateTime)
                .build());

        mockSecurityContext(account);

        // Act & Assert - Try to assign again
        AppException exception = assertThrows(AppException.class, () -> {
            assignService.addMembersToProject(
                    Collections.singletonList(pmAccount.getAccountId()),
                    project.getProjectId()
            );
        });

        assertEquals(ErrorCode.MEMBER_ALREADY_ASSIGNED, exception.getErrorCode());
    }

    @Test
    @DisplayName("AS_addMembersToProject_05")
    void addMembersToProject_shouldThrowExceptionWhenProjectNotFound() {
        // Arrange
        final Account pmAccount = accountRepository.save(Account.builder()
                .email("pm@test.com")
                .password(passwordEncoder.encode("Test@123"))
                .fullname("Test PM")
                .role(pmRole)
                .isActive(true)
                .createdAt(testDateTime)
                .build());

        mockSecurityContext(account);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            assignService.addMembersToProject(
                    Collections.singletonList(pmAccount.getAccountId()),
                    BigInteger.valueOf(999) // Non-existent project ID
            );
        });

        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    @DisplayName("AS_addMembersToProject_06")
    void addMembersToProject_shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        mockSecurityContext(Account.builder()
                .email("nonexistent@test.com")
                .role(adminRole)
                .build());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            assignService.addMembersToProject(
                    Collections.singletonList(BigInteger.valueOf(999)),
                    project.getProjectId()
            );
        });

        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, exception.getErrorCode());
    }
} 
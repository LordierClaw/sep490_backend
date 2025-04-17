package vn.com.fpt.sep490_g28_summer2024_be.unittest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.assign.AssignResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Assign;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Project;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Role;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AssignRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.service.assign.AssignServiceImpl;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssignServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AssignRepository assignRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AssignServiceImpl assignService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Test case: AS_viewMemberInProjectByFilter_01
     * Description: Lọc thành viên hợp lệ với projectId và không filter gì thêm
     * Input: page=0, size=5, projectId=1
     * Expected: Trả về danh sách các AssignResponseDTO đúng project
     */
    @Test
    public void AS_viewMemberInProjectByFilter_01() {
        // Arrange
        Integer page = 0;
        Integer size = 5;
        BigInteger projectId = BigInteger.valueOf(1);
        
        Account account = Account.builder()
                .accountId(BigInteger.valueOf(1))
                .email("test@email.com")
                .phone("1234567890")
                .fullname("Test User")
                .build();
        
        Project project = Project.builder()
                .projectId(projectId)
                .build();
        
        Assign assign = Assign.builder()
                .assignId(BigInteger.valueOf(1))
                .account(account)
                .project(project)
                .build();
        
        List<Assign> assigns = Collections.singletonList(assign);
        Page<Assign> assignPage = new PageImpl<>(assigns);
        
        when(assignRepository.findMembersInProject(
                eq(projectId), 
                any(), 
                any(), 
                any(), 
                any(Pageable.class)
        )).thenReturn(assignPage);
        
        // Act
        PageResponse<AssignResponseDTO> result = assignService.viewMemberInProjectByFilter(
                page, size, projectId, null, null, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(BigInteger.valueOf(1), result.getContent().get(0).getAssignId());
        assertEquals(BigInteger.valueOf(1), result.getContent().get(0).getAccountDTO().getAccountId());
    }
    
    /**
     * Test case: AS_viewMemberInProjectByFilter_02
     * Description: Lọc với roleId không khớp
     * Input: projectId=1, roleId=999
     * Expected: Trả về danh sách rỗng
     */
    @Test
    public void AS_viewMemberInProjectByFilter_02() {
        // Arrange
        Integer page = 0;
        Integer size = 5;
        BigInteger projectId = BigInteger.valueOf(1);
        BigInteger roleId = BigInteger.valueOf(999);
        
        // Empty page result
        Page<Assign> emptyPage = new PageImpl<>(new ArrayList<>());
        
        when(assignRepository.findMembersInProject(
                eq(projectId), 
                eq(roleId), 
                any(), 
                any(), 
                any(Pageable.class)
        )).thenReturn(emptyPage);
        
        // Act
        PageResponse<AssignResponseDTO> result = assignService.viewMemberInProjectByFilter(
                page, size, projectId, roleId, null, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }
    
    /**
     * Test case: AS_viewMemberInProjectByFilter_03
     * Description: Lọc với email & name chứa từ khoá
     * Input: email="test@", name="Nam"
     * Expected: Trả về đúng danh sách có tên/email khớp
     */
    @Test
    public void AS_viewMemberInProjectByFilter_03() {
        // Arrange
        Integer page = 0;
        Integer size = 5;
        BigInteger projectId = BigInteger.valueOf(1);
        String email = "test@";
        String name = "Nam";
        
        Account account = Account.builder()
                .accountId(BigInteger.valueOf(1))
                .email("test@email.com")
                .phone("1234567890")
                .fullname("Nam")
                .build();
        
        Project project = Project.builder()
                .projectId(projectId)
                .build();
        
        Assign assign = Assign.builder()
                .assignId(BigInteger.valueOf(1))
                .account(account)
                .project(project)
                .build();
        
        List<Assign> assigns = Collections.singletonList(assign);
        Page<Assign> assignPage = new PageImpl<>(assigns);
        
        when(assignRepository.findMembersInProject(
                eq(projectId), 
                any(), 
                eq(email), 
                eq(name), 
                any(Pageable.class)
        )).thenReturn(assignPage);
        
        // Act
        PageResponse<AssignResponseDTO> result = assignService.viewMemberInProjectByFilter(
                page, size, projectId, null, email, name);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("test@email.com", result.getContent().get(0).getAccountDTO().getEmail());
        assertEquals("Nam", result.getContent().get(0).getAccountDTO().getFullname());
    }
    
    /**
     * Test case: AS_viewMembersNotAssignedToProject_01
     * Description: Trả về danh sách account chưa được gán
     * Input: projectId tồn tại (ID = 1)
     * Expected: Danh sách AccountDTO không bị gán
     */
    @Test
    public void AS_viewMembersNotAssignedToProject_01() {
        // Arrange
        BigInteger projectId = BigInteger.valueOf(1);
        
        Project project = Project.builder()
                .projectId(projectId)
                .build();
        
        Account account = Account.builder()
                .accountId(BigInteger.valueOf(2))
                .email("unassigned@email.com")
                .build();
        
        List<Account> accounts = Collections.singletonList(account);
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(accountRepository.findActiveAccountsNotAssignedToProject(projectId))
                .thenReturn(accounts);
        
        // Act
        List<AccountDTO> result = assignService.viewMembersNotAssignedToProject(projectId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("unassigned@email.com", result.get(0).getEmail());
    }
    
    /**
     * Test case: AS_viewMembersNotAssignedToProject_02
     * Description: Ném lỗi khi project không tồn tại
     * Input: projectId = 999
     * Expected: AppException với mã PROJECT_NOT_EXISTED
     */
    @Test
    public void AS_viewMembersNotAssignedToProject_02() {
        // Arrange
        BigInteger projectId = BigInteger.valueOf(999);
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
        
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            assignService.viewMembersNotAssignedToProject(projectId);
        });
        
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, exception.getErrorCode());
    }
    
    /**
     * Test case: AS_addMembersToProject_01
     * Description: Gán thành công 1 PM vào project
     * Input: Tài khoản đăng nhập là Admin, accountIds chứa 1 Project Manager
     * Expected: Trả về list Assign chứa thông tin được tạo
     */
    @Test
    public void AS_addMembersToProject_01() {
        // Arrange
        BigInteger projectId = BigInteger.valueOf(1);
        List<BigInteger> accountIds = Collections.singletonList(BigInteger.valueOf(1));
        
        // Reset and reconfigure SecurityContextHolder for this test
        SecurityContextHolder.clearContext();
        SecurityContextHolder.setContext(securityContext);
        
        // Setup authentication with admin user
        CustomAccountDetails customAccountDetails = mock(CustomAccountDetails.class);
        when(customAccountDetails.getUsername()).thenReturn("admin@email.com");
        when(authentication.getPrincipal()).thenReturn(customAccountDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        
        // Setup repositories
        Role adminRole = Role.builder()
                .roleId(BigInteger.valueOf(1))
                .roleName("Admin")
                .build();
        
        Account adminAccount = Account.builder()
                .accountId(BigInteger.valueOf(10))
                .email("admin@email.com")
                .role(adminRole)
                .build();
        
        Role pmRole = Role.builder()
                .roleId(BigInteger.valueOf(2))
                .roleName("project manager")
                .build();
        
        Account pmAccount = Account.builder()
                .accountId(BigInteger.valueOf(1))
                .email("pm@email.com")
                .role(pmRole)
                .build();
        
        Project project = Project.builder()
                .projectId(projectId)
                .build();
        
        Assign assign = Assign.builder()
                .assignId(BigInteger.valueOf(1))
                .account(pmAccount)
                .project(project)
                .createdBy(adminAccount)
                .createdAt(LocalDateTime.now())
                .build();
        
        // Make sure the admin account is found by email
        when(accountRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(adminAccount));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(accountRepository.findAllById(accountIds)).thenReturn(Collections.singletonList(pmAccount));
        when(assignRepository.findByProject_ProjectIdAndAccount_AccountIdIn(projectId, accountIds))
                .thenReturn(Collections.emptyList());
        when(assignRepository.saveAll(any())).thenReturn(Collections.singletonList(assign));
        
        // Act
        List<Assign> result = assignService.addMembersToProject(accountIds, projectId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(pmAccount, result.get(0).getAccount());
        assertEquals(project, result.get(0).getProject());
        
        // Verify method calls
        verify(accountRepository).findByEmail("admin@email.com");
        verify(projectRepository).findById(projectId);
        verify(accountRepository).findAllById(accountIds);
        verify(assignRepository).findByProject_ProjectIdAndAccount_AccountIdIn(projectId, accountIds);
        verify(assignRepository).saveAll(any());
    }
    
    /**
     * Test case: AS_addMembersToProject_02
     * Description: Ném lỗi nếu tài khoản đăng nhập không phải Admin
     * Input: Tài khoản đăng nhập là project manager
     * Expected: AppException với mã ACCESS_DENIED
     */
    @Test
    public void AS_addMembersToProject_02() {
        // Arrange
        BigInteger projectId = BigInteger.valueOf(1);
        List<BigInteger> accountIds = Collections.singletonList(BigInteger.valueOf(1));
        
        // Reset and reconfigure SecurityContextHolder for this test
        SecurityContextHolder.clearContext();
        SecurityContextHolder.setContext(securityContext);
        
        // Setup authentication with PM user
        CustomAccountDetails customAccountDetails = mock(CustomAccountDetails.class);
        when(customAccountDetails.getUsername()).thenReturn("pm@email.com");
        when(authentication.getPrincipal()).thenReturn(customAccountDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        
        // Setup repositories
        Role pmRole = Role.builder()
                .roleId(BigInteger.valueOf(2))
                .roleName("project manager")
                .build();
        
        Account pmAccount = Account.builder()
                .accountId(BigInteger.valueOf(1))
                .email("pm@email.com")
                .role(pmRole)
                .build();
        
        Project project = Project.builder()
                .projectId(projectId)
                .build();
        
        when(accountRepository.findByEmail("pm@email.com")).thenReturn(Optional.of(pmAccount));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            assignService.addMembersToProject(accountIds, projectId);
        });
        
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
        
        // Verify method calls
        verify(accountRepository).findByEmail("pm@email.com");
        verify(projectRepository).findById(projectId);
    }
    
    /**
     * Test case: AS_addMembersToProject_03
     * Description: Ném lỗi nếu trong danh sách có account không phải Project Manager
     * Input: 1 trong các accountIds là role khác
     * Expected: AppException với ROLE_MEMBER_NOT_VALID
     */
    @Test
    public void AS_addMembersToProject_03() {
        // Arrange
        BigInteger projectId = BigInteger.valueOf(1);
        List<BigInteger> accountIds = Collections.singletonList(BigInteger.valueOf(1));
        
        // Reset and reconfigure SecurityContextHolder for this test
        SecurityContextHolder.clearContext();
        SecurityContextHolder.setContext(securityContext);
        
        // Setup authentication with admin user
        CustomAccountDetails customAccountDetails = mock(CustomAccountDetails.class);
        when(customAccountDetails.getUsername()).thenReturn("admin@email.com");
        when(authentication.getPrincipal()).thenReturn(customAccountDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        
        // Setup repositories
        Role adminRole = Role.builder()
                .roleId(BigInteger.valueOf(1))
                .roleName("Admin")
                .build();
        
        Account adminAccount = Account.builder()
                .accountId(BigInteger.valueOf(10))
                .email("admin@email.com")
                .role(adminRole)
                .build();
        
        Role userRole = Role.builder()
                .roleId(BigInteger.valueOf(3))
                .roleName("user")
                .build();
        
        Account userAccount = Account.builder()
                .accountId(BigInteger.valueOf(1))
                .email("user@email.com")
                .role(userRole)
                .build();
        
        Project project = Project.builder()
                .projectId(projectId)
                .build();
        
        when(accountRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(adminAccount));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(accountRepository.findAllById(accountIds)).thenReturn(Collections.singletonList(userAccount));
        
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            assignService.addMembersToProject(accountIds, projectId);
        });
        
        assertEquals(ErrorCode.ROLE_MEMBER_NOT_VALID, exception.getErrorCode());
        
        // Verify method calls
        verify(accountRepository).findByEmail("admin@email.com");
        verify(projectRepository).findById(projectId);
        verify(accountRepository).findAllById(accountIds);
    }
    
    /**
     * Test case: AS_addMembersToProject_04
     * Description: Ném lỗi nếu đã assign trước đó
     * Input: Account đã assign rồi
     * Expected: AppException với MEMBER_ALREADY_ASSIGNED
     */
    @Test
    public void AS_addMembersToProject_04() {
        // Arrange
        BigInteger projectId = BigInteger.valueOf(1);
        List<BigInteger> accountIds = Collections.singletonList(BigInteger.valueOf(1));
        
        // Reset and reconfigure SecurityContextHolder for this test
        SecurityContextHolder.clearContext();
        SecurityContextHolder.setContext(securityContext);
        
        // Setup authentication with admin user
        CustomAccountDetails customAccountDetails = mock(CustomAccountDetails.class);
        when(customAccountDetails.getUsername()).thenReturn("admin@email.com");
        when(authentication.getPrincipal()).thenReturn(customAccountDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        
        // Setup repositories
        Role adminRole = Role.builder()
                .roleId(BigInteger.valueOf(1))
                .roleName("Admin")
                .build();
        
        Account adminAccount = Account.builder()
                .accountId(BigInteger.valueOf(10))
                .email("admin@email.com")
                .role(adminRole)
                .build();
        
        Role pmRole = Role.builder()
                .roleId(BigInteger.valueOf(2))
                .roleName("project manager")
                .build();
        
        Account pmAccount = Account.builder()
                .accountId(BigInteger.valueOf(1))
                .email("pm@email.com")
                .role(pmRole)
                .build();
        
        Project project = Project.builder()
                .projectId(projectId)
                .build();
        
        Assign existingAssign = Assign.builder()
                .assignId(BigInteger.valueOf(1))
                .account(pmAccount)
                .project(project)
                .build();
        
        when(accountRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(adminAccount));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(accountRepository.findAllById(accountIds)).thenReturn(Collections.singletonList(pmAccount));
        when(assignRepository.findByProject_ProjectIdAndAccount_AccountIdIn(projectId, accountIds))
                .thenReturn(Collections.singletonList(existingAssign));
        
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            assignService.addMembersToProject(accountIds, projectId);
        });
        
        assertEquals(ErrorCode.MEMBER_ALREADY_ASSIGNED, exception.getErrorCode());
        
        // Verify method calls
        verify(accountRepository).findByEmail("admin@email.com");
        verify(projectRepository).findById(projectId);
        verify(accountRepository).findAllById(accountIds);
        verify(assignRepository).findByProject_ProjectIdAndAccount_AccountIdIn(projectId, accountIds);
    }
    
    /**
     * Test case: AS_addMembersToProject_05
     * Description: Ném lỗi nếu project không tồn tại
     * Input: projectId = 999
     * Expected: AppException với PROJECT_NOT_EXISTED
     */
    @Test
    public void AS_addMembersToProject_05() {
        // Arrange
        BigInteger projectId = BigInteger.valueOf(999);
        List<BigInteger> accountIds = Collections.singletonList(BigInteger.valueOf(1));
        
        // Reset and reconfigure SecurityContextHolder for this test
        SecurityContextHolder.clearContext();
        SecurityContextHolder.setContext(securityContext);
        
        // Setup authentication with admin user
        CustomAccountDetails customAccountDetails = mock(CustomAccountDetails.class);
        when(customAccountDetails.getUsername()).thenReturn("admin@email.com");
        when(authentication.getPrincipal()).thenReturn(customAccountDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        
        // Setup repositories
        Role adminRole = Role.builder()
                .roleId(BigInteger.valueOf(1))
                .roleName("Admin")
                .build();
        
        Account adminAccount = Account.builder()
                .accountId(BigInteger.valueOf(10))
                .email("admin@email.com")
                .role(adminRole)
                .build();
        
        when(accountRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(adminAccount));
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
        
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            assignService.addMembersToProject(accountIds, projectId);
        });
        
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, exception.getErrorCode());
        
        // Verify method calls
        verify(accountRepository).findByEmail("admin@email.com");
        verify(projectRepository).findById(projectId);
    }
    
    /**
     * Test case: AS_removeMember_01
     * Description: Xoá assign hợp lệ
     * Input: assignId tồn tại
     * Expected: Gọi delete() thành công, không exception
     */
    @Test
    public void AS_removeMember_01() {
        // Arrange
        BigInteger assignId = BigInteger.valueOf(1);
        
        Account account = Account.builder()
                .accountId(BigInteger.valueOf(1))
                .email("pm@email.com")
                .build();
        
        Project project = Project.builder()
                .projectId(BigInteger.valueOf(1))
                .build();
        
        Assign assign = Assign.builder()
                .assignId(assignId)
                .account(account)
                .project(project)
                .build();
        
        when(assignRepository.findById(assignId)).thenReturn(Optional.of(assign));
        
        // Act
        assignService.removeMember(assignId);
        
        // Assert
        verify(assignRepository, times(1)).delete(assign);
    }
    
    /**
     * Test case: AS_removeMember_02
     * Description: Ném lỗi nếu assignId không tồn tại
     * Input: assignId = 999
     * Expected: AppException với mã ACCOUNT_NO_CONTENT
     */
    @Test
    public void AS_removeMember_02() {
        // Arrange
        BigInteger assignId = BigInteger.valueOf(999);
        
        when(assignRepository.findById(assignId)).thenReturn(Optional.empty());
        
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            assignService.removeMember(assignId);
        });
        
        assertEquals(ErrorCode.ACCOUNT_NO_CONTENT, exception.getErrorCode());
    }
} 
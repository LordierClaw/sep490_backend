package vn.com.fpt.sep490_g28_summer2024_be;

import jakarta.mail.MessagingException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountForgotPasswordDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountProfilePageDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountRegisterDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.client.AmbassadorResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.client.TopAmbassadorResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.authentication.UserResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.role.RoleDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Role;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseService;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseServiceImpl;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.DonationRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.RoleRepository;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.service.account.AccountService;
import vn.com.fpt.sep490_g28_summer2024_be.service.email.DefaultEmailService;
import vn.com.fpt.sep490_g28_summer2024_be.service.email.EmailService;
import vn.com.fpt.sep490_g28_summer2024_be.utils.CodeUtils;
import vn.com.fpt.sep490_g28_summer2024_be.utils.Email;
import vn.com.fpt.sep490_g28_summer2024_be.utils.OtpUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@MockBean(FirebaseServiceImpl.class)
@MockBean(DefaultEmailService.class)
public class AccountServiceTest {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpUtils otpUtils;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CodeUtils codeUtils;

    @Autowired
    private EmailService emailService;

    @Autowired
    private FirebaseService firebaseService;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void prepareForEachTest() {
        entityManager.createNativeQuery(
                        "INSERT INTO role (role_id, role_name, role_description, created_at, updated_at, is_active) VALUES " +
                                "(1, N'Admin', N'Quản lý dự án', '2024-06-05 11:00:00', '2024-06-05 11:00:00', TRUE)," +
                                "(2, N'Project Manager', N'Quản lí dự án ', '2024-06-06 12:00:00', '2024-06-06 12:00:00', TRUE)," +
                                "(3, N'Social Staff', N'Quản lí tin tức', '2024-06-07 13:00:00', '2024-06-07 13:00:00', TRUE)," +
                                "(4, N'System User', N'Khách', '2024-06-08 14:00:00', '2024-06-08 14:00:00', TRUE);")
                .executeUpdate();

        List<Role> roles = roleRepository.findAll();
        Account savedAccount = Account.builder()
                .email("newuser@example.com")
                .password(passwordEncoder.encode("123456"))
                .fullname("Nguyen Van A")
                .gender(1)
                .code("ACC001")
                .phone("0912345678")
                .address("123 Đường ABC")
                .dob(LocalDate.of(2003, 12, 31))
                .createdAt(LocalDate.now().atStartOfDay())
                .isActive(true)
                .role(roles.get(3))
                .build();
        accountRepository.save(savedAccount);

        savedAccount = Account.builder()
                .email("b@example.com")
                .password(passwordEncoder.encode("123456"))
                .fullname("Le Van A")
                .gender(1)
                .code("ACC002")
                .phone("0912345678")
                .address("123 Đường ABC")
                .dob(LocalDate.of(2003, 12, 31))
                .createdAt(LocalDate.now().atStartOfDay())
                .isActive(true)
                .role(roles.get(1))
                .build();
        accountRepository.save(savedAccount);
        otpUtils.delete("c@example.com");
    }

    void setupCustomAccountDetails() {
        // Mock CustomAccountDetails và UserResponse
        CustomAccountDetails mockDetails = mock(CustomAccountDetails.class);
        UserResponse mockUserResponse = mock(UserResponse.class);

        when(mockDetails.getUsername()).thenReturn("newuser@example.com");
        when(mockDetails.getUserResponse()).thenReturn(mockUserResponse);

        // Mock SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken(mockDetails, null);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("AS_createAccount_01")
    void createAccount_shouldSuccess_whenEmailNotExistsAndRoleValid() {
        List<Role> roles = roleRepository.findAll();
        Role testRole = roles.getFirst();
        String testRoleName = testRole.getRoleName();
        BigInteger testRoleId = testRole.getRoleId();

        AccountDTO request = AccountDTO.builder()
                .email("newuser1@example.com")
                .password("123456")
                .fullname("Nguyen Van A")
                .gender(1)
                .phone("0912345678")
                .address("123 Đường ABC")
                .dob(LocalDate.of(2003, 12, 31))
                .role(RoleDTO.builder().roleId(testRoleId).build())
                .build();

        AccountDTO response = accountService.createAccount(request);

        // Kiểm tra kết quả trả về đúng
        assertNotNull(response.getAccountId());
        assertEquals("newuser1@example.com", response.getEmail());
        assertEquals("Nguyen Van A", response.getFullname());
        assertEquals(1, response.getGender());
        assertNotNull(response.getRole());
        assertEquals(testRoleId, response.getRole().getRoleId());
        assertEquals(testRoleName, response.getRole().getRoleName());

        // Kiểm tra dữ liệu trong DB
        Optional<Account> savedOpt = accountRepository.findById(response.getAccountId());
        assertTrue(savedOpt.isPresent());

        Account saved = savedOpt.get();
        assertEquals("newuser1@example.com", saved.getEmail());
        assertTrue(passwordEncoder.matches("123456", saved.getPassword()));
        assertEquals("Nguyen Van A", saved.getFullname());
        assertEquals(1, saved.getGender());
        assertEquals("0912345678", saved.getPhone());
        assertEquals("123 Đường ABC", saved.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), saved.getDob());
        assertTrue(saved.getIsActive());
        assertNotNull(saved.getRole());
        assertEquals(testRoleId, saved.getRole().getRoleId());
        assertEquals(testRoleName, saved.getRole().getRoleName());
        assertNotNull(saved.getCode());
    }

    @Test
    @DisplayName("AS_createAccount_02")
    void createAccount_shouldReturnException_whenEmailExists() {
        List<Role> roles = roleRepository.findAll();
        Role testRole = roles.getFirst();
        BigInteger testRoleId = testRole.getRoleId();

        AccountDTO duplicateRequest = AccountDTO.builder()
                .email("newuser@example.com")
                .password("654321")
                .fullname("Nguyen Van B")
                .gender(0)
                .phone("0987654321")
                .address("456 Đường XYZ")
                .dob(LocalDate.of(2000, 1, 1))
                .role(RoleDTO.builder().roleId(testRoleId).build())
                .build();

        // Kiểm tra kết quả trả về đúng
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.createAccount(duplicateRequest);
        });

        assertEquals(ErrorCode.USER_EXISTED, exception.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        List<Account> accountsWithSameEmail = accountRepository.findByEmail("newuser@example.com").stream().toList();
        assertEquals(1, accountsWithSameEmail.size());

        // Kiểm tra thông tin tài khoản đã lưu là của người dùng, không phải người dùng thứ hai
        Account lastSavedAccount = accountsWithSameEmail.getFirst();
        assertEquals("Nguyen Van A", lastSavedAccount.getFullname());
        assertTrue(passwordEncoder.matches("123456", lastSavedAccount.getPassword()));
        assertNotEquals("Nguyen Van B", lastSavedAccount.getFullname());
    }

    @Test
    @DisplayName("AS_createAccount_03")
    void createAccount_shouldSuccess_whenEmailNotExistsAndRoleInvalid() {
        BigInteger testRoleId = BigInteger.ZERO;

        AccountDTO request = AccountDTO.builder()
                .email("newuser1@example.com")
                .password("123456")
                .fullname("Nguyen Van A")
                .gender(1)
                .phone("0912345678")
                .address("123 Đường ABC")
                .dob(LocalDate.of(2003, 12, 31))
                .role(RoleDTO.builder().roleId(testRoleId).build())
                .build();

        // Kiểm tra kết quả trả về đúng
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.createAccount(request);
        });

        assertEquals(ErrorCode.ROLE_NOT_EXISTED, exception.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser1@example.com");
        assertTrue(accountOpt.isEmpty());
    }

    @Test
    @DisplayName("AS_viewSystemUserAccountsByFilter_01")
    void viewSystemUserAccountsByFilter_shouldReturnEmptyList_whenNoAccountsFound() {
        Integer page = 0;
        Integer size = 10;
        String email = "a@example.com";

        PageResponse<AccountDTO> result = accountService.viewSystemUserAccountsByFilter(page, size, null, email, null, null, null);

        // Kiểm tra kết quả trả về đúng
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(10, result.getLimit());
        assertEquals(0, result.getOffset());
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("AS_viewSystemUserAccountsByFilter_02")
    void viewSystemUserAccountsByFilter_shouldReturnListOfAccountDTO_whenDonationIsNotNull() {
        Integer page = 0;
        Integer size = 10;
        String email = "newuser@example.com";

        Optional<Account> savedAccountOpt = accountRepository.findByEmail(email);
        assertTrue(savedAccountOpt.isPresent());

        Account savedAccount = savedAccountOpt.get();

        entityManager.createNativeQuery(
                        "INSERT INTO donation (value, created_at, created_by) VALUES " +
                                "(1000.00, '2024-06-05 11:00:00', ?)")
                .setParameter(1, savedAccount.getAccountId())
                .executeUpdate();

        PageResponse<AccountDTO> result = accountService.viewSystemUserAccountsByFilter(
                page, size, null, email, null, null, null);

        // Kiểm tra kết quả trả về đúng
        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        Optional<AccountDTO> testAccountDTOOpt = result.getContent().stream()
                .filter(dto -> dto.getEmail().equals(email))
                .findFirst();

        assertTrue(testAccountDTOOpt.isPresent());
        AccountDTO testAccountDTO = testAccountDTOOpt.get();

        // Kiểm tra thông tin tài khoản
        assertEquals("Nguyen Van A", testAccountDTO.getFullname());
        assertEquals(email, testAccountDTO.getEmail());
        assertEquals("0912345678", testAccountDTO.getPhone());

        // Kiểm tra giá trị tổng số tiền quyên góp lớn hơn 0
        assertNotNull(testAccountDTO.getTotalDonations());
        assertTrue(testAccountDTO.getTotalDonations().compareTo(BigDecimal.ZERO) > 0);

        // Kiểm tra thông tin paging
        assertEquals(10, result.getLimit());
        assertEquals(0, result.getOffset());
        assertTrue(result.getTotal() > 0);
    }

    @Test
    @DisplayName("AS_viewSystemUserAccountsByFilter_03")
    void viewSystemUserAccountsByFilter_shouldReturnListOfAccountDTO_whenDonationIsNull() {
        Integer page = 0;
        Integer size = 10;
        String email = "newuser@example.com";

        Optional<Account> savedAccountOpt = accountRepository.findByEmail(email);
        assertTrue(savedAccountOpt.isPresent());

        PageResponse<AccountDTO> result = accountService.viewSystemUserAccountsByFilter(
                page, size, null, email, null, null, null);

        // Kiểm tra kết quả trả về đúng
        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());

        // Find our test account in the results
        Optional<AccountDTO> testAccountDTOOpt = result.getContent().stream()
                .filter(dto -> dto.getEmail().equals(email))
                .findFirst();

        assertTrue(testAccountDTOOpt.isPresent());
        AccountDTO testAccountDTO = testAccountDTOOpt.get();

        // Kiểm tra thông tin tài khoản
        assertEquals("Nguyen Van A", testAccountDTO.getFullname());
        assertEquals(email, testAccountDTO.getEmail());
        assertEquals("0912345678", testAccountDTO.getPhone());

        // Kiểm tra giá trị tổng số tiền quyên góp bằng 0
        assertNotNull(testAccountDTO.getTotalDonations());
        assertThat(testAccountDTO.getTotalDonations()).isEqualByComparingTo(BigDecimal.ZERO);

        // Kiểm tra thông tin paging
        assertEquals(10, result.getLimit());
        assertEquals(0, result.getOffset());
        assertTrue(result.getTotal() > 0);
    }

    @Test
    @DisplayName("AS_viewByFilter_01")
    void viewByFilter_shouldReturnException_whenNoAccountLogged() {
        // Chuẩn bị
        setupCustomAccountDetails();
        Integer page = 0;
        Integer size = 10;
        String email = "a@example.com";

        CustomAccountDetails mockDetails = mock(CustomAccountDetails.class);
        UserResponse mockUserResponse = mock(UserResponse.class);

        when(mockDetails.getUsername()).thenReturn(email);
        when(mockDetails.getUserResponse()).thenReturn(mockUserResponse);

        // Mock SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken(mockDetails, null);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Kiểm tra kết quả trả về đúng
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.viewByFilter(page, size, email, null, null);
        });

        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    @DisplayName("AS_viewByFilter_02")
    void viewByFilter_shouldReturnEmptyList_whenNoAccountsFound() {
        setupCustomAccountDetails();
        Integer page = 0;
        Integer size = 10;
        String email = "a@example.com";

        PageResponse<AccountDTO> result = (PageResponse<AccountDTO>) accountService.viewByFilter(page, size, email, null, null);

        // Kiểm tra kết quả trả về đúng
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(10, result.getLimit());
        assertEquals(0, result.getOffset());
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("AS_viewByFilter_03")
    void viewByFilter_shouldReturnListOfAccountDTO_whenExistAccount() {
        setupCustomAccountDetails();
        Integer page = 0;
        Integer size = 10;
        String email = "b@example.com";

        PageResponse<AccountDTO> result = (PageResponse<AccountDTO>) accountService.viewByFilter(page, size, email, null, null);

        // Kiểm tra kết quả trả về đúng
        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());

        // Find our test account in the results
        Optional<AccountDTO> testAccountDTOOpt = result.getContent().stream()
                .filter(dto -> dto.getEmail().equals(email))
                .findFirst();

        assertTrue(testAccountDTOOpt.isPresent());
        AccountDTO testAccountDTO = testAccountDTOOpt.get();

        // Kiểm tra thông tin tài khoản
        assertEquals("Le Van A", testAccountDTO.getFullname());
        assertEquals(email, testAccountDTO.getEmail());
        assertEquals("0912345678", testAccountDTO.getPhone());

        // Kiểm tra thông tin paging
        assertEquals(10, result.getLimit());
        assertEquals(0, result.getOffset());
        assertTrue(result.getTotal() > 0);
    }

    @Test
    @DisplayName("AS_getAccountById_01")
    void getAccountById_shouldReturnException_whenAccountNotFound() {
        BigInteger accountId = BigInteger.valueOf(0);

        // Kiểm tra kết quả trả về đúng
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.getAccountById(accountId);
        });

        assertEquals(ErrorCode.ACCOUNT_NO_CONTENT, exception.getErrorCode());
    }

    @Test
    @DisplayName("AS_getAccountById_02")
    void getAccountById_shouldReturnListOfAccountDTO_whenAccountFound() {
        Optional<Account> savedAccountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(savedAccountOpt.isPresent());
        Account savedAccount = savedAccountOpt.get();
        BigInteger accountId = savedAccount.getAccountId();

        AccountDTO result = accountService.getAccountById(accountId);

        // Kiểm tra kết quả trả về đúng
        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertEquals("newuser@example.com", result.getEmail());
        assertEquals("Nguyen Van A", result.getFullname());
        assertEquals(1, result.getGender());
        assertEquals("0912345678", result.getPhone());
        assertEquals("123 Đường ABC", result.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), result.getDob());
        assertTrue(result.getIsActive());

        // Kiểm tra thông tin vai trò
        assertNotNull(result.getRole());
        assertEquals(savedAccount.getRole().getRoleId(), result.getRole().getRoleId());
        assertEquals(savedAccount.getRole().getRoleName(), result.getRole().getRoleName());
    }

    @Test
    @DisplayName("AS_getTotalDonateAndCountDonateByAccountCode_01")
    void getTotalDonateAndCountDonateByAccountCode_shouldReturnException_whenAccountNotFound() {
        String accountCode = "ACC000";

        // Kiểm tra kết quả trả về đúng
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.getTotalDonateAndCountDonateByAccountCode(accountCode);
        });

        assertEquals(ErrorCode.ACCOUNT_NO_CONTENT, exception.getErrorCode());
    }

    @Test
    @DisplayName("AS_getTotalDonateAndCountDonateByAccountCode_02")
    void getTotalDonateAndCountDonateByAccountCode_shouldReturnException_whenAccountIsNotSystemUser() {
        String accountCode = "ACC002";

        // Kiểm tra kết quả trả về đúng
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.getTotalDonateAndCountDonateByAccountCode(accountCode);
        });

        assertEquals(ErrorCode.ADMIN_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("AS_getTotalDonateAndCountDonateByAccountCode_03")
    void getTotalDonateAndCountDonateByAccountCode_shouldReturnZeroValues_whenNoDonationsExist() {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        AccountProfilePageDTO result = accountService.getTotalDonateAndCountDonateByAccountCode(account.getCode());

        // Kiểm tra kết quả trả về đúng
        assertNotNull(result);
        assertThat(result.getTotalDonations()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalDonationsRefer()).isEqualByComparingTo(BigDecimal.ZERO);
        assertEquals(0L, result.getTotalDonationReferCount());
        assertEquals(0L, result.getDonationCount());
        assertEquals(account.getCode(), result.getCode());
        assertEquals(account.getFullname(), result.getFullname());
        assertEquals(account.getReferCode(), result.getReferCode());
    }

    @Test
    @DisplayName("AS_getTotalDonateAndCountDonateByAccountCode_04")
    void getTotalDonateAndCountDonateByAccountCode_shouldReturnCorrectValues_whenAllDonationExist() {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        entityManager.createNativeQuery(
                        "INSERT INTO donation (value, created_at, created_by) VALUES " +
                                "(1000.00, '2024-06-05 11:00:00', ?)")
                .setParameter(1, account.getAccountId())
                .executeUpdate();

        entityManager.createNativeQuery(
                        "INSERT INTO donation (value, created_at, created_by, refer_id) VALUES " +
                                "(500.00, '2024-06-05 12:00:00', ?, ?)")
                .setParameter(1, account.getAccountId())
                .setParameter(2, account.getAccountId())
                .executeUpdate();

        entityManager.createNativeQuery(
                        "INSERT INTO challenge (challenge_code, title, slug, content, goal, created_by, created_at) VALUES " +
                                "('TT001', 'Test Challenge', 'test-challenge', 'Test content', 1000.00, ?, '2024-06-05 13:00:00')")
                .setParameter(1, account.getAccountId())
                .executeUpdate();

        Long challengeIdLong = (Long) entityManager.createNativeQuery(
                        "SELECT challenge_id FROM challenge WHERE challenge_code = 'TT001'")
                .getSingleResult();

        BigInteger challengeId = BigInteger.valueOf(challengeIdLong);

        entityManager.createNativeQuery(
                        "INSERT INTO donation (value, created_at, created_by, challenge_id) VALUES " +
                                "(300.00, '2024-06-05 13:00:00', ?, ?)")
                .setParameter(1, account.getAccountId())
                .setParameter(2, challengeId)
                .executeUpdate();

        AccountProfilePageDTO result = accountService.getTotalDonateAndCountDonateByAccountCode(account.getCode());

        // Kiểm tra kết quả trả về đúng
        assertNotNull(result);
        assertEquals(new BigDecimal("1800.00"), result.getTotalDonations()); // 1000 + 500 + 300
        assertEquals(new BigDecimal("800.00"), result.getTotalDonationsRefer()); // 500 + 300
        assertEquals(2L, result.getTotalDonationReferCount()); // 1 refer + 1 challenge
        assertEquals(3L, result.getDonationCount());
        assertEquals(account.getCode(), result.getCode());
        assertEquals(account.getFullname(), result.getFullname());
        assertEquals(account.getReferCode(), result.getReferCode());
    }

    @Test
    @DisplayName("AS_updateAccountByAdmin_01")
    void updateAccountByAdmin_shouldThrowAppException_whenAccountNotFound() {
        List<Role> roles = roleRepository.findAll();
        Role testRole = roles.getFirst();

        BigInteger nonExistentId = BigInteger.valueOf(0);
        AccountDTO updateDTO = AccountDTO.builder()
                .accountId(nonExistentId)
                .email("newuser@example.com")
                .fullname("Nguyen Van A")
                .gender(1)
                .phone("0912345678")
                .address("123 Đường ABC")
                .dob(LocalDate.of(2003, 12, 31))
                .role(RoleDTO.builder().roleId(testRole.getRoleId()).build())
                .build();

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.updateAccountByAdmin(updateDTO, nonExistentId);
        });
        assertEquals(ErrorCode.ACCOUNT_NO_CONTENT, thrown.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail("newuser@example.com").stream().toList();
        assertEquals(1, accounts.size());

        // Kiểm tra thông tin tài khoản không bị thay đổi
        Account lastSavedAccount = accounts.getFirst();
        assertEquals("newuser@example.com", lastSavedAccount.getEmail());
        assertEquals("Nguyen Van A", lastSavedAccount.getFullname());
        assertEquals(1, lastSavedAccount.getGender());
        assertEquals("0912345678", lastSavedAccount.getPhone());
        assertEquals("123 Đường ABC", lastSavedAccount.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), lastSavedAccount.getDob());
        assertNotEquals(testRole, lastSavedAccount.getRole());
    }

    @Test
    @DisplayName("AS_updateAccountByAdmin_02")
    void updateAccountByAdmin_shouldUpdateAccountAndReturnUpdatedDTO_whenAccountExists() {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        List<Role> roles = roleRepository.findAll();
        Role testRole = roles.getFirst();

        AccountDTO updateDTO = AccountDTO.builder()
                .accountId(account.getAccountId())
                .email("newuser@example.com")
                .fullname("Nguyen Van A")
                .gender(1)
                .phone("0912345678")
                .address("123 Đường ABC")
                .dob(LocalDate.of(2003, 12, 31))
                .role(RoleDTO.builder().roleId(testRole.getRoleId()).build())
                .build();

        AccountDTO accountDTO = accountService.updateAccountByAdmin(updateDTO, account.getAccountId());

        // Kiểm tra kết quả trả về đúng
        assertNotNull(accountDTO);
        assertEquals(account.getAccountId(), accountDTO.getAccountId());
        assertEquals("newuser@example.com", accountDTO.getEmail());
        assertEquals("Nguyen Van A", accountDTO.getFullname());
        assertEquals(1, accountDTO.getGender());
        assertEquals("0912345678", accountDTO.getPhone());
        assertEquals("123 Đường ABC", accountDTO.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), accountDTO.getDob());
        assertEquals(testRole.getRoleId(), accountDTO.getRole().getRoleId());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail("newuser@example.com").stream().toList();
        assertEquals(1, accounts.size());

        // Kiểm tra thông tin tài khoản dã được lưu
        Account lastSavedAccount = accounts.getFirst();
        assertEquals(account.getAccountId(), lastSavedAccount.getAccountId());
        assertEquals("newuser@example.com", lastSavedAccount.getEmail());
        assertEquals("Nguyen Van A", lastSavedAccount.getFullname());
        assertEquals(1, lastSavedAccount.getGender());
        assertEquals("0912345678", lastSavedAccount.getPhone());
        assertEquals("123 Đường ABC", lastSavedAccount.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), lastSavedAccount.getDob());
        assertEquals(testRole.getRoleId(), lastSavedAccount.getRole().getRoleId());
    }

    @Test
    @DisplayName("AS_deactivateAccount_01")
    void deactivateAccount_shouldThrowAppException_whenAccountNotFound() {
        BigInteger nonExistentId = BigInteger.valueOf(0);

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.deactivateAccount(nonExistentId);
        });
        assertEquals(ErrorCode.ACCOUNT_NO_CONTENT, thrown.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findById(nonExistentId).stream().toList();
        assertNotNull(accounts);
        assertEquals(0, accounts.size());
    }

    @Test
    @DisplayName("AS_deactivateAccount_02")
    void deactivateAccount_shouldUpdateAccountAndReturnUpdatedDTO_whenAccountExists() {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        AccountDTO accountDTO = accountService.deactivateAccount(account.getAccountId());

        // Kiểm tra kết quả trả về đúng
        assertNotNull(accountDTO);
        assertEquals(account.getAccountId(), accountDTO.getAccountId());
        assertEquals("newuser@example.com", accountDTO.getEmail());
        assertEquals("Nguyen Van A", accountDTO.getFullname());
        assertEquals(1, accountDTO.getGender());
        assertEquals("0912345678", accountDTO.getPhone());
        assertEquals("123 Đường ABC", accountDTO.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), accountDTO.getDob());
        assertEquals(account.getRole().getRoleId(), accountDTO.getRole().getRoleId());
        assertEquals(false, accountDTO.getIsActive());

        // Kiểm tra dữ liệu trong DB
        List<Account> accountsWithSameEmail = accountRepository.findByEmail("newuser@example.com").stream().toList();
        assertEquals(1, accountsWithSameEmail.size());

        // Kiểm tra thông tin tài khoản dã được lưu
        Account lastSavedAccount = accountsWithSameEmail.getFirst();
        assertEquals(account.getAccountId(), lastSavedAccount.getAccountId());
        assertEquals("newuser@example.com", lastSavedAccount.getEmail());
        assertEquals("Nguyen Van A", lastSavedAccount.getFullname());
        assertEquals(1, lastSavedAccount.getGender());
        assertEquals("0912345678", lastSavedAccount.getPhone());
        assertEquals("123 Đường ABC", lastSavedAccount.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), lastSavedAccount.getDob());
        assertEquals(account.getRole().getRoleId(), lastSavedAccount.getRole().getRoleId());
        assertEquals(false, lastSavedAccount.getIsActive());
    }

    @Test
    @DisplayName("AS_getAuthorNewsAccounts_01")
    void getAuthorNewsAccounts_shouldThrowAppException_whenAccountNotFound() {
        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.getAuthorNewsAccounts();
        });
        assertEquals(ErrorCode.ACCOUNT_NO_CONTENT, thrown.getErrorCode());
    }

    @Test
    @DisplayName("AS_getAuthorNewsAccounts_02")
    void getAuthorNewsAccounts_shouldReturnList_whenAccountExists() {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        entityManager.createNativeQuery(
                        "INSERT INTO sep.news (title, content, created_by, created_at, updated_by, updated_at) VALUES " +
                                "('Test News', 'test-challenge', ?, '2024-06-05 13:00:00', ?, '2024-06-05 13:00:00')")
                .setParameter(1, account.getAccountId())
                .setParameter(2, account.getAccountId())
                .executeUpdate();

        accountService.getAuthorNewsAccounts();

        // Kiểm tra kết quả trả về đúng
        List<AccountDTO> authorNewsAccounts = accountService.getAuthorNewsAccounts();
        assertNotNull(authorNewsAccounts);
        assertEquals(1, authorNewsAccounts.size());

        // Kiểm tra thông tin tài khoản
        AccountDTO authorAccount = authorNewsAccounts.getFirst();
        assertEquals(account.getAccountId(), authorAccount.getAccountId());
        assertEquals("Nguyen Van A", authorAccount.getFullname());
    }

    @Test
    @DisplayName("AS_getIdAndEmailProjectManagerAccounts_01")
    void getIdAndEmailProjectManagerAccounts_shouldThrowAppException_whenAccountNotFound() {
        Optional<Account> accountOpt = accountRepository.findByEmail("b@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        List<Role> roles = roleRepository.findAll();
        Role testRole = roles.getFirst();
        account.setRole(testRole);
        accountRepository.save(account);

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.getIdAndEmailProjectManagerAccounts();
        });
        assertEquals(ErrorCode.ACCOUNT_NO_CONTENT, thrown.getErrorCode());
    }

    @Test
    @DisplayName("AS_getIdAndEmailProjectManagerAccounts_02")
    void getIdAndEmailProjectManagerAccounts_shouldReturnList_whenAccountExists() {
        Optional<Account> accountOpt = accountRepository.findByEmail("b@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        accountService.getIdAndEmailProjectManagerAccounts();

        // Kiểm tra kết quả trả về đúng
        List<AccountDTO> projectManagerAccounts = accountService.getIdAndEmailProjectManagerAccounts();
        assertNotNull(projectManagerAccounts);
        assertEquals(1, projectManagerAccounts.size());

        // Kiểm tra thông tin tài khoản
        AccountDTO projectManagerAccount = projectManagerAccounts.getFirst();
        assertEquals(account.getAccountId(), projectManagerAccount.getAccountId());
        assertEquals("b@example.com", projectManagerAccount.getEmail());
    }

    @Test
    @DisplayName("AS_changePassword_01")
    void changePassword_shouldThrowAppException_whenAccountNotFound() {
        String nonExistentEmail = "a@example.com";
        String oldPassword = "123456";
        String newPassword = "654321";

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.changePassword(nonExistentEmail, oldPassword, newPassword);
        });
        assertEquals(ErrorCode.USER_NOT_EXISTED, thrown.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(nonExistentEmail).stream().toList();
        assertNotNull(accounts);
        assertEquals(0, accounts.size());
    }

    @Test
    @DisplayName("AS_changePassword_02")
    void changePassword_shouldThrowAppException_whenOldPasswordNotMatch() {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();
        String oldPassword = "1234567";
        String newPassword = "654321";

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.changePassword(account.getEmail(), oldPassword, newPassword);
        });
        assertEquals(ErrorCode.OLD_PASSWORD_INCORRECT, thrown.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(account.getEmail()).stream().toList();
        assertNotNull(accounts);
        assertEquals(1, accounts.size());

        // Kiểm tra thông tin tài khoản không bị thay đổi
        Account lastSavedAccount = accounts.getFirst();
        assertEquals(account.getAccountId(), lastSavedAccount.getAccountId());
        assertEquals("newuser@example.com", lastSavedAccount.getEmail());
        assertEquals("Nguyen Van A", lastSavedAccount.getFullname());
        assertEquals(1, lastSavedAccount.getGender());
        assertEquals("0912345678", lastSavedAccount.getPhone());
        assertEquals("123 Đường ABC", lastSavedAccount.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), lastSavedAccount.getDob());
        assertEquals(account.getRole().getRoleId(), lastSavedAccount.getRole().getRoleId());
        assertTrue(passwordEncoder.matches("123456", lastSavedAccount.getPassword()));
    }

    @Test
    @DisplayName("AS_changePassword_03")
    void changePassword_shouldThrowAppException_whenNewPasswordSameAsOldPassword() {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();
        String oldPassword = "123456";
        String newPassword = "123456";

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.changePassword(account.getEmail(), oldPassword, newPassword);
        });
        assertEquals(ErrorCode.NEW_PASSWORD_MUST_BE_DIFFERENT, thrown.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(account.getEmail()).stream().toList();
        assertNotNull(accounts);
        assertEquals(1, accounts.size());

        // Kiểm tra thông tin tài khoản không bị thay đổi
        Account lastSavedAccount = accounts.getFirst();
        assertEquals(account.getAccountId(), lastSavedAccount.getAccountId());
        assertEquals("newuser@example.com", lastSavedAccount.getEmail());
        assertEquals("Nguyen Van A", lastSavedAccount.getFullname());
        assertEquals(1, lastSavedAccount.getGender());
        assertEquals("0912345678", lastSavedAccount.getPhone());
        assertEquals("123 Đường ABC", lastSavedAccount.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), lastSavedAccount.getDob());
        assertEquals(account.getRole().getRoleId(), lastSavedAccount.getRole().getRoleId());
        assertTrue(passwordEncoder.matches("123456", lastSavedAccount.getPassword()));
    }

    @Test
    @DisplayName("AS_changePassword_04")
    void changePassword_shouldUpdatePasswordAndReturnUpdatedDTO_whenAccountExists() {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();
        String oldPassword = "123456";
        String newPassword = "654321";

        AccountDTO accountDTO = accountService.changePassword(account.getEmail(), oldPassword, newPassword);

        // Kiểm tra kết quả đúng
        assertEquals(account.getEmail(), accountDTO.getEmail());
        assertEquals(newPassword, accountDTO.getPassword());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(account.getEmail()).stream().toList();
        assertNotNull(accounts);
        assertEquals(1, accounts.size());

        // Kiểm tra thông tin tài khoản bị thay đổi
        Account lastSavedAccount = accounts.getFirst();
        assertEquals(account.getAccountId(), lastSavedAccount.getAccountId());
        assertEquals("newuser@example.com", lastSavedAccount.getEmail());
        assertEquals("Nguyen Van A", lastSavedAccount.getFullname());
        assertEquals(1, lastSavedAccount.getGender());
        assertEquals("0912345678", lastSavedAccount.getPhone());
        assertEquals("123 Đường ABC", lastSavedAccount.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), lastSavedAccount.getDob());
        assertEquals(account.getRole().getRoleId(), lastSavedAccount.getRole().getRoleId());
        assertTrue(passwordEncoder.matches(newPassword, lastSavedAccount.getPassword()));
    }

    @Test
    @DisplayName("AS_updateProfile_01")
    void updateProfile_shouldThrowAppException_whenAccountNotFound() {
        String nonExistentEmail = "a@example.com";
        AccountDTO accountDTO = AccountDTO.builder()
                .email(nonExistentEmail)
                .build();

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.updateProfile(nonExistentEmail, accountDTO, null);
        });
        assertEquals(ErrorCode.USER_NOT_EXISTED, thrown.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(nonExistentEmail).stream().toList();
        assertNotNull(accounts);
        assertEquals(0, accounts.size());
    }

    @Test
    @DisplayName("AS_updateProfile_02")
    void updateProfile_shouldUpdateProfileAndReturnUpdatedDTO_whenAvatarDTOExistAndAvatarFileIsNull() {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        AccountDTO accountDTO = AccountDTO.builder()
                .fullname(account.getFullname())
                .gender(account.getGender())
                .phone(account.getPhone())
                .address(account.getAddress())
                .avatar("https://example.com/avatar.jpg")
                .dob(account.getDob())
                .build();

        MultipartFile avatarFile = null;
        AccountDTO updatedAccountDTO = accountService.updateProfile(account.getEmail(), accountDTO, avatarFile);

        // Kiểm tra kết quả đúng
        assertEquals(account.getEmail(), updatedAccountDTO.getEmail());
        assertEquals(account.getFullname(), updatedAccountDTO.getFullname());
        assertEquals(account.getGender(), updatedAccountDTO.getGender());
        assertEquals(account.getPhone(), updatedAccountDTO.getPhone());
        assertEquals(account.getAddress(), updatedAccountDTO.getAddress());
        assertEquals(account.getAvatar(), updatedAccountDTO.getAvatar());
        assertEquals(account.getDob(), updatedAccountDTO.getDob());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(account.getEmail()).stream().toList();
        assertNotNull(accounts);
        assertEquals(1, accounts.size());

        // Kiểm tra thông tin tài khoản đã được cập nhật
        Account lastSavedAccount = accounts.getFirst();
        assertEquals(account.getAccountId(), lastSavedAccount.getAccountId());
        assertEquals("newuser@example.com", lastSavedAccount.getEmail());
        assertEquals("Nguyen Van A", lastSavedAccount.getFullname());
        assertEquals(1, lastSavedAccount.getGender());
        assertEquals("0912345678", lastSavedAccount.getPhone());
        assertEquals("123 Đường ABC", lastSavedAccount.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), lastSavedAccount.getDob());
        assertEquals(account.getRole().getRoleId(), lastSavedAccount.getRole().getRoleId());
        assertEquals("https://example.com/avatar.jpg", lastSavedAccount.getAvatar());
    }

    @Test
    @DisplayName("AS_updateProfile_03")
    void updateProfile_shouldThrowException_whenAvatarNotExistAndAvatarDTOInvalidAndAvatarFileInvalidType() {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        AccountDTO accountDTO = AccountDTO.builder()
                .fullname(account.getFullname())
                .gender(account.getGender())
                .phone(account.getPhone())
                .address(account.getAddress())
                .avatar(null)
                .dob(account.getDob())
                .build();

        MultipartFile avatarFile = new MockMultipartFile("avatar", "not-image-type", "not-image-type", "avatar".getBytes());

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.updateProfile(account.getEmail(), accountDTO, avatarFile);
        });
        assertEquals(ErrorCode.HTTP_FILE_IS_NOT_IMAGE, thrown.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(account.getEmail()).stream().toList();
        assertNotNull(accounts);
        assertEquals(1, accounts.size());

        // Kiểm tra thông tin tài khoản không bị thay đổi
        Account lastSavedAccount = accounts.getFirst();
        assertEquals(account.getAccountId(), lastSavedAccount.getAccountId());
        assertEquals("newuser@example.com", lastSavedAccount.getEmail());
        assertEquals("Nguyen Van A", lastSavedAccount.getFullname());
        assertEquals(1, lastSavedAccount.getGender());
        assertEquals("0912345678", lastSavedAccount.getPhone());
        assertEquals("123 Đường ABC", lastSavedAccount.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), lastSavedAccount.getDob());
        assertEquals(account.getRole().getRoleId(), lastSavedAccount.getRole().getRoleId());
        assertNull(lastSavedAccount.getAvatar());
    }

    @Test
    @DisplayName("AS_updateProfile_04")
    void updateProfile_shouldThrowException_whenAvatarNotExistAndAvatarDTOInvalidAndAvatarFileInvalidSize() {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        AccountDTO accountDTO = AccountDTO.builder()
                .fullname(account.getFullname())
                .gender(account.getGender())
                .phone(account.getPhone())
                .address(account.getAddress())
                .avatar(null)
                .dob(account.getDob())
                .build();

        // >2 MB
        MockMultipartFile avatarFile = new MockMultipartFile("avatar", "image.png", "image/png", new byte[3 * 1024 * 1024]);

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.updateProfile(account.getEmail(), accountDTO, avatarFile);
        });
        assertEquals(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT, thrown.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(account.getEmail()).stream().toList();
        assertNotNull(accounts);
        assertEquals(1, accounts.size());

        // Kiểm tra thông tin tài khoản không bị thay đổi
        Account lastSavedAccount = accounts.getFirst();
        assertEquals(account.getAccountId(), lastSavedAccount.getAccountId());
        assertEquals("newuser@example.com", lastSavedAccount.getEmail());
        assertEquals("Nguyen Van A", lastSavedAccount.getFullname());
        assertEquals(1, lastSavedAccount.getGender());
        assertEquals("0912345678", lastSavedAccount.getPhone());
        assertEquals("123 Đường ABC", lastSavedAccount.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), lastSavedAccount.getDob());
        assertEquals(account.getRole().getRoleId(), lastSavedAccount.getRole().getRoleId());
        assertNull(lastSavedAccount.getAvatar());
    }

    @Test
    @DisplayName("AS_updateProfile_05")
    void updateProfile_shouldThrowException_whenAvatarNotExistAndAvatarDTOInvalidAndAvatarFileValidAndUploadFileFail() throws IOException {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        AccountDTO accountDTO = AccountDTO.builder()
                .fullname(account.getFullname())
                .gender(account.getGender())
                .phone(account.getPhone())
                .address(account.getAddress())
                .avatar(null)
                .dob(account.getDob())
                .build();

        MultipartFile avatarFile = new MockMultipartFile("avatar", "image.png", "image/png", "avatar".getBytes());

        when(firebaseService.uploadOneFile(any(), any(), any())).thenThrow(IOException.class);

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.updateProfile(account.getEmail(), accountDTO, avatarFile);
        });
        assertEquals(ErrorCode.UPLOAD_FAILED, thrown.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(account.getEmail()).stream().toList();
        assertNotNull(accounts);
        assertEquals(1, accounts.size());

        // Kiểm tra thông tin tài khoản không bị thay đổi
        Account lastSavedAccount = accounts.getFirst();
        assertEquals(account.getAccountId(), lastSavedAccount.getAccountId());
        assertEquals("newuser@example.com", lastSavedAccount.getEmail());
        assertEquals("Nguyen Van A", lastSavedAccount.getFullname());
        assertEquals(1, lastSavedAccount.getGender());
        assertEquals("0912345678", lastSavedAccount.getPhone());
        assertEquals("123 Đường ABC", lastSavedAccount.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), lastSavedAccount.getDob());
        assertEquals(account.getRole().getRoleId(), lastSavedAccount.getRole().getRoleId());
        assertNull(lastSavedAccount.getAvatar());
    }

    @Test
    @DisplayName("AS_updateProfile_06")
    void updateProfile_shouldUpdateProfileAndReturnUpdatedDTO_whenAvatarNotExistAndAvatarDTOInvalidAndAvatarFileValidAndUploadFileSuccess() throws IOException {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        AccountDTO accountDTO = AccountDTO.builder()
                .fullname(account.getFullname())
                .gender(account.getGender())
                .phone(account.getPhone())
                .address(account.getAddress())
                .avatar(null)
                .dob(account.getDob())
                .build();

        MultipartFile avatarFile = new MockMultipartFile("avatar", "image.png", "image/png", "avatar".getBytes());

        String uploadedUrl = "https://example.com/uploaded-avatar.png";
        when(firebaseService.uploadOneFile(any(), any(), any())).thenReturn(uploadedUrl);

        AccountDTO updatedAccountDTO = accountService.updateProfile(account.getEmail(), accountDTO, avatarFile);

        // Kiểm tra kết quả đúng
        assertEquals(account.getEmail(), updatedAccountDTO.getEmail());
        assertEquals(account.getFullname(), updatedAccountDTO.getFullname());
        assertEquals(account.getGender(), updatedAccountDTO.getGender());
        assertEquals(account.getPhone(), updatedAccountDTO.getPhone());
        assertEquals(account.getAddress(), updatedAccountDTO.getAddress());
        assertEquals(uploadedUrl, updatedAccountDTO.getAvatar());
        assertEquals(account.getDob(), updatedAccountDTO.getDob());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(account.getEmail()).stream().toList();
        assertNotNull(accounts);
        assertEquals(1, accounts.size());

        // Kiểm tra thông tin tài khoản đã được cập nhật
        Account lastSavedAccount = accounts.getFirst();
        assertEquals(account.getAccountId(), lastSavedAccount.getAccountId());
        assertEquals("newuser@example.com", lastSavedAccount.getEmail());
        assertEquals("Nguyen Van A", lastSavedAccount.getFullname());
        assertEquals(1, lastSavedAccount.getGender());
        assertEquals("0912345678", lastSavedAccount.getPhone());
        assertEquals("123 Đường ABC", lastSavedAccount.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), lastSavedAccount.getDob());
        assertEquals(account.getRole().getRoleId(), lastSavedAccount.getRole().getRoleId());
        assertEquals(uploadedUrl, lastSavedAccount.getAvatar());
    }

    @Test
    @DisplayName("AS_updateProfile_07")
    void updateProfile_shouldUpdateProfileAndReturnUpdatedDTO_whenAvatarExistAndAvatarDTOInvalidAndAvatarFileIsNullAndDeleteAvatarFail() throws IOException {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        account.setAvatar("https://example.com/avatar.jpg");
        accountRepository.save(account);

        AccountDTO accountDTO = AccountDTO.builder()
                .fullname(account.getFullname())
                .gender(account.getGender())
                .phone(account.getPhone())
                .address(account.getAddress())
                .avatar(null)
                .dob(account.getDob())
                .build();

        MultipartFile avatarFile = null;

        when(firebaseService.deleteFileByPath(any())).thenThrow(IOException.class);

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.updateProfile(account.getEmail(), accountDTO, avatarFile);
        });

        assertEquals(ErrorCode.DELETE_FILE_FAILED, thrown.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(account.getEmail()).stream().toList();
        assertNotNull(accounts);
        assertEquals(1, accounts.size());

        // Kiểm tra thông tin tài khoản không bị thay đổi
        Account lastSavedAccount = accounts.getFirst();
        assertEquals(account.getAccountId(), lastSavedAccount.getAccountId());
        assertEquals("newuser@example.com", lastSavedAccount.getEmail());
        assertEquals("Nguyen Van A", lastSavedAccount.getFullname());
        assertEquals(1, lastSavedAccount.getGender());
        assertEquals("0912345678", lastSavedAccount.getPhone());
        assertEquals("123 Đường ABC", lastSavedAccount.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), lastSavedAccount.getDob());
        assertEquals(account.getRole().getRoleId(), lastSavedAccount.getRole().getRoleId());
        assertEquals("https://example.com/avatar.jpg", lastSavedAccount.getAvatar());
    }

    @Test
    @DisplayName("AS_updateProfile_08")
    void updateProfile_shouldUpdateProfileAndReturnUpdatedDTO_whenAvatarExistAndAvatarDTOInvalidAndAvatarFileIsNullAndDeleteAvatarSuccess() throws IOException {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        account.setAvatar("https://example.com/avatar.jpg");
        accountRepository.save(account);

        AccountDTO accountDTO = AccountDTO.builder()
                .fullname(account.getFullname())
                .gender(account.getGender())
                .phone(account.getPhone())
                .address(account.getAddress())
                .avatar(null)
                .dob(account.getDob())
                .build();

        MultipartFile avatarFile = null;

        when(firebaseService.deleteFileByPath(any())).thenReturn(true);

        AccountDTO updatedAccountDTO = accountService.updateProfile(account.getEmail(), accountDTO, avatarFile);

        // Kiểm tra kết quả đúng
        assertEquals(account.getEmail(), updatedAccountDTO.getEmail());
        assertEquals(account.getFullname(), updatedAccountDTO.getFullname());
        assertEquals(account.getGender(), updatedAccountDTO.getGender());
        assertEquals(account.getPhone(), updatedAccountDTO.getPhone());
        assertEquals(account.getAddress(), updatedAccountDTO.getAddress());
        assertNull(updatedAccountDTO.getAvatar());
        assertEquals(account.getDob(), updatedAccountDTO.getDob());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(account.getEmail()).stream().toList();
        assertNotNull(accounts);
        assertEquals(1, accounts.size());

        // Kiểm tra thông tin tài khoản đã được cập nhật
        Account lastSavedAccount = accounts.getFirst();
        assertEquals(account.getAccountId(), lastSavedAccount.getAccountId());
        assertEquals("newuser@example.com", lastSavedAccount.getEmail());
        assertEquals("Nguyen Van A", lastSavedAccount.getFullname());
        assertEquals(1, lastSavedAccount.getGender());
        assertEquals("0912345678", lastSavedAccount.getPhone());
        assertEquals("123 Đường ABC", lastSavedAccount.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), lastSavedAccount.getDob());
        assertEquals(account.getRole().getRoleId(), lastSavedAccount.getRole().getRoleId());
        assertNull(lastSavedAccount.getAvatar());
    }

    @Test
    @DisplayName("AS_register_01")
    void register_shouldThrowException_whenOTPIsNull() {
        AccountRegisterDTO accountRegisterDTO = AccountRegisterDTO.builder()
                .email("c@example.com")
                .password("123456")
                .build();

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.register(accountRegisterDTO);
        });

        assertEquals(ErrorCode.HTTP_OTP_INVALID, thrown.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(accountRegisterDTO.getEmail()).stream().toList();
        assertNotNull(accounts);
        assertEquals(0, accounts.size());
    }

    @Test
    @DisplayName("AS_register_02")
    void register_shouldThrowException_whenOTPDoesNotMatch() {
        otpUtils.add("c@example.com", "654321");
        AccountRegisterDTO accountRegisterDTO = AccountRegisterDTO.builder()
                .email("c@example.com")
                .password("123456")
                .otp("123456")
                .build();

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.register(accountRegisterDTO);
        });
        assertEquals(ErrorCode.HTTP_OTP_INVALID, thrown.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(accountRegisterDTO.getEmail()).stream().toList();
        assertNotNull(accounts);
        assertEquals(0, accounts.size());
    }

    @Test
    @DisplayName("AS_register_03")
    void register_shouldThrowException_whenSystemUserRoleNotFound() {
        roleRepository.deleteAll();
        otpUtils.add("c@example.com", "123456");
        AccountRegisterDTO accountRegisterDTO = AccountRegisterDTO.builder()
                .email("c@example.com")
                .password("123456")
                .otp("123456")
                .build();

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.register(accountRegisterDTO);
        });
        assertEquals(ErrorCode.ROLE_NOT_EXISTED, thrown.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(accountRegisterDTO.getEmail()).stream().toList();
        assertNotNull(accounts);
        assertEquals(0, accounts.size());
    }

    @Test
    @DisplayName("AS_register_04")
    void register_shouldReturnAccount_whenRegisterSuccessfully() {
        otpUtils.add("c@example.com", "123456");
        AccountRegisterDTO accountRegisterDTO = AccountRegisterDTO.builder()
                .email("c@example.com")
                .password("123456")
                .otp("123456")
                .build();

        // Kiểm tra kết quả đúng
        ApiResponse<?> response = accountService.register(accountRegisterDTO);
        assertNotNull(response);
        assertEquals("Register successfully!", response.getMessage());
        assertEquals("200", response.getCode());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(accountRegisterDTO.getEmail()).stream().toList();
        assertNotNull(accounts);
        assertEquals(1, accounts.size());
        Account lastSavedAccount = accounts.getFirst();
        assertEquals(accountRegisterDTO.getEmail(), lastSavedAccount.getEmail());
        assertEquals(accountRegisterDTO.getPassword(), lastSavedAccount.getPassword());
    }

    @Test
    @DisplayName("AS_sendOtp_01")
    void sendOtp_shouldThrowException_whenEmailExist() {
        Email email = Email.builder()
                .email("newuser@example.com")
                .build();

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.sendOtp(email);
        });
        assertEquals(ErrorCode.EXIST_EMAIL, thrown.getErrorCode());
        verify(emailService, never()).sendEmail(any());
    }

    @Test
    @DisplayName("AS_sendOtp_02")
    void sendOtp_shouldSendEmailSuccessfullyWhenEmailNotExist() {
        Email email = Email.builder()
                .email("c@example.com")
                .build();

        when(emailService.sendEmail(any(Email.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        ApiResponse<?> response = accountService.sendOtp(email);

        // Kiểm tra kết quả đúng
        assertNotNull(response);
        assertEquals("Successfully!", response.getMessage());
        assertEquals("200", response.getCode());

        // Kiểm tra email
        verify(emailService, times(1)).sendEmail(any());
        String otp = otpUtils.get(email.getEmail());
        assertNotNull(otp);
        assertEquals(otp, otpUtils.get(email.getEmail()));
    }

    @Test
    @DisplayName("AS_forgot_01")
    void forgot_shouldThrowException_whenEmailNotExist() {
        AccountForgotPasswordDTO request = AccountForgotPasswordDTO.builder()
                .email("c@example.com")
                .build();

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.forgot(request);
        });
        assertEquals(ErrorCode.USER_NOT_EXISTED, thrown.getErrorCode());
        verify(emailService, never()).sendEmail(any());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(request.getEmail()).stream().toList();
        assertNotNull(accounts);
        assertEquals(0, accounts.size());
    }

    @Test
    @DisplayName("AS_forgot_02")
    void forgot_shouldSendEmailSuccessfully_whenEmailExist() throws MessagingException {
        AccountForgotPasswordDTO request = AccountForgotPasswordDTO.builder()
                .email("newuser@example.com")
                .build();

        when(emailService.sendEmail(any(Email.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        ApiResponse<?> response = accountService.forgot(request);

        // Kiểm tra kết quả đúng
        assertNotNull(response);
        assertEquals(ErrorCode.HTTP_OK.getMessage(), response.getMessage());
        assertEquals(ErrorCode.HTTP_OK.getCode(), response.getCode());
        verify(emailService, times(1)).sendEmail(any());

        // Kiểm tra dữ liệu trong DB
        List<Account> accounts = accountRepository.findByEmail(request.getEmail()).stream().toList();
        assertNotNull(accounts);
        assertEquals(1, accounts.size());

        // Kiểm tra thông tin tài khoản đã được lưu
        Account lastSavedAccount = accounts.getFirst();
        assertEquals(request.getEmail(), lastSavedAccount.getEmail());
        assertEquals("Nguyen Van A", lastSavedAccount.getFullname());
        assertEquals(1, lastSavedAccount.getGender());
        assertEquals("0912345678", lastSavedAccount.getPhone());
        assertEquals("123 Đường ABC", lastSavedAccount.getAddress());
        assertEquals(LocalDate.of(2003, 12, 31), lastSavedAccount.getDob());
    }

    @Test
    @DisplayName("AS_findAccountByEmail_01")
    void findAccountByEmail_shouldThrowException_whenEmailNotExist() {
        String email = "c@example.com";

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            accountService.findAccountByEmail(email);
        });
        assertEquals(ErrorCode.USER_NOT_EXISTED, thrown.getErrorCode());
    }

    @Test
    @DisplayName("AS_findAccountByEmail_02")
    void findAccountByEmail_shouldReturnAccount_whenEmailExist() {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        AccountDTO accountDTO = accountService.findAccountByEmail(account.getEmail());

        // Kiểm tra kết quả trả về đúng
        assertNotNull(accountDTO);
        assertEquals(account.getEmail(), accountDTO.getEmail());
        assertEquals(account.getFullname(), accountDTO.getFullname());
        assertEquals(account.getGender(), accountDTO.getGender());
        assertEquals(account.getPhone(), accountDTO.getPhone());
        assertEquals(account.getAddress(), accountDTO.getAddress());
        assertEquals(account.getDob(), accountDTO.getDob());
        assertEquals(account.getAvatar(), accountDTO.getAvatar());
        assertEquals(account.getReferCode(), accountDTO.getReferCode());
        assertEquals(account.getCode(), accountDTO.getCode());
    }

    @Test
    @DisplayName("AS_getTopAmbassador_01")
    void getTopAmbassador_shouldReturnEmptyList_whenNoAccount() {
        entityManager.createNativeQuery("DELETE FROM account")
                .executeUpdate();
        Integer limit = 1;

        List<TopAmbassadorResponseDTO> result = accountService.getTopAmbassador(limit);

        // Kiểm tra kết quả trả về đúng
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("AS_getTopAmbassador_02")
    void getTopAmbassador_shouldReturnList_whenHaveAccountAndDonation() {
        Integer limit = 1;
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        entityManager.createNativeQuery(
                        "INSERT INTO donation (value, created_at, created_by) VALUES " +
                                "(1000.00, '2024-06-05 11:00:00', ?)")
                .setParameter(1, account.getAccountId())
                .executeUpdate();

        entityManager.createNativeQuery(
                        "INSERT INTO donation (value, created_at, created_by, refer_id) VALUES " +
                                "(500.00, '2024-06-05 12:00:00', ?, ?)")
                .setParameter(1, account.getAccountId())
                .setParameter(2, account.getAccountId())
                .executeUpdate();

        entityManager.createNativeQuery(
                        "INSERT INTO challenge (challenge_code, title, slug, content, goal, created_by, created_at) VALUES " +
                                "('TT001', 'Test Challenge', 'test-challenge', 'Test content', 1000.00, ?, '2024-06-05 13:00:00')")
                .setParameter(1, account.getAccountId())
                .executeUpdate();

        Long challengeIdLong = (Long) entityManager.createNativeQuery(
                        "SELECT challenge_id FROM challenge WHERE challenge_code = 'TT001'")
                .getSingleResult();

        BigInteger challengeId = BigInteger.valueOf(challengeIdLong);

        entityManager.createNativeQuery(
                        "INSERT INTO donation (value, created_at, created_by, challenge_id) VALUES " +
                                "(300.00, '2024-06-05 13:00:00', ?, ?)")
                .setParameter(1, account.getAccountId())
                .setParameter(2, challengeId)
                .executeUpdate();

        List<TopAmbassadorResponseDTO> result = accountService.getTopAmbassador(limit);

        // Kiểm tra kết quả trả về đúng
        assertNotNull(result);
        assertEquals(1, result.size());

        // Kiểm tra thông tin tài khoản
        TopAmbassadorResponseDTO topAmbassador = result.getFirst();
        assertEquals(account.getAccountId(), topAmbassador.getAccountId());
        assertEquals(account.getFullname(), topAmbassador.getFullname());
        assertEquals(account.getCode(), topAmbassador.getCode());
        assertEquals(account.getAvatar(), topAmbassador.getAvatar());
        assertEquals(2, topAmbassador.getCountDonations());
        assertThat(topAmbassador.getTotalDonation()).isEqualByComparingTo(BigDecimal.valueOf(800));
    }

    @Test
    @DisplayName("AS_getTopDonors_01")
    void getTopDonors_shouldReturnEmptyList_whenNoAccount() {
        entityManager.createNativeQuery("DELETE FROM account")
                .executeUpdate();
        Integer limit = 1;

        List<TopAmbassadorResponseDTO> result = accountService.getTopDonors(limit);

        // Kiểm tra kết quả trả về đúng
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("AS_getTopDonors_02")
    void getTopDonors_shouldReturnList_whenHaveAccountAndDonation() {
        Integer limit = 1;
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        entityManager.createNativeQuery(
                        "INSERT INTO donation (value, created_at, created_by) VALUES " +
                                "(1000.00, '2024-06-05 11:00:00', ?)")
                .setParameter(1, account.getAccountId())
                .executeUpdate();

        entityManager.createNativeQuery(
                        "INSERT INTO donation (value, created_at, created_by, refer_id) VALUES " +
                                "(500.00, '2024-06-05 12:00:00', ?, ?)")
                .setParameter(1, account.getAccountId())
                .setParameter(2, account.getAccountId())
                .executeUpdate();

        entityManager.createNativeQuery(
                        "INSERT INTO challenge (challenge_code, title, slug, content, goal, created_by, created_at) VALUES " +
                                "('TT001', 'Test Challenge', 'test-challenge', 'Test content', 1000.00, ?, '2024-06-05 13:00:00')")
                .setParameter(1, account.getAccountId())
                .executeUpdate();

        Long challengeIdLong = (Long) entityManager.createNativeQuery(
                        "SELECT challenge_id FROM challenge WHERE challenge_code = 'TT001'")
                .getSingleResult();

        BigInteger challengeId = BigInteger.valueOf(challengeIdLong);

        entityManager.createNativeQuery(
                        "INSERT INTO donation (value, created_at, created_by, challenge_id) VALUES " +
                                "(300.00, '2024-06-05 13:00:00', ?, ?)")
                .setParameter(1, account.getAccountId())
                .setParameter(2, challengeId)
                .executeUpdate();

        List<TopAmbassadorResponseDTO> result = accountService.getTopDonors(limit);

        // Kiểm tra kết quả trả về đúng
        assertNotNull(result);
        assertEquals(1, result.size());

        // Kiểm tra thông tin tài khoản
        TopAmbassadorResponseDTO topDonor = result.getFirst();
        assertEquals(account.getAccountId(), topDonor.getAccountId());
        assertEquals(account.getFullname(), topDonor.getFullname());
        assertEquals(account.getCode(), topDonor.getCode());
        assertEquals(account.getAvatar(), topDonor.getAvatar());
        assertEquals(3, topDonor.getCountDonations());
        assertThat(topDonor.getTotalDonation()).isEqualByComparingTo(BigDecimal.valueOf(1800));
    }

    @Test
    @DisplayName("AS_getAmbassadors_01")
    void getAmbassadors_shouldReturnEmptyList_whenNoAccount() {
        entityManager.createNativeQuery("DELETE FROM account")
                .executeUpdate();
        Integer page = 0;
        Integer size = 10;

        PageResponse<AmbassadorResponseDTO> result = (PageResponse<AmbassadorResponseDTO>) accountService.getAmbassadors(page, size, null, null, null, null, null, null);

        // Kiểm tra kết quả trả về đúng
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(10, result.getLimit());
        assertEquals(0, result.getOffset());
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("AS_getAmbassadors_02")
    void getAmbassadors_shouldReturnListOfAmbassadorResponseDTO_whenExistAccount() {
        Optional<Account> accountOpt = accountRepository.findByEmail("newuser@example.com");
        assertTrue(accountOpt.isPresent());
        Account account = accountOpt.get();

        Integer page = 0;
        Integer size = 10;
        String email = account.getEmail();

        entityManager.createNativeQuery(
                        "INSERT INTO donation (value, created_at, created_by) VALUES " +
                                "(1000.00, '2024-06-05 11:00:00', ?)")
                .setParameter(1, account.getAccountId())
                .executeUpdate();

        entityManager.createNativeQuery(
                        "INSERT INTO donation (value, created_at, created_by, refer_id) VALUES " +
                                "(500.00, '2024-06-05 12:00:00', ?, ?)")
                .setParameter(1, account.getAccountId())
                .setParameter(2, account.getAccountId())
                .executeUpdate();

        entityManager.createNativeQuery(
                        "INSERT INTO challenge (challenge_code, title, slug, content, goal, created_by, created_at) VALUES " +
                                "('TT001', 'Test Challenge', 'test-challenge', 'Test content', 1000.00, ?, '2024-06-05 13:00:00')")
                .setParameter(1, account.getAccountId())
                .executeUpdate();

        Long challengeIdLong = (Long) entityManager.createNativeQuery(
                        "SELECT challenge_id FROM challenge WHERE challenge_code = 'TT001'")
                .getSingleResult();

        BigInteger challengeId = BigInteger.valueOf(challengeIdLong);

        entityManager.createNativeQuery(
                        "INSERT INTO donation (value, created_at, created_by, challenge_id) VALUES " +
                                "(300.00, '2024-06-05 13:00:00', ?, ?)")
                .setParameter(1, account.getAccountId())
                .setParameter(2, challengeId)
                .executeUpdate();

        PageResponse<AmbassadorResponseDTO> result = (PageResponse<AmbassadorResponseDTO>) accountService.getAmbassadors(page, size, null, null, email, null, null, null);

        // Kiểm tra kết quả trả về đúng
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(10, result.getLimit());
        assertEquals(0, result.getOffset());
        assertEquals(1, result.getTotal());

        // Kiểm tra thông tin tài khoản
        AmbassadorResponseDTO ambassador = result.getContent().getFirst();
        assertEquals(account.getAccountId(), ambassador.getAccountId());
        assertEquals(account.getFullname(), ambassador.getFullname());
        assertEquals(account.getCode(), ambassador.getCode());
        assertEquals(account.getCreatedAt(), ambassador.getCreatedAt());
        assertEquals(account.getAvatar(), ambassador.getAvatar());
        assertThat(ambassador.getTotalDonation()).isEqualByComparingTo(BigDecimal.valueOf(800));
        assertEquals(2, ambassador.getCountChallenges());
    }
}
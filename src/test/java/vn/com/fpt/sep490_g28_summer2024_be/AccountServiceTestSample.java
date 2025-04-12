package vn.com.fpt.sep490_g28_summer2024_be;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.role.RoleDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Role;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.RoleRepository;
import vn.com.fpt.sep490_g28_summer2024_be.service.account.AccountService;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("dev")
@SpringBootTest
@Transactional
public class AccountServiceTestSample {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepareForEachTest() {
        // Dữ lieu cho tung test
        // Cu moi test chay 1 lan
    }

    @BeforeAll
    static void prepareAll() {
        // Chay 1 lan duy nhat
    }


    @Test
    @DisplayName("M03_LoginViewModel_request_01")
    void createAccount_shouldSucceed_whenEmailNotExistsAndRoleValid() {
        // Chuẩn bị dữ liệu
        List<Role> roles = roleRepository.findAll();
        Role testRole = roles.getFirst();
        String testRoleName = testRole.getRoleName();
        BigInteger testRoleId = testRole.getRoleId();

        AccountDTO request = AccountDTO.builder()
                .email("newuser@example.com")
                .password("123456")
                .fullname("Nguyen Van A")
                .gender(1)
                .phone("0912345678")
                .address("123 Đường ABC")
                .dob(LocalDate.of(2003, 12, 31))
                .role(RoleDTO.builder().roleId(testRoleId).build())
                .build();

        // Thực hiện chạy
        AccountDTO response = accountService.createAccount(request);

        // Kiểm tra kết quả trả về đúng
        assertNotNull(response.getAccountId());
        assertEquals("newuser@example.com", response.getEmail());
        assertEquals("Nguyen Van A", response.getFullname());
        assertEquals(1, response.getGender());
        assertNotNull(response.getRole());
        assertEquals(testRoleId, response.getRole().getRoleId());
        assertEquals(testRoleName, response.getRole().getRoleName());

        // Kiểm tra dữ liệu trong DB
        Optional<Account> savedOpt = accountRepository.findById(response.getAccountId());
        assertTrue(savedOpt.isPresent());

        Account saved = savedOpt.get();
        assertEquals("newuser@example.com", saved.getEmail());
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
    }

    @Test
    @DisplayName("M03_LoginViewModel_request_02")
    void createAccount_shouldSucceed_whenEmailNotExistsAndRoleValid2() {

    }
}

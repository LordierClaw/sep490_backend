package vn.com.fpt.sep490_g28_summer2024_be.unittest.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestHeader;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.authentication.AuthenticationRequest;
import vn.com.fpt.sep490_g28_summer2024_be.dto.authentication.IntrospectRequest;
import vn.com.fpt.sep490_g28_summer2024_be.dto.authentication.RefreshTokenRequest;
import vn.com.fpt.sep490_g28_summer2024_be.dto.authentication.UserResponse;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Role;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.RoleRepository;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.JwtTokenGenerator;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.JwtTokenUtils;
import vn.com.fpt.sep490_g28_summer2024_be.service.authentication.DefaultAuthenticationService;
import vn.com.fpt.sep490_g28_summer2024_be.service.authentication.DefaultLogoutService;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
public class AuthenticationServiceTest {

    @Autowired
    private DefaultAuthenticationService authenticationService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtTokenGenerator jwtTokenGenerator;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DefaultLogoutService logoutService;

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
                .role(roles.getFirst())
                .build();
        savedAccount = accountRepository.save(savedAccount);
    }

    @Test
    @DisplayName("AuS_authenticate_01")
    void authenticate_shouldThrowsAppException_WhenUserNotFound() {
        var request = AuthenticationRequest.builder()
                .email("notfound@example.com")
                .password("any")
                .build();
        AppException ex = assertThrows(AppException.class, () ->
                authenticationService.authenticate(request)
        );
        assertEquals(ErrorCode.USER_NOT_EXISTED, ex.getErrorCode());
    }

    @Test
    @DisplayName("AuS_authenticate_02")
    void authenticate_shouldThrowsAppException_WhenPasswordInvalid() {
        var request = AuthenticationRequest.builder()
                .email("newuser@example.com")
                .password("wrongpassword")
                .build();
        AppException ex = assertThrows(
                AppException.class,
                () -> authenticationService.authenticate(request)
        );
        assertEquals(ErrorCode.INVALID_PASSWORD, ex.getErrorCode());
    }

    @Test
    @DisplayName("AuS_authenticate_03")
    void authenticate_shouldThrowsAppException_WhenUserInactive() {
        // Set user inactive
        Account acc = accountRepository.findByEmail("newuser@example.com").get();
        acc.setIsActive(false);
        accountRepository.save(acc);
        var request = AuthenticationRequest.builder()
                .email("newuser@example.com")
                .password("123456")
                .build();
        AppException ex = assertThrows(
                AppException.class,
                () -> authenticationService.authenticate(request)
        );
        assertEquals(ErrorCode.HTTP_USER_NOT_ACTIVE, ex.getErrorCode());
        // Revert for other tests
        acc.setIsActive(true);
        accountRepository.save(acc);
    }

    @Test
    @DisplayName("AuS_authenticate_04")
    void authenticate_shouldReturnsAuthenticationResponse_WhenUserValid() throws Exception {
        var request = AuthenticationRequest.builder()
                .email("newuser@example.com")
                .password("123456")
                .build();
        var response = authenticationService.authenticate(request);
        assertTrue(response.getAuthenticated());
        assertEquals("ACC001", response.getCode());
        assertEquals("Nguyen Van A", response.getFullname());
        assertEquals("0912345678", response.getPhoneNumber());
        assertEquals("newuser@example.com", response.getEmail());
        assertNotNull(response.getToken());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getExpiryTime());
        assertNotNull(response.getRefreshExpiryTime());
        assertNotNull(response.getRole());
    }

    @Test
    @DisplayName("AuS_introspect_01")
    void introspect_shouldReturnsValidTrue_WhenTokenValid() throws Exception {
        // Arrange: Authenticate to get a valid token
        var request = AuthenticationRequest.builder()
                .email("newuser@example.com")
                .password("123456")
                .build();
        var authResponse = authenticationService.authenticate(request);
        var token = authResponse.getToken();

        var introspectRequest = IntrospectRequest.builder()
                .token(token)
                .build();

        // Act
        var response = authenticationService.introspect(introspectRequest);

        // Assert
        assertNotNull(response);
        assertEquals(Boolean.TRUE, response.getValid());
    }

    @Test
    @DisplayName("AuS_introspect_02")
    void introspect_invalidToken_throwsAppException() {
        var introspectRequest = IntrospectRequest.builder()
                .token("invalid.token.value")
                .build();
        Exception ex = assertThrows(Exception.class, () ->
                authenticationService.introspect(introspectRequest)
        );
    }

    @Test
    @DisplayName("AuS_introspect_03")
    void introspect_shouldThrowsAppExceptionWhenTokenExpired() throws Exception {
        // Manually create an expired token for the test account
        var account = accountRepository.findByEmail("newuser@example.com").get();
        var userDetails = new CustomAccountDetails(
                UserResponse.builder()
                        .email(account.getEmail())
                        .fullname(account.getFullname())
                        .avatar(account.getAvatar())
                        .password(account.getPassword())
                        .isActive(account.getIsActive())
                        .scope(account.getRole().getRoleName())
                        .build(),
                List.of(new SimpleGrantedAuthority(
                        "ROLE_" + account.getRole().getRoleName().toUpperCase().replace(" ", "_")))
        );
        // Use reflection to get JWT_SECRET
        Field field = jwtTokenUtils.getClass().getDeclaredField("JWT_SECRET");
        field.setAccessible(true);
        String jwtSecret = (String) field.get(jwtTokenUtils);
        // Create an expired token
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(userDetails.getUsername())
                .issuer("test-issuer")
                .issueTime(new Date(System.currentTimeMillis() - 100000L))
                .expirationTime(new Date(System.currentTimeMillis() - 50000L))
                .jwtID(UUID.randomUUID().toString())
                .claim("user", userDetails)
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        jwsObject.sign(new MACSigner(jwtSecret.getBytes()));
        String expiredToken = jwsObject.serialize();

        var introspectRequest = IntrospectRequest.builder()
                .token(expiredToken)
                .build();
        Exception ex = assertThrows(Exception.class, () ->
                authenticationService.introspect(introspectRequest)
        );
    }

    @Test
    @DisplayName("AuS_refreshToken_01 - valid refresh token returns AuthenticationResponse")
    void refreshToken_shouldReturnsAuthenticationResponse_WhenTokenValid() throws Exception {
        // Arrange: Authenticate to get refresh token
        var request = AuthenticationRequest.builder()
                .email("newuser@example.com")
                .password("123456")
                .build();
        var authResponse = authenticationService.authenticate(request);
        var refreshToken = authResponse.getRefreshToken();
        var httpServletRequest = new MockHttpServletRequest();
        var refreshRequest = new RefreshTokenRequest(refreshToken);

        // Act
        var response = authenticationService.refreshToken(refreshRequest, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals("newuser@example.com", response.getEmail());
        assertNotNull(response.getToken());
        assertNotNull(response.getRefreshToken());
        assertTrue(response.getAuthenticated());
        assertNotNull(response.getExpiryTime());
        assertNotNull(response.getRefreshExpiryTime());
    }

    @Test
    @DisplayName("AuS_refreshToken_02")
    void refreshToken_shouldThrowsAppExceptionWhenTokenInvalid() {
        var httpServletRequest = new MockHttpServletRequest();
        var refreshRequest = new RefreshTokenRequest("invalid.token.value");
        Exception ex = assertThrows(Exception.class, () ->
                authenticationService.refreshToken(refreshRequest, httpServletRequest)
        );
    }

    @Test
    @DisplayName("AuS_refreshToken_03 ")
    void refreshToken_shouldThrowsAppException_WhenTokenExpired() throws Exception {
        // Arrange: Create expired refresh token for the test account
        var account = accountRepository.findByEmail("newuser@example.com").get();
        var userDetails = new CustomAccountDetails(
                UserResponse.builder()
                        .email(account.getEmail())
                        .fullname(account.getFullname())
                        .avatar(account.getAvatar())
                        .password(account.getPassword())
                        .isActive(account.getIsActive())
                        .scope(account.getRole().getRoleName())
                        .build(),
                java.util.List.of(new SimpleGrantedAuthority(
                        "ROLE_" + account.getRole().getRoleName().toUpperCase().replace(" ", "_")))
        );
        // Use reflection to get JWT_REFRESH_SECRET
        java.lang.reflect.Field field = jwtTokenUtils.getClass().getDeclaredField("JWT_REFRESH_SECRET");
        field.setAccessible(true);
        String jwtSecret = (String) field.get(jwtTokenUtils);
        // Create an expired refresh token
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(userDetails.getUsername())
                .issuer("test-issuer")
                .issueTime(new java.util.Date(System.currentTimeMillis() - 100000L))
                .expirationTime(new java.util.Date(System.currentTimeMillis() - 50000L))
                .jwtID(java.util.UUID.randomUUID().toString())
                .claim("user", userDetails)
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        jwsObject.sign(new MACSigner(jwtSecret.getBytes()));
        String expiredRefreshToken = jwsObject.serialize();
        var httpServletRequest = new MockHttpServletRequest();
        var refreshRequest = new RefreshTokenRequest(expiredRefreshToken);
        Exception ex = assertThrows(Exception.class, () ->
                authenticationService.refreshToken(refreshRequest, httpServletRequest)
        );
    }

    @Test
    @DisplayName("AuS_logout_01")
    void logout_shouldDoNothing_whenTokenNotExistInHeader() {
        // Arrange
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        // Act
        logoutService.logout(request, response, authentication);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("AuS_logout_02")
    void logout_shouldDoNothing_whenTokenNotStartWithBearer() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Invalid token");
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        // Act
        logoutService.logout(request, response, authentication);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("AuS_logout_03")
    void logout_shouldDoNothing_whenTokenInvalid() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token.value");
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        // Act
        logoutService.logout(request, response, authentication);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("AuS_logout_04")
    void logout_shouldClearContext_whenTokenValid() throws Exception {
        // Arrange
        // Create a valid JWT token
        var account = accountRepository.findByEmail("newuser@example.com").get();
        var userDetails = new CustomAccountDetails(
                UserResponse.builder()
                        .email(account.getEmail())
                        .fullname(account.getFullname())
                        .avatar(account.getAvatar())
                        .password(account.getPassword())
                        .isActive(account.getIsActive())
                        .scope(account.getRole().getRoleName())
                        .build(),
                List.of(new SimpleGrantedAuthority(
                        "ROLE_" + account.getRole().getRoleName().toUpperCase().replace(" ", "_")))
        );

        // Use reflection to get JWT_SECRET
        Field field = jwtTokenUtils.getClass().getDeclaredField("JWT_SECRET");
        field.setAccessible(true);
        String jwtSecret = (String) field.get(jwtTokenUtils);

        // Create a valid token
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(userDetails.getUsername())
                .issuer("test-issuer")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 60000))
                .jwtID(UUID.randomUUID().toString())
                .claim("user", userDetails)
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        jwsObject.sign(new MACSigner(jwtSecret.getBytes()));
        String validToken = jwsObject.serialize();

        // Set up request with valid token
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + validToken);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        // Set up security context
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act
        logoutService.logout(request, response, authentication);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}


package vn.com.fpt.sep490_g28_summer2024_be;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.role.RoleDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Role;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.repository.RoleRepository;
import vn.com.fpt.sep490_g28_summer2024_be.service.role.DefaultRoleService;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class DefaultRoleServiceTest {

    @Autowired
    private DefaultRoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    private Role role;
    private final LocalDateTime testDateTime = LocalDateTime.of(2023, 7, 15, 10, 30, 0);

    @BeforeEach
    void setUp() {
        role = Role.builder()
                .roleName("Test Role")
                .roleDescription("Test Description")
                .isActive(true)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();
        role = roleRepository.save(role);
    }

    @Test
    @DisplayName("RS_create_01")
    void RS_create_01() {
        // Chuẩn bị dữ liệu test
        RoleDTO request = RoleDTO.builder()
                .roleName("Admin")
                .roleDescription("Quản trị viên hệ thống")
                .isActive(true)
                .build();

        // Thực thi và kiểm tra kết quả
        ApiResponse<?> response = roleService.create(request);

        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("successfully!", response.getHeader().get("message"));
        
        RoleDTO createdRole = (RoleDTO) response.getData();
        assertEquals("Admin", createdRole.getRoleName());
        assertEquals("Quản trị viên hệ thống", createdRole.getRoleDescription());
        assertTrue(createdRole.getIsActive());

        // Kiểm tra trong database
        Optional<Role> savedRole = roleRepository.findRoleByRoleName("Admin");
        assertTrue(savedRole.isPresent());
        assertEquals("Admin", savedRole.get().getRoleName());
    }

    @Test
    @DisplayName("RS_create_02")
    void RS_create_02() {
        // Tạo role với tên "Admin" trước
        Role existingRole = Role.builder()
                .roleName("Admin")
                .roleDescription("Quản trị viên khác")
                .isActive(true)
                .build();
        roleRepository.save(existingRole);

        // Tạo request với tên trùng
        RoleDTO request = RoleDTO.builder()
                .roleName("Admin")
                .roleDescription("Quản trị viên khác")
                .isActive(true)
                .build();

        // Kiểm tra ném exception khi tạo role trùng tên
        assertThrows(AppException.class, () -> roleService.create(request));
    }

    @Test
    @DisplayName("RS_create_03")
    void RS_create_03() {
        // Chuẩn bị dữ liệu test
        RoleDTO request = RoleDTO.builder()
                .roleName("Viewer")
                .roleDescription("Chỉ xem dữ liệu")
                .isActive(false)
                .build();

        // Thực thi và kiểm tra kết quả
        ApiResponse<?> response = roleService.create(request);
        
        assertNotNull(response);
        assertEquals("200", response.getCode());
        
        RoleDTO createdRole = (RoleDTO) response.getData();
        assertEquals("Viewer", createdRole.getRoleName());
        assertEquals("Chỉ xem dữ liệu", createdRole.getRoleDescription());
        assertFalse(createdRole.getIsActive());
    }

    @Test
    @DisplayName("RS_toRole_01")
    void RS_toRole_01() {
        // Chuẩn bị dữ liệu test
        RoleDTO dto = RoleDTO.builder()
                .roleId(BigInteger.ONE)
                .roleName("Admin")
                .roleDescription("Quản trị viên")
                .isActive(true)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        // Thực thi và kiểm tra kết quả
        Role result = roleService.toRole(dto);

        assertNotNull(result);
        assertEquals(BigInteger.ONE, result.getRoleId());
        assertEquals("Admin", result.getRoleName());
        assertEquals("Quản trị viên", result.getRoleDescription());
        assertTrue(result.getIsActive());
        assertEquals(testDateTime, result.getCreatedAt());
        assertEquals(testDateTime, result.getUpdatedAt());
    }

    @Test
    @DisplayName("RS_toRole_02")
    void RS_toRole_02() {
        // Chuẩn bị dữ liệu test với các trường null
        RoleDTO dto = RoleDTO.builder()
                .roleName("User")
                .build();

        // Thực thi và kiểm tra kết quả
        Role result = roleService.toRole(dto);

        assertNotNull(result);
        assertEquals("User", result.getRoleName());
        assertNull(result.getRoleDescription());
        assertNull(result.getIsActive());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }

    @Test
    @DisplayName("RS_toRole_03")
    void RS_toRole_03() {
        assertThrows(NullPointerException.class, () -> roleService.toRole(null));
    }

    @Test
    @DisplayName("RS_toRoleDTO_01")
    void RS_toRoleDTO_01() {
        // Chuẩn bị dữ liệu test
        Role entity = Role.builder()
                .roleId(BigInteger.ONE)
                .roleName("Admin")
                .roleDescription("Quản trị viên")
                .isActive(true)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        // Thực thi và kiểm tra kết quả
        RoleDTO result = roleService.toRoleDTO(entity);

        assertNotNull(result);
        assertEquals(BigInteger.ONE, result.getRoleId());
        assertEquals("Admin", result.getRoleName());
        assertEquals("Quản trị viên", result.getRoleDescription());
        assertTrue(result.getIsActive());
        assertEquals(testDateTime, result.getCreatedAt());
        assertEquals(testDateTime, result.getUpdatedAt());
    }

    @Test
    @DisplayName("RS_toRoleDTO_02")
    void RS_toRoleDTO_02() {
        // Chuẩn bị dữ liệu test
        Role entity = Role.builder()
                .roleId(BigInteger.valueOf(2))
                .roleName("User")
                .isActive(true)
                .createdAt(testDateTime)
                .build();

        // Thực thi và kiểm tra kết quả
        RoleDTO result = roleService.toRoleDTO(entity);

        assertNotNull(result);
        assertEquals(BigInteger.valueOf(2), result.getRoleId());
        assertEquals("User", result.getRoleName());
        assertNull(result.getRoleDescription());
        assertTrue(result.getIsActive());
        assertEquals(testDateTime, result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }

    @Test
    @DisplayName("RS_toRoleDTO_03")
    void RS_toRoleDTO_03() {
        assertThrows(NullPointerException.class, () -> roleService.toRoleDTO(null));
    }

    @Test
    @DisplayName("RS_getAllRolesIdAndName_01")
    void RS_getAllRolesIdAndName_01() {
        // Thực thi và kiểm tra kết quả
        List<RoleDTO> result = roleService.getAllRolesIdAndName();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(dto -> 
            dto.getRoleName().equals("Test Role") && dto.getRoleId() != null
        ));
    }

    @Test
    @DisplayName("RS_getAllRolesIdAndName_02")
    void RS_getAllRolesIdAndName_02() {
        // Xóa hết dữ liệu trong bảng role
        roleRepository.deleteAll();

        // Thực thi và kiểm tra kết quả
        List<RoleDTO> result = roleService.getAllRolesIdAndName();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("RS_getAllRolesIdAndName_03")
    void RS_getAllRolesIdAndName_03() {
        assertDoesNotThrow(() -> roleService.getAllRolesIdAndName());
    }
} 
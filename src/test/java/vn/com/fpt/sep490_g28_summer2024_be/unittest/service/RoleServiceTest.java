package vn.com.fpt.sep490_g28_summer2024_be.unittest.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.role.RoleDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Role;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.repository.RoleRepository;
import vn.com.fpt.sep490_g28_summer2024_be.service.role.DefaultRoleService;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RoleServiceTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DefaultRoleService defaultRoleService;

    @Autowired
    private EntityManager entityManager;

    private Role role;
    private RoleDTO roleDTO;

    @BeforeEach
    void setUp() {
        // Clean up data before each test using EntityManager for better cleanup
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE role").executeUpdate();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

        // Create test data
        role = roleRepository.save(Role.builder()
                .roleName("TEST_ROLE")
                .roleDescription("Test Role Description")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build());

        roleDTO = RoleDTO.builder()
                .roleName("NEW_TEST_ROLE")
                .roleDescription("New Test Role Description")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("RS_create_01")
    void create_shouldCreateNewRole() {
        // Execute
        ApiResponse<?> response = defaultRoleService.create(roleDTO);

        // Verify response structure
        assertNotNull(response);
        assertEquals(ErrorCode.HTTP_OK.getCode(), response.getCode());
        assertTrue(response.getHeader().containsKey("message"));
        assertEquals("successfully!", response.getHeader().get("message"));

        // Verify created data
        RoleDTO createdRole = (RoleDTO) response.getData();
        assertNotNull(createdRole);
        assertEquals(roleDTO.getRoleName(), createdRole.getRoleName());
        assertEquals(roleDTO.getRoleDescription(), createdRole.getRoleDescription());
        assertEquals(roleDTO.getIsActive(), createdRole.getIsActive());

        // Verify database state
        Role savedRole = roleRepository.findById(createdRole.getRoleId()).orElse(null);
        assertNotNull(savedRole);
        assertEquals(roleDTO.getRoleName(), savedRole.getRoleName());
        assertEquals(roleDTO.getRoleDescription(), savedRole.getRoleDescription());
        assertEquals(roleDTO.getIsActive(), savedRole.getIsActive());
    }

    @Test
    @DisplayName("RS_create_02")
    void create_shouldHandleNullValues() {
        // Setup
        RoleDTO nullDTO = RoleDTO.builder().build();

        // Execute
        ApiResponse<?> response = defaultRoleService.create(nullDTO);

        // Verify response
        assertNotNull(response);
        assertEquals(ErrorCode.HTTP_OK.getCode(), response.getCode());

        // Verify created data
        RoleDTO createdRole = (RoleDTO) response.getData();
        assertNotNull(createdRole);
        assertNull(createdRole.getRoleName());
        assertNull(createdRole.getRoleDescription());

        // Verify database state
        Role savedRole = roleRepository.findById(createdRole.getRoleId()).orElse(null);
        assertNotNull(savedRole);
        assertNull(savedRole.getRoleName());
        assertNull(savedRole.getRoleDescription());
    }

    @Test
    @DisplayName("RS_create_03")
    void create_shouldHandleDuplicateRoleName() {
        // Setup - create role with same name
        RoleDTO duplicateDTO = RoleDTO.builder()
                .roleName("TEST_ROLE") // Same as existing role
                .roleDescription("Different description")
                .isActive(true)
                .build();

        // Execute & Verify
        AppException exception = assertThrows(AppException.class,
                () -> defaultRoleService.create(duplicateDTO));
        assertEquals(ErrorCode.ROLE_NOT_EXISTED, exception.getErrorCode());

        // Verify database state - no new role added
        assertEquals(1, roleRepository.count());
    }

    @Test
    @DisplayName("RS_create_04")
    void create_shouldHandleSpecialCharactersInRoleName() {
        // Setup
        RoleDTO specialCharDTO = RoleDTO.builder()
                .roleName("TEST@ROLE#123")
                .roleDescription("Role with special characters")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        // Execute
        ApiResponse<?> response = defaultRoleService.create(specialCharDTO);

        // Verify
        assertNotNull(response);
        assertEquals(ErrorCode.HTTP_OK.getCode(), response.getCode());
        RoleDTO createdRole = (RoleDTO) response.getData();
        assertEquals("TEST@ROLE#123", createdRole.getRoleName());
    }

    @Test
    @DisplayName("RS_create_05")
    void create_shouldHandleLongDescriptions() {
        // Setup
        String longDescription = "A".repeat(500); // Create a long description
        RoleDTO longDescDTO = RoleDTO.builder()
                .roleName("LONG_DESC_ROLE")
                .roleDescription(longDescription)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        // Execute
        ApiResponse<?> response = defaultRoleService.create(longDescDTO);

        // Verify
        assertNotNull(response);
        assertEquals(ErrorCode.HTTP_OK.getCode(), response.getCode());
        RoleDTO createdRole = (RoleDTO) response.getData();
        assertEquals(longDescription, createdRole.getRoleDescription());
    }

    @Test
    @DisplayName("RS_toRole_01")
    void toRole_shouldConvertDTOToEntity() {
        // Execute
        Role convertedRole = defaultRoleService.toRole(roleDTO);

        // Verify all fields are correctly mapped
        assertNotNull(convertedRole);
        assertEquals(roleDTO.getRoleId(), convertedRole.getRoleId());
        assertEquals(roleDTO.getRoleName(), convertedRole.getRoleName());
        assertEquals(roleDTO.getRoleDescription(), convertedRole.getRoleDescription());
        assertEquals(roleDTO.getIsActive(), convertedRole.getIsActive());
        assertEquals(roleDTO.getCreatedAt(), convertedRole.getCreatedAt());
        assertEquals(roleDTO.getUpdatedAt(), convertedRole.getUpdatedAt());
    }

    @Test
    @DisplayName("RS_toRole_02")
    void toRole_shouldHandleNullDTO() {
        // Execute & Verify
        assertThrows(NullPointerException.class,
                () -> defaultRoleService.toRole(null));
    }

    @Test
    @DisplayName("RS_toRole_03")
    void toRole_shouldHandleEmptyStrings() {
        // Setup
        RoleDTO emptyStringsDTO = RoleDTO.builder()
                .roleName("")
                .roleDescription("")
                .isActive(true)
                .build();

        // Execute
        Role convertedRole = defaultRoleService.toRole(emptyStringsDTO);

        // Verify
        assertNotNull(convertedRole);
        assertEquals("", convertedRole.getRoleName());
        assertEquals("", convertedRole.getRoleDescription());
    }

    @Test
    @DisplayName("RS_toRoleDTO_01")
    void toRoleDTO_shouldConvertEntityToDTO() {
        // Execute
        RoleDTO convertedDTO = defaultRoleService.toRoleDTO(role);

        // Verify all fields are correctly mapped
        assertNotNull(convertedDTO);
        assertEquals(role.getRoleId(), convertedDTO.getRoleId());
        assertEquals(role.getRoleName(), convertedDTO.getRoleName());
        assertEquals(role.getRoleDescription(), convertedDTO.getRoleDescription());
        assertEquals(role.getIsActive(), convertedDTO.getIsActive());
        assertEquals(role.getCreatedAt(), convertedDTO.getCreatedAt());
        assertEquals(role.getUpdatedAt(), convertedDTO.getUpdatedAt());
    }

    @Test
    @DisplayName("RS_toRoleDTO_02")
    void toRoleDTO_shouldHandleNullEntity() {
        // Execute & Verify
        assertThrows(NullPointerException.class,
                () -> defaultRoleService.toRoleDTO(null));
    }

    @Test
    @DisplayName("RS_toRoleDTO_03")
    void toRoleDTO_shouldHandleAllNullFields() {
        // Setup
        Role nullFieldsRole = Role.builder().build();

        // Execute
        RoleDTO convertedDTO = defaultRoleService.toRoleDTO(nullFieldsRole);

        // Verify
        assertNotNull(convertedDTO);
        assertNull(convertedDTO.getRoleId());
        assertNull(convertedDTO.getRoleName());
        assertNull(convertedDTO.getRoleDescription());
        assertNull(convertedDTO.getIsActive());
        assertNull(convertedDTO.getCreatedAt());
        assertNull(convertedDTO.getUpdatedAt());
    }

    @Test
    @DisplayName("RS_getAllRolesIdAndName_01")
    void getAllRolesIdAndName_shouldReturnAllRoles() {
        // Setup additional roles
        Role role2 = roleRepository.save(Role.builder()
                .roleName("TEST_ROLE_2")
                .roleDescription("Test Role Description 2")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build());

        // Execute
        List<RoleDTO> roles = defaultRoleService.getAllRolesIdAndName();

        // Verify list properties
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.stream().anyMatch(r -> r.getRoleName().equals("TEST_ROLE")));
        assertTrue(roles.stream().anyMatch(r -> r.getRoleName().equals("TEST_ROLE_2")));

        // Verify each role only contains id and name
        roles.forEach(roleDTO -> {
            assertNotNull(roleDTO.getRoleId());
            assertNotNull(roleDTO.getRoleName());
            assertNull(roleDTO.getRoleDescription());
            assertNull(roleDTO.getCreatedAt());
            assertNull(roleDTO.getUpdatedAt());
            assertNull(roleDTO.getIsActive());
        });
    }

    @Test
    @DisplayName("RS_getAllRolesIdAndName_02")
    void getAllRolesIdAndName_shouldHandleEmptyDatabase() {
        // Setup
        roleRepository.deleteAll();

        // Execute
        List<RoleDTO> roles = defaultRoleService.getAllRolesIdAndName();

        // Verify
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    @Test
    @DisplayName("RS_getAllRolesIdAndName_03")
    void getAllRolesIdAndName_shouldHandleInactiveRoles() {
        // Setup - create an inactive role
        Role inactiveRole = roleRepository.save(Role.builder()
                .roleName("INACTIVE_ROLE")
                .roleDescription("Inactive Role Description")
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .build());

        // Execute
        List<RoleDTO> roles = defaultRoleService.getAllRolesIdAndName();

        // Verify
        assertNotNull(roles);
        assertEquals(2, roles.size()); // Should include both active and inactive roles
        assertTrue(roles.stream().anyMatch(r -> r.getRoleName().equals("INACTIVE_ROLE")));
    }

    @Test
    @DisplayName("RS_getAllRolesIdAndName_04")
    void getAllRolesIdAndName_shouldHandleLargeNumberOfRoles() {
        // Setup - create 100 roles
        for (int i = 0; i < 100; i++) {
            roleRepository.save(Role.builder()
                    .roleName("TEST_ROLE_" + i)
                    .roleDescription("Description " + i)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        // Execute
        List<RoleDTO> roles = defaultRoleService.getAllRolesIdAndName();

        // Verify
        assertNotNull(roles);
        assertEquals(101, roles.size()); // 100 new + 1 from setUp
        assertTrue(roles.stream().allMatch(r -> r.getRoleName().startsWith("TEST_ROLE_") 
                || r.getRoleName().equals("TEST_ROLE")));
    }

    @Test
    @DisplayName("RS_findAll_01")
    void findAll_shouldReturnNull() {
        assertNull(defaultRoleService.findAll());
    }

    @Test
    @DisplayName("RS_findAll_02")
    void findAll_shouldReturnAllRolesWithFullDetails() {
        // Setup additional roles with different states
        Role activeRole = roleRepository.save(Role.builder()
                .roleName("ACTIVE_ROLE")
                .roleDescription("Active Role Description")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build());

        Role inactiveRole = roleRepository.save(Role.builder()
                .roleName("INACTIVE_ROLE")
                .roleDescription("Inactive Role Description")
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .build());

        // Execute
        ApiResponse<?> response = defaultRoleService.findAll();

        // Verify response structure
        assertNotNull(response);
        assertEquals(ErrorCode.HTTP_OK.getCode(), response.getCode());
        
        // Extract and cast the data
        @SuppressWarnings("unchecked")
        List<RoleDTO> roles = (List<RoleDTO>) response.getData();

        // Verify
        assertNotNull(roles);
        assertEquals(3, roles.size()); // Including the role from setUp

        // Verify full details are returned
        roles.forEach(roleDTO -> {
            assertNotNull(roleDTO.getRoleId());
            assertNotNull(roleDTO.getRoleName());
            assertNotNull(roleDTO.getRoleDescription());
            assertNotNull(roleDTO.getIsActive());
            assertNotNull(roleDTO.getCreatedAt());
        });

        // Verify both active and inactive roles are included
        assertTrue(roles.stream().anyMatch(r -> r.getRoleName().equals("ACTIVE_ROLE")));
        assertTrue(roles.stream().anyMatch(r -> r.getRoleName().equals("INACTIVE_ROLE")));
    }

    @Test
    @DisplayName("RS_update_01")
    void update_shouldReturnNull() {
        assertNull(defaultRoleService.update(BigInteger.ONE, roleDTO));
    }

    @Test
    @DisplayName("RS_update_02")
    void update_shouldUpdateExistingRole() {
        // Setup update data
        RoleDTO updateDTO = RoleDTO.builder()
                .roleName("UPDATED_ROLE")
                .roleDescription("Updated Description")
                .isActive(true)
                .build();

        // Execute
        ApiResponse<?> response = defaultRoleService.update(role.getRoleId(), updateDTO);

        // Verify response
        assertNotNull(response);
        assertEquals(ErrorCode.HTTP_OK.getCode(), response.getCode());
        
        // Verify updated data
        RoleDTO updatedRole = (RoleDTO) response.getData();
        assertEquals("UPDATED_ROLE", updatedRole.getRoleName());
        assertEquals("Updated Description", updatedRole.getRoleDescription());
        
        // Verify database state
        Role dbRole = roleRepository.findById(role.getRoleId()).orElse(null);
        assertNotNull(dbRole);
        assertEquals("UPDATED_ROLE", dbRole.getRoleName());
        assertEquals("Updated Description", dbRole.getRoleDescription());
    }

    @Test
    @DisplayName("RS_update_03")
    void update_shouldHandleNonExistentRole() {
        // Setup
        BigInteger nonExistentId = BigInteger.valueOf(999L);
        RoleDTO updateDTO = RoleDTO.builder()
                .roleName("NEW_ROLE")
                .roleDescription("New Description")
                .isActive(true)
                .build();

        // Execute & Verify
        AppException exception = assertThrows(AppException.class,
                () -> defaultRoleService.update(nonExistentId, updateDTO));
        assertEquals(ErrorCode.ROLE_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    @DisplayName("RS_deactive_01")
    void deactive_shouldReturnNull() {
        assertNull(defaultRoleService.deactive(BigInteger.ONE));
    }

    @Test
    @DisplayName("RS_deactive_02")
    void deactive_shouldDeactivateExistingRole() {
        // Execute
        ApiResponse<?> response = defaultRoleService.deactive(role.getRoleId());

        // Verify response
        assertNotNull(response);
        assertEquals(ErrorCode.HTTP_OK.getCode(), response.getCode());

        // Verify database state
        Role deactivatedRole = roleRepository.findById(role.getRoleId()).orElse(null);
        assertNotNull(deactivatedRole);
        assertFalse(deactivatedRole.getIsActive());
    }

    @Test
    @DisplayName("RS_deactive_03")
    void deactive_shouldHandleNonExistentRole() {
        // Setup
        BigInteger nonExistentId = BigInteger.valueOf(999L);

        // Execute & Verify
        AppException exception = assertThrows(AppException.class,
                () -> defaultRoleService.deactive(nonExistentId));
        assertEquals(ErrorCode.ROLE_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    @DisplayName("RS_deactive_04")
    void deactive_shouldHandleAlreadyInactiveRole() {
        // Setup - first deactivate the role
        defaultRoleService.deactive(role.getRoleId());

        // Execute - try to deactivate again
        ApiResponse<?> response = defaultRoleService.deactive(role.getRoleId());

        // Verify
        assertNotNull(response);
        assertEquals(ErrorCode.HTTP_OK.getCode(), response.getCode());
        
        // Verify role remains inactive
        Role deactivatedRole = roleRepository.findById(role.getRoleId()).orElse(null);
        assertNotNull(deactivatedRole);
        assertFalse(deactivatedRole.getIsActive());
    }
} 
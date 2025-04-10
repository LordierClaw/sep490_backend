package vn.com.fpt.sep490_g28_summer2024_be.service.role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.role.RoleDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Role;
import vn.com.fpt.sep490_g28_summer2024_be.repository.RoleRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultRoleService implements RoleService {

    private final RoleRepository repository;

    @Override
    public ApiResponse<?> create(RoleDTO request) {
        return ApiResponse.builder()
                .code(ErrorCode.HTTP_OK.getCode())
                .header(Map.of("message","successfully!"))
                .data(toRoleDTO(repository.save(toRole(request))))
                .build();
    }

    @Override
    public ApiResponse<?> findAll() {
        return null;
    }

    @Override
    public ApiResponse<?> update(BigInteger id, RoleDTO request) {
        return null;
    }

    @Override
    public ApiResponse<?> deactive(BigInteger id) {
        return null;
    }

    @Override
    public Role toRole(RoleDTO roleDTO){
        return Role.builder()
                .roleId(roleDTO.getRoleId())
                .roleName(roleDTO.getRoleName())
                .roleDescription(roleDTO.getRoleDescription())
                .createdAt(roleDTO.getCreatedAt())
                .updatedAt(roleDTO.getUpdatedAt())
                .isActive(roleDTO.getIsActive())
                .build();
    }

    @Override
    public RoleDTO toRoleDTO(Role role) {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(role, RoleDTO.class);
    }
    @Override
    public List<RoleDTO> getAllRolesIdAndName() {
        List<Role> roles = repository.findAll();
        return roles.stream()
                .map(role -> RoleDTO.builder()
                        .roleId(role.getRoleId())
                        .roleName(role.getRoleName())
                        .build())
                .collect(Collectors.toList());
    }
}

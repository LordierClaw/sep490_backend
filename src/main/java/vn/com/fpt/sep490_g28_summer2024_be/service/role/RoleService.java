package vn.com.fpt.sep490_g28_summer2024_be.service.role;

import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.role.RoleDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Role;

import java.math.BigInteger;
import java.util.List;

public interface RoleService {
    public ApiResponse<?> create(RoleDTO request);
    public ApiResponse<?> findAll();
    public ApiResponse<?> update(BigInteger id, RoleDTO request);
    public ApiResponse<?> deactive(BigInteger id);
    public Role toRole(RoleDTO roleDTO);
    public RoleDTO toRoleDTO(Role role);
    List<RoleDTO> getAllRolesIdAndName();
}

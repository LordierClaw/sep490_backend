package vn.com.fpt.sep490_g28_summer2024_be.web.rest.role;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.service.role.RoleService;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/admin/role")
@RequiredArgsConstructor
public class RoleRest {

    private final RoleService roleService;

    @GetMapping("/id-name")
    public ApiResponse<?> getAllCampaignsIdAndName() {
        return ApiResponse.builder()
                .code("200")
                .message("OK")
                .data(roleService.getAllRolesIdAndName())
                .build();
    }

}

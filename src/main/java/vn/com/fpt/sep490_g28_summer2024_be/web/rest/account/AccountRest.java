package vn.com.fpt.sep490_g28_summer2024_be.web.rest.account;


import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;


import java.math.BigDecimal;
import java.math.BigInteger;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import vn.com.fpt.sep490_g28_summer2024_be.service.account.AccountService;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
public class AccountRest {
    private final AccountService accountService;


    @GetMapping("")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ApiResponse<?> viewListAccounts(@RequestParam(defaultValue = "0") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) String email,
                                           @RequestParam(required = false) Boolean isActive,
                                           @RequestParam(required = false) BigInteger roleId) {
        return ApiResponse.builder()
                .code("200")
                .message("Danh sách tài khoản!")
                .data(accountService.viewByFilter(page, size, email, isActive, roleId))
                .build();
    }

    @GetMapping("/system-users")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> viewListSystemUserAccounts(@RequestParam(defaultValue = "0") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer size,
                                                     @RequestParam(required = false) String fullname,
                                                     @RequestParam(required = false) String email,
                                                     @RequestParam(required = false) String phone,
                                                     @RequestParam(required = false) BigDecimal minDonation,
                                                     @RequestParam(required = false) BigDecimal maxDonation) {
        return ApiResponse.builder()
                .code("200")
                .message("Danh sách tài khoản system user!")
                .data(accountService.viewSystemUserAccountsByFilter(page, size, fullname, email, phone, minDonation, maxDonation))
                .build();
    }

    @PostMapping("create")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ApiResponse<?> create(@RequestBody @Valid AccountDTO request) {
        return ApiResponse.builder()
                .code("200")
                .message("Tạo tài khoản thành công!")
                .data(accountService.createAccount(request))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ApiResponse<?> getAccountById(@PathVariable BigInteger id) {
        return ApiResponse.builder()
                .code("200")
                .message("OK")
                .data(accountService.getAccountById(id))
                .build();
    }

    @PutMapping("/deactivate/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ApiResponse<?> deactivateAccount(@PathVariable BigInteger id) {
        return ApiResponse.builder()
                .code("200")
                .message("Tài khoản đã bị vô hiệu hóa")
                .data(accountService.deactivateAccount(id))
                .build();
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ApiResponse<?> updateAccountByAdmin(@RequestBody AccountDTO accountDTO, @PathVariable BigInteger id) {
        return ApiResponse.builder()
                .code("200")
                .message("Cập nhật tài khoản thành công")
                .data(accountService.updateAccountByAdmin(accountDTO, id))
                .build();
    }

    @GetMapping("/news-creator")
    public ApiResponse<?> getAccountsByNewsCreatedBy() {
        return ApiResponse.builder()
                .code("200")
                .message("OK")
                .data(accountService.getAuthorNewsAccounts())
                .build();
    }

    @GetMapping("/dropdown/project-managers/id-email")
    public ApiResponse<?> getIdAndEmailProjectManagerAccounts() {
        return ApiResponse.builder()
                .code("200")
                .message("OK")
                .data(accountService.getIdAndEmailProjectManagerAccounts())
                .build();
    }
}
package vn.com.fpt.sep490_g28_summer2024_be.web.rest.profile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountChangePasswordDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.service.account.AccountService;
import vn.com.fpt.sep490_g28_summer2024_be.service.donation.DonationService;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileRest {
    private final AccountService accountService;
    private final DonationService donationService;

    @GetMapping
    public ApiResponse<?> viewProfile(@AuthenticationPrincipal CustomAccountDetails userDetails) {
        return ApiResponse.builder()
                .code(ErrorCode.HTTP_OK.getCode())
                .message(ErrorCode.HTTP_OK.getMessage())
                .data(accountService.findAccountByEmail(userDetails.getUsername()))
                .build();
    }

    @PutMapping("/change-password")
    public ApiResponse<AccountDTO> changePassword(@AuthenticationPrincipal CustomAccountDetails userDetails,
                                                  @RequestBody @Valid AccountChangePasswordDTO changePasswordRequest){
        return ApiResponse.<AccountDTO>builder()
                .message("Đổi mật khẩu thành công")
                .data(accountService.changePassword(userDetails.getUsername(), changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword()))
                .build();
    }

    @PutMapping(value = "/update-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AccountDTO> updateProfile(@AuthenticationPrincipal CustomAccountDetails userDetails,
                                                 @RequestPart("profile") AccountDTO accountDTO,
                                                 @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        AccountDTO updatedProfile = accountService.updateProfile(userDetails.getUsername(), accountDTO, avatar);
        return ApiResponse.<AccountDTO>builder()
                .code("200")
                .message("Cập nhật tài khoản thành công")
                .data(updatedProfile)
                .build();
    }

    @GetMapping("/history-donations")
    public ApiResponse<?> viewListDonationByAccount(@RequestParam(defaultValue = "0", required = false) Integer page,
                                                    @RequestParam(defaultValue = "10", required = false) Integer size,
                                                    @AuthenticationPrincipal CustomAccountDetails userDetails,
                                                    @RequestParam(required = false) String description
    ) {
        return ApiResponse.builder()
                .code("200")
                .message("Danh sách donate")
                .data(donationService.viewDonationsByAccount(page,size,userDetails.getUsername(),description))
                .build();
    }



}

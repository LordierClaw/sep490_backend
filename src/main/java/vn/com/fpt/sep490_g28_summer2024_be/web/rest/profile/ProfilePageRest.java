package vn.com.fpt.sep490_g28_summer2024_be.web.rest.profile;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountProfilePageDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.ChallengeResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.service.account.AccountService;
import vn.com.fpt.sep490_g28_summer2024_be.service.challenge.ChallengeService;
import vn.com.fpt.sep490_g28_summer2024_be.service.donation.DonationService;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/profile-page")
@RequiredArgsConstructor
public class ProfilePageRest {

    private final DonationService donationService;
    public final ChallengeService challengeService;
    public final AccountService accountService;


    @GetMapping("/challenges/active")
    public ApiResponse<?> viewActiveChallenges(@RequestParam(defaultValue = "0") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               @RequestParam(name = "account_code", required = false) String accountCode) {

        PageResponse<ChallengeResponseDTO> activeChallenges = challengeService.viewActiveChallengesByFilter(page, size, accountCode);

        return ApiResponse.builder()
                .code("200")
                .message("Active challenges retrieved successfully")
                .data(activeChallenges)
                .build();
    }


    @GetMapping("/challenges/expired")
    public ApiResponse<?> viewExpiredChallenges(@RequestParam(defaultValue = "0") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(name = "account_code", required = false) String accountCode) {

        PageResponse<ChallengeResponseDTO> expiredChallenges = challengeService.viewExpiredChallengesByFilter(page, size, accountCode);

        return ApiResponse.builder()
                .code("200")
                .message("Expired challenges retrieved successfully")
                .data(expiredChallenges)
                .build();
    }

    @GetMapping("/challenges/{id}")
    public ApiResponse<?> getChallengeById(@PathVariable BigInteger id) {
        ChallengeResponseDTO challengeResponseDTO = challengeService.getChallengeById(id);
        return ApiResponse.builder()
                .code("200")
                .message("Chi tiết challenge")
                .data(challengeResponseDTO)
                .build();
    }


    @GetMapping("/challenges/{id}/donations")
    public ApiResponse<?> viewListDonationByChallengeId(@RequestParam(defaultValue = "0", required = false) Integer page,
                                                        @RequestParam(defaultValue = "10", required = false) Integer size,
                                                        @RequestParam(required = false) String description,
                                                        @PathVariable BigInteger id
    ) {
        return ApiResponse.builder()
                .code("200")
                .message("Danh sách donate")
                .data(donationService.viewDonationsByChallengeId(page, size, id, description))
                .build();
    }


    @GetMapping("/total-count-donations")
    public ApiResponse<?> getTotalDonateAndCountDonateByAccountCode(@RequestParam(name = "account_code") String accountCode) {
        AccountProfilePageDTO accountProfilePageDTO = accountService.getTotalDonateAndCountDonateByAccountCode(accountCode);

        return ApiResponse.builder()
                .code("200")
                .message("OK")
                .data(accountProfilePageDTO)
                .build();
    }

    @GetMapping("/{code}/refer-donations")
    public ApiResponse<?> getAllDonationsByReferCode(@PathVariable("code") String referCode,
                                                     @RequestParam(defaultValue = "0") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer size) {
        return ApiResponse.builder()
                .code("200")
                .message("Successfully!")
                .data(donationService.viewDonationsByReferCode(page, size, referCode))
                .build();
    }


}

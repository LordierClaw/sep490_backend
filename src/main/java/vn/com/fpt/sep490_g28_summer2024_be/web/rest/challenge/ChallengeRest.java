package vn.com.fpt.sep490_g28_summer2024_be.web.rest.challenge;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.service.account.AccountService;
import vn.com.fpt.sep490_g28_summer2024_be.service.challenge.ChallengeService;

import java.math.BigDecimal;

@RestController
@RequestMapping(path = "/api/admin/challenges")
@RequiredArgsConstructor
public class ChallengeRest {

    public final ChallengeService challengeService;
    public final AccountService accountService;

    @GetMapping
    public ApiResponse<?> viewListChallenges(@RequestParam(defaultValue = "0") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String title,
                                             @RequestParam(required = false) Integer year,
                                             @RequestParam(required = false) BigDecimal minDonation,
                                             @RequestParam(required = false) BigDecimal maxDonation) {
        return ApiResponse.builder()
                .code("200")
                .message("Danh sách các challenge!")
                .data(challengeService.viewChallengesAdminByFilter(page, size, title, year, minDonation, maxDonation))
                .build();
    }
}

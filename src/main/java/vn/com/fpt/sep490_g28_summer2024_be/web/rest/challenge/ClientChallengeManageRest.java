package vn.com.fpt.sep490_g28_summer2024_be.web.rest.challenge;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.ChallengeRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.ChallengeResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.service.account.AccountService;
import vn.com.fpt.sep490_g28_summer2024_be.service.challenge.ChallengeService;


import java.math.BigInteger;

@RestController
@RequestMapping(path = "/api/manage/challenges")
@RequiredArgsConstructor
public class ClientChallengeManageRest {

    public final ChallengeService challengeService;
    public final AccountService accountService;

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_USER')")
    public ApiResponse<?> createChallenge(@RequestPart("challenge") @Valid ChallengeRequestDTO requestDTO,
                                          @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
                                          @AuthenticationPrincipal CustomAccountDetails userDetails) {

        ChallengeResponseDTO createdChallenge = challengeService.addChallenge(requestDTO, userDetails.getUsername(), thumbnail);
        return ApiResponse.builder()
                .code("200")
                .message("Tạo thành công challenge")
                .data(createdChallenge)
                .build();
    }


    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_USER')")
    public ApiResponse<?> updateChallenge(@RequestPart("challenge") ChallengeRequestDTO request,
                                          @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
                                          @PathVariable BigInteger id,
                                          @AuthenticationPrincipal CustomAccountDetails userDetails) {
        ChallengeResponseDTO updateChallenge = challengeService.updateChallenge(request, id, thumbnail,userDetails.getUsername());
        return ApiResponse.builder()
                .code("200")
                .message("Cập nhật thành công challenge!")
                .data(updateChallenge)
                .build();
    }

}

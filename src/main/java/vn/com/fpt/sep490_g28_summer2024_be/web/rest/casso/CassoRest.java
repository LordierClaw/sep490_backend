package vn.com.fpt.sep490_g28_summer2024_be.web.rest.casso;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.casso.WebhookResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.service.casso.CassoService;
import vn.com.fpt.sep490_g28_summer2024_be.service.wrongdonation.WrongDonationService;


@RestController
@RequestMapping("/api/casso")
@RequiredArgsConstructor
public class CassoRest {

    private final CassoService cassoService;
    private final WrongDonationService wrongDonationService;

    @PostMapping("/in")
    public ApiResponse<?> addInPayment(@RequestBody WebhookResponseDTO payload){
        return ApiResponse.builder()
                .data(cassoService.handleInPayment(payload.getData().get(0)))
                .build();
    }

    @PostMapping("/out")
    public ApiResponse<?> addOutPayment(@RequestBody WebhookResponseDTO payload){
        return ApiResponse.builder()
                .data(cassoService.handleOutPayment(payload.getData().get(0)))
                .build();
    }

    @GetMapping("/update-all-wrong-donations")
    public ApiResponse<?> updateWrongDonation(){
        wrongDonationService.updateWrongDonation().join();
        return ApiResponse.builder()
                .code("200")
                .message("Successfully!")
                .build();
    }
}

package vn.com.fpt.sep490_g28_summer2024_be.web.rest.donation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.service.donation.DonationService;


@RestController
@RequestMapping("/api/admin/donations")
@RequiredArgsConstructor
public class DonationRest {

    private final DonationService donationService;

    @GetMapping
    public ApiResponse<?> viewAllDonation(@RequestParam(defaultValue = "0", required = false) Integer page,
                                          @RequestParam(defaultValue = "10", required = false) Integer size,
                                          @RequestParam(required = false) String description){
        return ApiResponse.builder()
                .code("200")
                .message("Successfully!")
                .data(donationService.viewAllDonations(page, size, description))
                .build();
    }


}

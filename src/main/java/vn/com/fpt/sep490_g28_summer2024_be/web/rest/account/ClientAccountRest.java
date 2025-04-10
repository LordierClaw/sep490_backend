package vn.com.fpt.sep490_g28_summer2024_be.web.rest.account;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.service.account.AccountService;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/ambassadors")
@RequiredArgsConstructor
public class ClientAccountRest {

    private final AccountService accountService;

    @GetMapping("/top/{limit}")
    public ApiResponse<?> getTopAmbassadors(@PathVariable("limit") Integer limit){
        return ApiResponse.builder()
                .code("200")
                .message("Successfully!")
                .data(accountService.getTopAmbassador(limit))
                .build();
    }

    @GetMapping("/top-donors/{limit}")
    public ApiResponse<?> getTopDonor(@PathVariable("limit") Integer limit){
        return ApiResponse.builder()
                .code("200")
                .message("Successfully!")
                .data(accountService.getTopDonors(limit))
                .build();
    }

    @GetMapping("")
    public ApiResponse<?> getAmbassadors(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                         @RequestParam(value = "size", defaultValue = "10") Integer size,
                                        @RequestParam(value = "fullname", required = false) String fullname,
                                        @RequestParam(value = "code", required = false) String code,
                                        @RequestParam(value = "min", required = false) BigDecimal min,
                                        @RequestParam(value = "max", required = false) BigDecimal max){
        return ApiResponse.builder()
                .code("200")
                .message("Successfully")
                .data(accountService.getAmbassadors(page, size, fullname, code, null, null, min, max))
                .build();
    }

}



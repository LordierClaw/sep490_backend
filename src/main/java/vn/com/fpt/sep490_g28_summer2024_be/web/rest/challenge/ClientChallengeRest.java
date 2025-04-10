package vn.com.fpt.sep490_g28_summer2024_be.web.rest.challenge;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.service.challenge.ChallengeService;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class ClientChallengeRest {

    private final ChallengeService challengeService;

    @GetMapping("top/{number}")
    public ApiResponse<?> getTopChallenges(@PathVariable("number") Integer number ){
        return ApiResponse.builder()
                .code("200")
                .message("Successfully")
                .data(challengeService.getTopChallenges(number))
                .build();
    }

    @GetMapping("")
    public ApiResponse<?> getChallenges(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                        @RequestParam(value = "size", defaultValue = "10") Integer size,
                                        @RequestParam(value = "title", required = false) String title,
                                        @RequestParam(value = "fullname", required = false) String fullname,
                                        @RequestParam(value = "min", required = false) BigDecimal min,
                                        @RequestParam(value = "max", required = false) BigDecimal max){
        return ApiResponse.builder()
                .code("200")
                .message("Successfully")
                .data(challengeService.getChallenges(page, size, title, fullname, min, max))
                .build();
    }



}

package vn.com.fpt.sep490_g28_summer2024_be.web.rest.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.service.statistics.StatisticsService;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsRest {

    private final StatisticsService statisticsService;

    @GetMapping("/by-campaign-id/{id}")
    public ApiResponse<?> getByCampaignId(@PathVariable("id") BigInteger campaignId,
                                          @RequestParam(value = "year", required = false) Integer year){

        year = year == null ? LocalDateTime.now().getYear() : year;

        var dataDonation = statisticsService.getDonationStaticDataByCampaign(campaignId, year);
        var dataProject = statisticsService.getProjectStaticDataByCampaign(campaignId, year);
        return ApiResponse.builder()
                .code("200")
                .message("Successfully")
                .data(Map.of("tong-so-quyen-gop", dataDonation.getTotalDonation(),
                        "tong-so-luot-quyen-gop", dataDonation.getTotalNumberDonations(),
                        "tong-so-du-an", dataProject.getTotalProjects(),
                        "tong-so-du-an-can-quyen-gop", dataProject.getTotalOnGoingProjects(),
                        "tong-so-du-an-dang-thi-cong", dataProject.getTotalProcessingProjects(),
                        "tong-so-du-an-da-hoan-thanh", dataProject.getTotalDoneProjects()))
                .build();
    }

    @GetMapping("/common")
    public ApiResponse<?> getByCampaignId(@RequestParam(value = "year", required = false) Integer year){

        year = year == null ? LocalDateTime.now().getYear() : year;

        var dataDonation = statisticsService.getDonationStaticDataByCampaign(null, year);
        var dataProject = statisticsService.getProjectStaticDataByCampaign(null, year);
        return ApiResponse.builder()
                .code("200")
                .message("Successfully")
                .data(Map.of("tong-so-quyen-gop", dataDonation.getTotalDonation(),
                        "tong-so-luot-quyen-gop", dataDonation.getTotalNumberDonations(),
                        "tong-so-du-an", dataProject.getTotalProjects(),
                        "tong-so-du-an-can-quyen-gop", dataProject.getTotalOnGoingProjects(),
                        "tong-so-du-an-dang-thi-cong", dataProject.getTotalProcessingProjects(),
                        "tong-so-du-an-da-hoan-thanh", dataProject.getTotalDoneProjects()))
                .build();
    }

}

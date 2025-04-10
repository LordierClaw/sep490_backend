package vn.com.fpt.sep490_g28_summer2024_be.web.rest.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.service.statistics.StatisticsService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardRest {
    public final StatisticsService statisticsService;

    @GetMapping("/overview")
    public ApiResponse<?> dashboard(@RequestParam(value = "month", required = false) Integer month,
                                    @RequestParam(value = "year", required = false) Integer year){

        month = month == null ? LocalDateTime.now().getMonthValue() : month;
        year = year == null ? LocalDateTime.now().getYear() : year;

        Map<String, Object> res = new HashMap<>();
        res.put("tong-so-luot-quyen-gop", statisticsService.getTotalCountDonations(month, year));
        res.put("tong-so-quyen-gop", statisticsService.getTotalDonation(month, year));
        res.put("tong-so-quyen-gop-sai", statisticsService.getTotalWrongDonation(month, year));
        res.put("tong-so-tien-tai-tro", statisticsService.getTotalSponsorValue(month, year));

        return ApiResponse.builder()
                .code("200")
                .message("success")
                .data(res)
                .build();
    }

    @GetMapping("/pie-chart")
    public ApiResponse<?> getDataPieChart(@RequestParam(value = "month", required = false) Integer month,
                                          @RequestParam(value = "year", required = false) Integer year){

        month = month == null ? LocalDateTime.now().getMonthValue() : month;
        year = year == null ? LocalDateTime.now().getYear() : year;

        var data = statisticsService.getDataPieChart(month, year);

        return ApiResponse.builder()
                .code("200")
                .message("success")
                .data(data)
                .build();
    }

    @GetMapping("/total-donation/line-chart")
    public ApiResponse<?> getDataLineChart(@RequestParam(value = "year", required = false) Integer year){

        year = year == null ? LocalDateTime.now().getYear() : year;

        var data = statisticsService.getDataLineChart(year);

        return ApiResponse.builder()
                .code("200")
                .message("success")
                .data(data)
                .build();
    }

    @GetMapping("/total-donation/bar-chart")
    public ApiResponse<?> getDataBarChart(@RequestParam(value = "month", required = false) Integer month,
                                          @RequestParam(value = "year", required = false) Integer year){

        month = month == null ? LocalDateTime.now().getMonthValue() : month;
        year = year == null ? LocalDateTime.now().getYear() : year;

        var data = statisticsService.getDataBarChart(month, year);

        return ApiResponse.builder()
                .code("200")
                .message("success")
                .data(data)
                .build();
    }

}

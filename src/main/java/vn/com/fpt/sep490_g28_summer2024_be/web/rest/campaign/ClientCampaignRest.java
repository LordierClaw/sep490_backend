package vn.com.fpt.sep490_g28_summer2024_be.web.rest.campaign;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignStatisticsResponse;
import vn.com.fpt.sep490_g28_summer2024_be.service.campaign.CampaignService;
import vn.com.fpt.sep490_g28_summer2024_be.service.project.ProjectService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/campaigns")
@RequiredArgsConstructor
public class ClientCampaignRest {

    private final CampaignService campaignService;
    private final ProjectService projectService;

    @GetMapping("")
    public ApiResponse<?> viewListCampaigns(){
        return ApiResponse.builder()
                .code(ErrorCode.HTTP_OK.getCode())
                .message("Lấy danh sách chiến dịch thành công!")
                .data(campaignService.getAllCampaigns())
                .build();
    }

    @GetMapping("/{id}/projects")
    public ApiResponse<?> viewListProjectsByCampaignId(@RequestParam(defaultValue = "0", required = false) Integer page,
                                                       @RequestParam(defaultValue = "2", required = false) Integer size,
                                                       @RequestParam(required = false) Integer status,
                                                       @RequestParam(required = false) BigDecimal minTotalBudget,
                                                       @RequestParam(required = false) BigDecimal maxTotalBudget,
                                                       @PathVariable BigInteger id) {
        return ApiResponse.builder()
                .code(ErrorCode.HTTP_OK.getCode())
                .message("danh sách dự án của chiến dịch!")
                .data(projectService.viewProjectsClientByCampaignId(page, size, status, id, minTotalBudget, maxTotalBudget))
                .build();
    }


    @GetMapping("/{id}")
    public ApiResponse<?> viewCampaignDetail(@PathVariable BigInteger id) {
        CampaignResponseDTO campaignDTO = campaignService.getCampaignClientById(id);
        return ApiResponse.builder()
                .code("200")
                .message("Chi tiết chiến dịch")
                .data(campaignDTO)
                .build();
    }

    @GetMapping("/id-title")
    public ApiResponse<?> getAllCampaignsIdAndName() {
        List<CampaignResponseDTO> campaignDTO = campaignService.getAllCampaignsIdAndName();
        return ApiResponse.builder()
                .code("200")
                .message("ID và Title của chiến dịch")
                .data(campaignDTO)
                .build();
    }
    @GetMapping("/statistics")
    public ApiResponse<CampaignStatisticsResponse> viewProjectStatistics() {
        CampaignStatisticsResponse statistics = campaignService.getCountProjectsGroupedByCampaignAndStatus();
        return ApiResponse.<CampaignStatisticsResponse>builder()
                .code("200")
                .message("Thống kê dự án trong chiến dịch")
                .data(statistics)
                .build();
    }
}

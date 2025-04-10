package vn.com.fpt.sep490_g28_summer2024_be.web.rest.project;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignProjectsDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.tracking.GroupedTrackingImageDTO;
import vn.com.fpt.sep490_g28_summer2024_be.service.donation.DonationService;
import vn.com.fpt.sep490_g28_summer2024_be.service.project.ProjectService;
import vn.com.fpt.sep490_g28_summer2024_be.service.sponsor.SponsorService;
import vn.com.fpt.sep490_g28_summer2024_be.service.tracking.TrackingService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ClientProjectRest {

    private final ProjectService projectService;
    private final DonationService donationService;
    private final TrackingService trackingService;
    private final SponsorService sponsorService;
    @GetMapping("/{id}/donations")
    public ApiResponse<?> viewListDonationByProjectId(@RequestParam(defaultValue = "0", required = false) Integer page,
                                                      @RequestParam(defaultValue = "10", required = false) Integer size,
                                                      @RequestParam(required = false) String description,
                                                      @PathVariable BigInteger id) {
        return ApiResponse.builder()
                .code("200")
                .message("Danh sách donate")
                .data(donationService.viewListDonations(page, size, id, description))
                .build();
    }


    @GetMapping("/cards")
    public ApiResponse<?> viewListProjectsCard(@RequestParam(defaultValue = "0", required = false) Integer page,
                                               @RequestParam(defaultValue = "16", required = false) Integer size,
                                               @RequestParam(required = false) String title,
                                               @RequestParam(value = "campaign_id", required = false) BigInteger campaignId,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(required = false) Integer year,
                                               @RequestParam(required = false) String code,
                                               @RequestParam(required = false) BigDecimal minTotalBudget,
                                               @RequestParam(required = false) BigDecimal maxTotalBudget) {
        return ApiResponse.builder()
                .code("200")
                .message("Danh sách dự án!")
                .data(projectService.viewProjectCards(page, size, title, campaignId, status, year, code,minTotalBudget, maxTotalBudget))
                .build();
    }


    @GetMapping("/{id}")
    public ApiResponse<?> viewProjectDetail(@PathVariable BigInteger id) {
        return ApiResponse.builder()
                .code("200")
                .message("Chi tiết dự án")
                .data(projectService.getProjectDetailClient(id))
                .build();
    }


    @GetMapping("/{id}/tracking-images")
    public ApiResponse<?> getImagesByProjectIdAndTitles(@PathVariable BigInteger id) {
        List<GroupedTrackingImageDTO> images = trackingService.getImagesByProjectIdAndTitles(id);
        return ApiResponse.<List<GroupedTrackingImageDTO>>builder()
                .code("200")
                .message("Success")
                .data(images)
                .build();
    }


    @GetMapping(value = "/{id}/sponsors")
    public ApiResponse<?> viewSponsors(@PathVariable(name = "id") BigInteger id,
                                       @RequestParam(defaultValue = "0", required = false) Integer page,
                                       @RequestParam(defaultValue = "10", required = false) Integer size,
                                       @RequestParam(name = "company_name", required = false) String companyName) {
        return ApiResponse.builder()
                .code(ErrorCode.HTTP_OK.getCode())
                .message(ErrorCode.HTTP_OK.getMessage())
                .data(sponsorService.viewListSponsorInProject(page, size, companyName, id))
                .build();
    }

    @GetMapping("/by-status")
    public ApiResponse<?> getProjectsByStatus() {
        List<CampaignProjectsDTO> campaignProjects = projectService.getProjectsByStatus();
        return ApiResponse.builder()
                .code("200")
                .message("OK")
                .data(campaignProjects)
                .build();
    }
}

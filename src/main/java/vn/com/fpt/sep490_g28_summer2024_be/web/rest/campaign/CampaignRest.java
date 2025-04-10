package vn.com.fpt.sep490_g28_summer2024_be.web.rest.campaign;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigInteger;
import java.util.*;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Campaign;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.repository.CampaignRepository;
import vn.com.fpt.sep490_g28_summer2024_be.service.campaign.CampaignService;
import vn.com.fpt.sep490_g28_summer2024_be.service.exportFile.ExportFileService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/admin/campaigns")
@RequiredArgsConstructor
public class CampaignRest {

    private final CampaignService campaignService;
    private final ExportFileService exportFileService;
    private final CampaignRepository campaignRepository;

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> viewListCampaigns(@RequestParam(defaultValue = "0") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) String title,
                                            @RequestParam(required = false) Boolean is_active
    ) {
        return ApiResponse.builder()
                .code("200")
                .message("Danh sách chien dich!")
                .data(campaignService.viewByFilter(page, size, title, is_active))
                .build();
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ApiResponse<?> createCampaign(@RequestPart("campaign") @Valid CampaignRequestDTO campaignDTO,
                                         @RequestPart(value = "image", required = false) MultipartFile image) {

        CampaignResponseDTO createdCampaign = campaignService.addCampaign(campaignDTO, image);
        return ApiResponse.builder()
                .code("200")
                .message("Campaign created successfully!")
                .data(createdCampaign)
                .build();
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ApiResponse<?> updateCampaign(@RequestPart("campaign") @Valid CampaignRequestDTO campaignDTO,
                                         @RequestPart(value = "image", required = false) MultipartFile images,
                                         @PathVariable BigInteger id) {
        CampaignResponseDTO upadatedCampaign = campaignService.updateCampaign(campaignDTO, id, images);
        return ApiResponse.builder()
                .code("200")
                .message("Campaign updated successfully!")
                .data(upadatedCampaign)
                .build();
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> getCampaignById(@PathVariable BigInteger id) {
        CampaignResponseDTO campaignDTO = campaignService.getCampaignById(id);
        return ApiResponse.builder()
                .code("200")
                .message("OK")
                .data(campaignDTO)
                .build();
    }

    @GetMapping("/id-title")
    public ApiResponse<?> getAllCampaignsIdAndName() {
        List<CampaignResponseDTO> campaignDTO = campaignService.getAllCampaignsIdAndName();
        return ApiResponse.builder()
                .code("200")
                .message("OK")
                .data(campaignDTO)
                .build();
    }

    @GetMapping("/export/{id}")
    public ResponseEntity<byte[]> exportProjectByIdToPdf(@PathVariable BigInteger id) {

        Campaign campaign = campaignRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NOT_EXISTED));

        String exportTitle = "Báo cáo chiến dịch " + campaign.getTitle();
        byte[] pdfBytes = exportFileService.exportCampaignReportToPdf(id,exportTitle);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "project_detail.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}








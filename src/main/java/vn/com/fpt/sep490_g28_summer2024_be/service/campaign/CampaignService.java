package vn.com.fpt.sep490_g28_summer2024_be.service.campaign;

import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignStatisticsResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;

import java.math.BigInteger;
import java.util.List;


public interface CampaignService {
    PageResponse<?> viewByFilter(Integer page, Integer size, String title, Boolean isActive);

    CampaignResponseDTO getCampaignById(BigInteger id) ;

    List<CampaignResponseDTO> getAllCampaignsIdAndName();

    CampaignResponseDTO addCampaign(CampaignRequestDTO campaignDTO, MultipartFile newImage);

    CampaignResponseDTO updateCampaign(CampaignRequestDTO campaignDTO, BigInteger id, MultipartFile newImage);

    List<CampaignResponseDTO> getAllCampaigns();

    CampaignResponseDTO getCampaignClientById(BigInteger id) ;

    CampaignStatisticsResponse getCountProjectsGroupedByCampaignAndStatus();

}
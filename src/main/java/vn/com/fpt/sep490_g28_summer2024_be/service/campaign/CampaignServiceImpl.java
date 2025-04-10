
package vn.com.fpt.sep490_g28_summer2024_be.service.campaign;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignStatisticsResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectByStatusAndCampaignResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Campaign;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseServiceImpl;
import vn.com.fpt.sep490_g28_summer2024_be.mapper.Mapper;
import vn.com.fpt.sep490_g28_summer2024_be.repository.CampaignRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;
import vn.com.fpt.sep490_g28_summer2024_be.utils.SlugUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final FirebaseServiceImpl firebaseService;
    private final ProjectRepository projectRepository;
    private final SlugUtils slugUtils;

    @Override
    public PageResponse<?> viewByFilter(Integer page, Integer size, String title, Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Campaign> campaignPage = campaignRepository.findCampaignsByFilters(title, isActive, pageable);

        List<CampaignResponseDTO> campaignDTOS = campaignPage.map(campaign -> CampaignResponseDTO.builder()
                .campaignId(campaign.getCampaignId())
                .title(campaign.getTitle())
                .slug(campaign.getSlug())
                .description(campaign.getDescription())
                .createdAt(campaign.getCreatedAt())
                .thumbnail(campaign.getThumbnail())
                .updatedAt(campaign.getUpdatedAt())
                .isActive(campaign.getIsActive())
                .build()).getContent();

        return PageResponse.<CampaignResponseDTO>builder()
                .content(campaignDTOS)
                .limit(size)
                .offset(page)
                .total((int) campaignPage.getTotalElements())
                .build();
    }

    @Override
    public CampaignResponseDTO getCampaignById(BigInteger id) {
        return campaignRepository.findById(id)
                .map(campaign -> Mapper.mapEntityToDto(campaign, CampaignResponseDTO.class))
                .orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NO_CONTENT));
    }

    @Override
    public List<CampaignResponseDTO> getAllCampaignsIdAndName() {
        List<Campaign> campaigns = campaignRepository.findAll();
        return campaigns.stream()
                .map(campaign -> CampaignResponseDTO.builder()
                        .campaignId(campaign.getCampaignId())
                        .slug(campaign.getSlug())
                        .title(campaign.getTitle())
                        .build())
                .toList();
    }

    @Override
    public CampaignResponseDTO addCampaign(CampaignRequestDTO campaignDTO, MultipartFile newImage) {
        if (campaignRepository.existsByTitle(campaignDTO.getTitle())) {
            throw new AppException(ErrorCode.DUPLICATE_TITLE);
        }

        Campaign campaign = Campaign.builder()
                .title(campaignDTO.getTitle())
                .description(campaignDTO.getDescription())
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(campaign);


        campaign.setSlug(slugUtils.genSlug(String.format("%s %s", campaign.getCampaignId(), campaign.getTitle())));
        campaignRepository.save(campaign);

        if (newImage != null) {
            // Kiểm tra loại tệp
            if (newImage.getContentType() == null || !newImage.getContentType().startsWith("image/")) {
                throw new AppException(ErrorCode.HTTP_FILE_IS_NOT_IMAGE);
            }

            if (newImage.getSize() > 2 * 1024 * 1024) { // 2MB
                throw new AppException(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT);
            }

            try {
                String thumbnail = firebaseService.uploadOneFile(newImage, campaign.getCampaignId(), "campaign-images/thumbnail");
                campaign.setThumbnail(thumbnail);
                campaignRepository.save(campaign);
            } catch (IOException e) {
                throw new AppException(ErrorCode.UPLOAD_FAILED);
            }
        }

        return CampaignResponseDTO.builder()
                .campaignId(campaign.getCampaignId())
                .title(campaign.getTitle())
                .slug(campaign.getSlug())
                .thumbnail(campaign.getThumbnail())
                .build();
    }

    @Override
    public CampaignResponseDTO updateCampaign(CampaignRequestDTO campaignDTO, BigInteger id, MultipartFile newImage) {
        try {
            Campaign campaign = campaignRepository.findById(id).orElseThrow(() ->
                    new AppException(ErrorCode.CAMPAIGN_NOT_EXISTED));

            if (campaignRepository.existsByTitle(campaignDTO.getTitle()) && !campaign.getTitle().equals(campaignDTO.getTitle())) {
                throw new AppException(ErrorCode.DUPLICATE_TITLE);
            }

            campaign.setTitle(campaignDTO.getTitle());
            campaign.setDescription(campaignDTO.getDescription());
            campaign.setUpdatedAt(LocalDate.now());

            if (campaignDTO.getThumbnail() == null || campaignDTO.getThumbnail().isBlank()) {
                if (campaign.getThumbnail() != null) {
                    firebaseService.deleteFileByPath(campaign.getThumbnail());
                }
                campaign.setThumbnail(null);
            }

            // Xử lý hình ảnh mới
            if (newImage != null) {
                // Kiểm tra loại tệp
                if (newImage.getContentType() == null || !newImage.getContentType().startsWith("image/")) {
                    throw new AppException(ErrorCode.HTTP_FILE_IS_NOT_IMAGE);
                }

                // Kiểm tra kích thước tệp
                if (newImage.getSize() > 2 * 1024 * 1024) { // 2MB
                    throw new AppException(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT);
                }

                String fileName = firebaseService.uploadOneFile(newImage, id, "campaign-images/thumbnail");
                campaign.setThumbnail(fileName);
            }

            Campaign savedCampaign = campaignRepository.save(campaign);
            return Mapper.mapEntityToDto(savedCampaign, CampaignResponseDTO.class);
        } catch (IOException e) {
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }


    @Override
    public List<CampaignResponseDTO> getAllCampaigns() {
        List<Campaign> campaigns = campaignRepository.findAll();
        return campaigns.stream().map(campaign -> CampaignResponseDTO.builder()
                .campaignId(campaign.getCampaignId())
                .title(campaign.getTitle())
                .slug(campaign.getSlug())
                .description(campaign.getDescription())
                .thumbnail(campaign.getThumbnail())
                .build()).toList();
    }


    @Override
    public CampaignResponseDTO getCampaignClientById(BigInteger id) {
        return campaignRepository.findById(id)
                .map(campaign -> CampaignResponseDTO.builder()
                        .campaignId(campaign.getCampaignId())
                        .title(campaign.getTitle())
                        .slug(campaign.getSlug())
                        .description(campaign.getDescription())
                        .createdAt(campaign.getCreatedAt())
                        .thumbnail(campaign.getThumbnail())
                        .updatedAt(campaign.getUpdatedAt())
                        .isActive(campaign.getIsActive())
                        .build())
                .orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NO_CONTENT));
    }

    @Override
    public CampaignStatisticsResponse getCountProjectsGroupedByCampaignAndStatus() {
        List<Object[]> results = projectRepository.countProjectsGroupedByCampaignAndStatus();

        Map<String, ProjectByStatusAndCampaignResponseDTO> map = new HashMap<>();
        long totalProjects = 0;

        for (Object[] result : results) {
            long campaignId = ((Number) result[0]).longValue();
            int status = ((Number) result[1]).intValue();
            long count = ((Number) result[2]).longValue();

            String campaignTitle = campaignRepository.findById(BigInteger.valueOf(campaignId))
                    .map(Campaign::getTitle)
                    .orElse("Unknown Campaign");

            ProjectByStatusAndCampaignResponseDTO dto = map.getOrDefault(campaignTitle, new ProjectByStatusAndCampaignResponseDTO(
                    campaignTitle, 0L, 0L, 0L, 0L, 0L));
            dto.setCount(dto.getCount() + count);

            switch (status) {
                case 1 -> dto.setStatus1(dto.getStatus1() + count);
                case 2 -> dto.setStatus2(dto.getStatus2() + count);
                case 3 -> dto.setStatus3(dto.getStatus3() + count);
                case 4 -> dto.setStatus4(dto.getStatus4() + count);
            }

            map.put(campaignTitle, dto);
            totalProjects += count;
        }

        return new CampaignStatisticsResponse(new ArrayList<>(map.values()), totalProjects);
    }

}
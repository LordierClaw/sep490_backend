package vn.com.fpt.sep490_g28_summer2024_be.service.project;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignProjectsDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectUpdateRequestDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public interface ProjectService {
    PageResponse<?> viewByFilter(Integer page, Integer size, String title, BigInteger campaignId, Integer status, String province, String year);
    PageResponse<ProjectResponseDTO> viewProjectsByAccountId(Integer page, Integer size, String email, String title, BigInteger campaignId, Integer status, String province, String year);
    ProjectResponseDTO getProjectById(BigInteger id);

    ProjectResponseDTO getProjectDetailClient(BigInteger id);
    ProjectResponseDTO addProject(ProjectRequestDTO projectDTO, MultipartFile[] projectImages, MultipartFile[] projectFiles);
    ProjectResponseDTO updateProject(ProjectUpdateRequestDTO projectDTO, BigInteger id, MultipartFile[] images, MultipartFile[] files);
    ProjectResponseDTO updateProjectStatus(BigInteger projectId, Integer status);
    PageResponse<?> viewProjectCards(Integer page, Integer size, String title, BigInteger campaignId, Integer status, Integer year, String code,BigDecimal minTotalDonation, BigDecimal maxTotalDonation);
    PageResponse<ProjectResponseDTO> viewProjectsClientByCampaignId(Integer page, Integer size, Integer status, BigInteger campaignId, BigDecimal minTotalBudget, BigDecimal maxTotalBudget);
    List<CampaignProjectsDTO> getProjectsByStatus();
}
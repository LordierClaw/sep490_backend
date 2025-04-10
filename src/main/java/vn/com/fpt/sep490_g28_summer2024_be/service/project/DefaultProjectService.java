package vn.com.fpt.sep490_g28_summer2024_be.service.project;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.AppConfig;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignProjectsDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.construction.ConstructionResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.file.ProjectImageDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.file.RelatedFileDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.*;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.interfacedto.ProjectInterfaceDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.*;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseService;
import vn.com.fpt.sep490_g28_summer2024_be.repository.*;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.utils.CodeUtils;
import vn.com.fpt.sep490_g28_summer2024_be.utils.SlugUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
@Service
@RequiredArgsConstructor
public class DefaultProjectService implements ProjectService {

    private final AccountRepository accountRepository;
    private final ProjectRepository projectRepository;
    private final CampaignRepository campaignRepository;
    private final ProjectImageRepository projectImageRepository;
    private final BudgetRepository budgetRepository;
    private final AssignRepository assignRepository;
    private final SponsorRepository sponsorRepository;
    private final FirebaseService firebaseService;
    private final RelatedFileRepository relatedFileRepository;
    private final ConstructionRepository constructionRepository;
    private final CodeUtils codeUtils;
    private final SlugUtils slugUtils;

    @Override
    public PageResponse<?> viewByFilter(Integer page, Integer size, String title, BigInteger campaignId, Integer status, String province, String year) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectInterfaceDTO> listedProject = projectRepository.findProjectByFilters(title, campaignId, year, province, status, pageable);

        var dtoList = listedProject.map(project -> ProjectResponseDTO.builder()
                .projectId(project.getProjectId())
                .code(project.getCode())
                .title(project.getTitle())
                .campaign(CampaignResponseDTO.builder()
                        .title(project.getCampaignTitle())
                        .build())
                .totalBudget(project.getTotalBudget())
                .amountNeededToRaise(project.getAmountNeededToRaise())
                .totalDonation(project.getTotalDonation())
                .address(project.getAddress())
                .ward(project.getWard())
                .district(project.getDistrict())
                .province(project.getProvince())
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .build()).toList();
        return PageResponse.<ProjectResponseDTO>builder()
                .content(dtoList)
                .limit(size)
                .offset(page)
                .total((int) listedProject.getTotalElements())
                .build();
    }

    @Override
    public PageResponse<ProjectResponseDTO> viewProjectsByAccountId(Integer page, Integer size, String email, String title, BigInteger campaignId, Integer status, String province, String year) {
        Pageable pageable = PageRequest.of(page, size);

        Account loggedAccount = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

        if (loggedAccount.getRole().getRoleName().equalsIgnoreCase("Admin")) {
            throw new AppException(ErrorCode.ADMIN_ACCESS_DENIED);
        }

        Page<ProjectInterfaceDTO> projects = projectRepository.findProjectsByAccountId(loggedAccount.getAccountId(), title, campaignId, year, province, status, pageable);


        List<ProjectResponseDTO> dtoList = new ArrayList<>();
        projects.forEach(project -> dtoList.add(ProjectResponseDTO.builder()
                .projectId(project.getProjectId())
                .code(project.getCode())
                .title(project.getTitle())
                .campaign(CampaignResponseDTO.builder()
                        .title(project.getCampaignTitle())
                        .build())
                .totalBudget(project.getTotalBudget())
                .amountNeededToRaise(project.getAmountNeededToRaise())
                .totalDonation(project.getTotalDonation())
                .address(project.getAddress())
                .ward(project.getWard())
                .district(project.getDistrict())
                .province(project.getProvince())
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .build()));

        return PageResponse.<ProjectResponseDTO>builder()
                .content(dtoList)
                .limit(size)
                .offset(page)
                .total((int) projects.getTotalElements())
                .build();
    }

    @Override
    public ProjectResponseDTO getProjectById(BigInteger id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));
        return ProjectResponseDTO.builder()
                .projectId(project.getProjectId())
                .code(project.getCode())
                .title(project.getTitle())
                .campaign(CampaignResponseDTO.builder()
                        .campaignId(project.getCampaign().getCampaignId())
                        .title(project.getCampaign().getTitle())
                        .build())
                .background(project.getBackground())
                .address(project.getAddress())
                .ward(project.getWard())
                .district(project.getDistrict())
                .province(project.getProvince())
                .constructions(project.getConstructions().stream().map(construction -> ConstructionResponseDTO.builder()
                        .constructionId(construction.getConstructionId())
                        .title(construction.getTitle())
                        .quantity(construction.getQuantity())
                        .unit(construction.getUnit())
                        .note(construction.getNote())
                        .build()).toList())
                .status(project.getStatus())
                .totalBudget(project.getTotalBudget())
                .amountNeededToRaise(project.getAmountNeededToRaise())
                .images(project.getProjectImages().stream().map(projectImage -> ProjectImageDTO.builder()
                        .image(projectImage.getImage())
                        .build()).collect(toList()))
                .files(project.getRelatedFile().stream().map(relatedFile -> RelatedFileDTO.builder()
                        .file(relatedFile.getFile())
                        .build()).toList())
                .build();
    }


    @Override
    @Transactional
    public ProjectResponseDTO addProject(ProjectRequestDTO projectDTO, MultipartFile[] projectImages, MultipartFile[] projectFiles) {
        try {
            CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername()).orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

            Campaign campaign = campaignRepository.findById(projectDTO.getCampaign().getCampaignId()).orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NO_CONTENT));

            if (projectRepository.existsByTitle(projectDTO.getTitle())) {
                throw new AppException(ErrorCode.DUPLICATE_TITLE);
            }

            Project project = Project.builder()
                    .title(projectDTO.getTitle())
                    .background(projectDTO.getBackground())
                    .address(projectDTO.getAddress())
                    .ward(projectDTO.getWard())
                    .district(projectDTO.getDistrict())
                    .province(projectDTO.getProvince())
                    .totalBudget(new BigDecimal(projectDTO.getTotalBudget()))
                    .amountNeededToRaise(new BigDecimal(projectDTO.getAmountNeededToRaise()))
                    .status(1)
                    .campaign(campaign)
                    .build();

            project.setCreatedAt(LocalDateTime.now());
            project.setUpdatedAt(LocalDateTime.now());
            projectRepository.save(project);

            project.setCode(codeUtils.genCode(AppConfig.PROJECT_PREFIX, project.getProjectId()));
            project.setSlug(slugUtils.genSlug(String.format("%s %s", project.getCode(), project.getTitle())));
            projectRepository.save(project);

            //images
            if (projectImages != null) {
                if (!firebaseService.filesIsImage(projectImages))
                    throw new AppException(ErrorCode.HTTP_FILE_IS_NOT_IMAGE);

                //upload file into firebase
                List<String> images = firebaseService.uploadMultipleFile(projectImages, project.getProjectId(), "project/project_images");

                images.forEach(s ->
                        projectImageRepository.save(ProjectImage.builder()
                                .project(project)
                                .image(s)
                                .build())
                );
            }

            //files
            if (projectFiles != null) {
                //upload file into firebase
                List<String> files = firebaseService.uploadMultipleFile(projectImages, project.getProjectId(), "project/project_files");
                files.forEach(s ->
                        relatedFileRepository.save(RelatedFile.builder()
                                .project(project)
                                .file(s)
                                .build())
                );
            }

            //construction
            if (projectDTO.getConstructions() != null && projectDTO.getConstructions().size() != 0) {
                projectDTO.getConstructions().forEach(construction ->
                        constructionRepository.save(Construction.builder()
                                .project(project)
                                .title(construction.getTitle())
                                .quantity(construction.getQuantity())
                                .unit(construction.getUnit())
                                .note(construction.getNote())
                                .build()));
            }

            //assign
            if (projectDTO.getAssign() != null && projectDTO.getAssign().getAccounts().size() != 0) {
                projectDTO.getAssign().getAccounts().forEach(member -> {
                    Account account = accountRepository.findByAccountId(member.getAccountId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                    assignRepository.save(Assign.builder()
                            .account(account)
                            .project(project)
                            .createdBy(loggedAccount)
                            .createdAt(LocalDateTime.now())
                            .updatedBy(loggedAccount)
                            .updatedAt(LocalDateTime.now())
                            .build());
                });
            }


            //budget
            if (projectDTO.getBudgets() != null && projectDTO.getBudgets().size() != 0) {
                projectDTO.getBudgets().forEach(budget -> budgetRepository.save(Budget.builder()
                        .title(budget.getTitle())
                        .project(project)
                        .unitPrice(new BigDecimal(budget.getUnitPrice()))
                        .note(budget.getNote())
                        .status(2)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()));
            }

            return ProjectResponseDTO.builder()
                    .projectId(project.getProjectId())
                    .title(project.getTitle())
                    .build();
        } catch (IOException e) {
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }

    @Override
    @Transactional
    public ProjectResponseDTO updateProject(ProjectUpdateRequestDTO projectDTO, BigInteger id, MultipartFile[] images, MultipartFile[] files) {
        try {
            Project refProject = projectRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));

            //Check if the logged-in account is an admin or an employee assigned to the project
            CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername()).orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

            if (!loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin") &&
                    !refProject.getAssigns().stream().map(Assign::getAccount).toList().contains(loggedAccount)){
                throw new  AppException(ErrorCode.ACCESS_DENIED);
            }

            Campaign campaign = campaignRepository.findById(projectDTO.getCampaign().getCampaignId()).orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NO_CONTENT));

            if (!refProject.getTitle().equals(projectDTO.getTitle()) && projectRepository.existsByTitle(projectDTO.getTitle())) {
                throw new AppException(ErrorCode.DUPLICATE_TITLE);
            }

            refProject.setTitle(projectDTO.getTitle());
            refProject.setBackground(projectDTO.getBackground());
            refProject.setAddress(projectDTO.getAddress());
            refProject.setWard(projectDTO.getWard());
            refProject.setDistrict(projectDTO.getDistrict());
            refProject.setProvince(projectDTO.getProvince());
            refProject.setCampaign(campaign);
            refProject.setUpdatedAt(LocalDateTime.now());
            projectRepository.save(refProject);

            List<ProjectImage> imageList = refProject.getProjectImages();
            //delete images
            if (!imageList.isEmpty()) {
                if (projectDTO.getImages() != null &&
                        projectDTO.getImages().size() != 0) {
                    List<String> imagePaths = projectDTO.getImages().stream().map(ProjectImageDTO::getImage).toList();
                    for (ProjectImage image : imageList) {
                        if (!imagePaths.contains(image.getImage())) {
                            projectImageRepository.deleteById(image.getProjectImageId());
                            firebaseService.deleteFileByPath(image.getImage());
                        }
                    }
                } else {
                    for (ProjectImage image : imageList) {
                        projectImageRepository.deleteById(image.getProjectImageId());
                        firebaseService.deleteFileByPath(image.getImage());
                    }
                }
            }

            //upload images
            if (images != null && images.length != 0) {
                if (!firebaseService.filesIsImage(images)) throw new AppException(ErrorCode.HTTP_FILE_IS_NOT_IMAGE);
                //upload file into firebase
                List<String> newImages = firebaseService.uploadMultipleFile(images, refProject.getProjectId(), "project/project_images");
                newImages.forEach(s -> projectImageRepository.save(ProjectImage.builder()
                        .project(refProject)
                        .image(s)
                        .build()));
            }

            //delete related files
            List<RelatedFile> relatedFiles = refProject.getRelatedFile();
            if (!relatedFiles.isEmpty()) {
                List<String> filePaths = projectDTO.getFiles() != null && projectDTO.getFiles().size() != 0 ? projectDTO.getFiles().stream().map(RelatedFileDTO::getFile).toList() : List.of();
                for (RelatedFile relatedFile : relatedFiles) {
                    if (!filePaths.contains(relatedFile.getFile())) {
                        relatedFileRepository.delete(relatedFile);
                        firebaseService.deleteFileByPath(relatedFile.getFile());
                    }
                }
            }

            //upload related files
            if (files != null && files.length != 0) {
                //upload file into firebase
                List<String> newFiles = firebaseService.uploadMultipleFile(files, refProject.getProjectId(), "project/related_files");
                newFiles.forEach(s -> relatedFileRepository.save(
                        RelatedFile.builder()
                                .project(refProject)
                                .file(s)
                                .build()));
            }

            System.out.println("construction: " + refProject.getConstructions());

            //update constructions
            if (projectDTO.getConstructions() != null && !projectDTO.getConstructions().isEmpty()) {
                //update and add new constructions
                List<Construction> updatedConstruction = projectDTO.getConstructions().stream().map(construction ->
                        Construction.builder()
                                .project(refProject)
                                .constructionId(construction.getConstructionId())
                                .title(construction.getTitle())
                                .quantity(construction.getQuantity())
                                .unit(construction.getUnit())
                                .note(construction.getNote())
                                .build()
                ).toList();
                constructionRepository.saveAll(updatedConstruction);

                //delete redundant constructions
                List<Construction> existingConstructions = refProject.getConstructions() != null ? refProject.getConstructions() : Collections.emptyList();
                List<Construction> deletionConstructions = existingConstructions.stream().filter(construction -> !updatedConstruction.contains(construction)).toList();
                constructionRepository.deleteAll(deletionConstructions);
            } else {
                throw new AppException(ErrorCode.PROJECT_CONSTRUCTION_CONFLICT);
            }

            return ProjectResponseDTO.builder()
                    .projectId(refProject.getProjectId())
                    .title(refProject.getTitle())
                    .build();
        } catch (IOException e) {
            throw new AppException(ErrorCode.UPLOAD_FAILED);

        }
    }

    @Override
    public ProjectResponseDTO updateProjectStatus(BigInteger projectId, Integer status) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));

        //Check if the logged-in account is an admin or an employee assigned to the project
        CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername()).orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

        if (!loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin") &&
                !project.getAssigns().stream().map(Assign::getAccount).toList().contains(loggedAccount)){
            throw new  AppException(ErrorCode.ACCESS_DENIED);
        }

        project.setStatus(status);
        projectRepository.save(project);
        return ProjectResponseDTO.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .build();
    }

    @Override
    public PageResponse<?> viewProjectCards(Integer page, Integer size, String title, BigInteger campaignId, Integer status, Integer year, String code,BigDecimal minTotalBudget, BigDecimal maxTotalBudget) {
        Pageable pageable = PageRequest.of(page, size);

        Page<ProjectInterfaceDTO> listedProject = projectRepository.findProjectCards(title, campaignId, status, year, code, minTotalBudget, maxTotalBudget, pageable);

        List<ProjectResponseDTO> dtoList = new ArrayList<>();

        listedProject.forEach(project -> {
            Project projectEntity = projectRepository.findById(project.getProjectId())
                    .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));

            ProjectImageDTO firstImageDTO = projectEntity.getProjectImages().stream()
                    .findFirst()
                    .map(projectImage -> ProjectImageDTO.builder()
                            .image(projectImage.getImage())
                            .build())
                    .orElse(null);

            dtoList.add(ProjectResponseDTO.builder()
                    .projectId(project.getProjectId())
                    .code(project.getCode())
                    .title(project.getTitle())
                    .slug(projectEntity.getSlug())
                    .background(project.getBackground())
                    .campaign(CampaignResponseDTO.builder()
                            .title(project.getCampaignTitle())
                            .build())
                    .totalBudget(project.getTotalBudget())
                    .amountNeededToRaise(project.getAmountNeededToRaise())
                    .totalDonation(project.getTotalDonation())
                    .address(project.getAddress())
                    .ward(project.getWard())
                    .district(project.getDistrict())
                    .province(project.getProvince())
                    .status(project.getStatus())
                    .images(firstImageDTO == null ? Collections.emptyList() : List.of(firstImageDTO))
                    .createdAt(project.getCreatedAt())
                    .build());
        });

        return PageResponse.<ProjectResponseDTO>builder()
                .content(dtoList)
                .limit(size)
                .offset(page)
                .total((int) listedProject.getTotalElements())
                .build();
    }


    @Override
    @Transactional
    public PageResponse<ProjectResponseDTO> viewProjectsClientByCampaignId(Integer page, Integer size, Integer status, BigInteger campaignId, BigDecimal minTotalBudget, BigDecimal maxTotalBudget) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectInterfaceDTO> listedProject = projectRepository.findProjectsClientByCampaignId(status, campaignId, minTotalBudget, maxTotalBudget, pageable);

        List<ProjectResponseDTO> dtoList = new ArrayList<>();

        listedProject.forEach(p -> {
            long numberSponsors = sponsorRepository.countSponsorsByProjectId(p.getProjectId());

            Project project = projectRepository.findById(p.getProjectId()).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));

            List<ProjectImageDTO> projectImageDTOList = project.getProjectImages().stream()
                    .map(projectImage -> ProjectImageDTO.builder()
                            .projectImageId(projectImage.getProjectImageId())
                            .image(projectImage.getImage())
                            .build()).collect(Collectors.toList());

            List<ConstructionResponseDTO> constructionResponseDTOS = project.getConstructions().stream()
                    .map(construction -> ConstructionResponseDTO.builder()
                            .constructionId(construction.getConstructionId())
                            .title(construction.getTitle())
                            .quantity(construction.getQuantity())
                            .unit(construction.getUnit())
                            .note(construction.getNote())
                            .build()).collect(Collectors.toList());

            dtoList.add(ProjectResponseDTO.builder()
                    .projectId(project.getProjectId())
                    .code(project.getCode())
                    .title(project.getTitle())
                    .slug(project.getSlug())
                    .background(project.getBackground())
                    .campaign(CampaignResponseDTO.builder()
                            .title(project.getCampaign().getTitle())
                            .build())
                    .totalBudget(project.getTotalBudget())
                    .amountNeededToRaise(project.getAmountNeededToRaise())
                    .totalDonation(p.getTotalDonation())
                    .address(project.getAddress())
                    .ward(project.getWard())
                    .district(project.getDistrict())
                    .constructions(constructionResponseDTOS)
                    .province(project.getProvince())
                    .numberSponsors(numberSponsors)
                    .status(project.getStatus())
                    .images(projectImageDTOList)
                    .createdAt(project.getCreatedAt())
                    .build());
        });

        return PageResponse.<ProjectResponseDTO>builder()
                .content(dtoList)
                .limit(size)
                .offset(page)
                .total((int) listedProject.getTotalElements())
                .build();
    }


    @Override
    public ProjectResponseDTO getProjectDetailClient(BigInteger id) {
        ProjectInterfaceDTO projectInterfaceDTO = projectRepository.getProjectDetailByProjectId(id);

        if (projectInterfaceDTO == null) {
            throw new AppException(ErrorCode.PROJECT_NOT_EXISTED);
        }

        Project project = projectRepository.findById(projectInterfaceDTO.getProjectId())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));

        return ProjectResponseDTO.builder()
                .projectId(project.getProjectId())
                .code(project.getCode())
                .title(project.getTitle())
                .slug(project.getSlug())
                .campaign(project.getCampaign() != null ? CampaignResponseDTO.builder()
                        .campaignId(project.getCampaign().getCampaignId())
                        .title(project.getCampaign().getTitle())
                        .build() : null)
                .background(project.getBackground())
                .address(project.getAddress())
                .ward(project.getWard())
                .district(project.getDistrict())
                .totalDonation(projectInterfaceDTO.getTotalDonation())
                .province(project.getProvince())
                .constructions(project.getConstructions() != null ? project.getConstructions().stream().map(construction -> ConstructionResponseDTO.builder()
                        .constructionId(construction.getConstructionId())
                        .title(construction.getTitle())
                        .quantity(construction.getQuantity())
                        .unit(construction.getUnit())
                        .note(construction.getNote())
                        .build()).collect(Collectors.toList()) : Collections.emptyList())
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .totalBudget(project.getTotalBudget())
                .amountNeededToRaise(project.getAmountNeededToRaise())
                .images(project.getProjectImages() != null ? project.getProjectImages().stream().map(projectImage -> ProjectImageDTO.builder()
                        .image(projectImage.getImage())
                        .build()).collect(Collectors.toList()) : Collections.emptyList())
                .build();
    }

    @Override
    public List<CampaignProjectsDTO> getProjectsByStatus() {
        List<Project> projects = projectRepository.findProjectByStatus(BigInteger.valueOf(2));

        Map<Campaign, List<Project>> groupedByCampaign = projects.stream()
                .collect(Collectors.groupingBy(Project::getCampaign));

        return groupedByCampaign.entrySet().stream()
                .map(entry -> {

                    Campaign campaign = entry.getKey();

                    List<ProjectResponseDTO> projectDTOs = entry.getValue().stream()
                            .map(project -> ProjectResponseDTO.builder()
                                    .projectId(project.getProjectId())
                                    .title(project.getTitle())
                                    .build())
                            .collect(Collectors.toList());

                    return CampaignProjectsDTO.builder()
                            .campaignId(campaign.getCampaignId())
                            .title(campaign.getTitle())
                            .projects(projectDTOs)
                            .build();
                })
                .collect(Collectors.toList());
    }
}

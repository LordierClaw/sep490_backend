package vn.com.fpt.sep490_g28_summer2024_be.service.tracking;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;

import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.tracking.GroupedTrackingImageDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.tracking.TrackingDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.tracking.TrackingImageDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.*;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseServiceImpl;
import vn.com.fpt.sep490_g28_summer2024_be.mapper.Mapper;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.TrackingImageRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.TrackingRepository;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingServiceImpl implements TrackingService {

    private final FirebaseServiceImpl firebaseService;
    private final TrackingRepository trackingRepository;
    private final TrackingImageRepository trackingImageRepository;
    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;


    @Override
    @Transactional
    public TrackingDTO addTracking(TrackingDTO trackingDTO, MultipartFile[] newImages) {
        try {
            Project project = projectRepository.findById(trackingDTO.getProject().getProjectId())
                    .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));

            //Check if the logged-in account is an admin or an employee assigned to the project
            CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername()).orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

            if (!loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin") &&
                    !project.getAssigns().stream().map(Assign::getAccount).toList().contains(loggedAccount)){
                throw new  AppException(ErrorCode.ACCESS_DENIED);
            }

            Tracking tracking = Mapper.mapDtoToEntity(trackingDTO, Tracking.class);
            tracking.setCreatedAt(LocalDateTime.now());
            tracking.setDate(trackingDTO.getDate());
            tracking.setProject(project);
            Tracking savedTracking = trackingRepository.save(tracking);

            if (newImages != null) {
                // Kiểm tra loại và kích thước từng tệp ảnh
                for (MultipartFile image : newImages) {
                    if (image.getContentType() == null || !image.getContentType().startsWith("image/")) {
                        throw new AppException(ErrorCode.HTTP_FILE_IS_NOT_IMAGE);
                    }
                    if (image.getSize() > 2 * 1024 * 1024) { // 2MB
                        throw new AppException(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT);
                    }
                }

                List<String> images = firebaseService.uploadMultipleFile(newImages, savedTracking.getTrackingId(), "project/tracking-images");

                images.forEach(imageUrl -> trackingImageRepository.save(
                        TrackingImage.builder()
                                .tracking(savedTracking)
                                .image(imageUrl)
                                .build()
                ));
            }

            ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                    .projectId(savedTracking.getProject().getProjectId())
                    .title(savedTracking.getProject().getTitle())
                    .build();

            return TrackingDTO.builder()
                    .trackingId(savedTracking.getTrackingId())
                    .title(savedTracking.getTitle())
                    .content(savedTracking.getContent())
                    .date(savedTracking.getDate())
                    .createdAt(savedTracking.getCreatedAt())
                    .updatedAt(savedTracking.getUpdatedAt())
                    .project(projectResponseDTO)
                    .trackingImages(savedTracking.getTrackingImages() != null ? savedTracking.getTrackingImages().stream()
                            .map(trackingImage -> TrackingImageDTO.builder()
                                    .imageUrl(trackingImage.getImage())
                                    .build())
                            .collect(Collectors.toList()) : null)
                    .build();
        } catch (IOException e) {
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }

    @Override
    public TrackingDTO getTrackingById(BigInteger id) {
        Tracking tracking = trackingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HTTP_TRACKING_NOT_FOUND));

        ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                .projectId(tracking.getProject().getProjectId())
                .title(tracking.getProject().getTitle())
                .build();

        return TrackingDTO.builder()
                .trackingId(tracking.getTrackingId())
                .title(tracking.getTitle())
                .content(tracking.getContent())
                .date(tracking.getDate())
                .createdAt(tracking.getCreatedAt())
                .updatedAt(tracking.getUpdatedAt())
                .project(projectResponseDTO)
                .trackingImages(tracking.getTrackingImages().stream()
                        .map(trackingImage -> TrackingImageDTO.builder()
                                .imageUrl(trackingImage.getImage())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional
    public TrackingDTO updateTracking(TrackingDTO trackingDTO, BigInteger id, MultipartFile[] newImages) {
        try {
            Tracking tracking = trackingRepository.findById(id)
                    .orElseThrow(() -> new AppException(ErrorCode.HTTP_TRACKING_NOT_FOUND));

            // Check if the logged-in account is an admin or an employee assigned to the project
            CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername()).orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

            if (!loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin") &&
                    !tracking.getProject().getAssigns().stream().map(Assign::getAccount).toList().contains(loggedAccount)) {
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }

            tracking.setTitle(trackingDTO.getTitle());
            tracking.setContent(trackingDTO.getContent());
            tracking.setDate(trackingDTO.getDate());
            tracking.setUpdatedAt(LocalDateTime.now());

            List<TrackingImage> currentTrackingImages = trackingImageRepository.findByTracking_Id(id);
            if (trackingDTO.getTrackingImages() != null) {
                List<String> remainImageUrls = trackingDTO.getTrackingImages().stream()
                        .map(TrackingImageDTO::getImageUrl)
                        .toList();

                for (TrackingImage currentImage : currentTrackingImages) {
                    if (currentImage != null && !remainImageUrls.contains(currentImage.getImage())) {
                        firebaseService.deleteFileByPath(currentImage.getImage());
                        trackingImageRepository.delete(currentImage);
                    }
                }
                // Flush changes to ensure images are deleted immediately
                trackingImageRepository.flush();
            } else {
                for (TrackingImage currentImage : currentTrackingImages) {
                    if (currentImage != null) {
                        firebaseService.deleteFileByPath(currentImage.getImage());
                        trackingImageRepository.delete(currentImage);
                    }
                }
                // Flush changes to ensure images are deleted immediately
                trackingImageRepository.flush();
            }

            if (newImages != null) {
                for (MultipartFile image : newImages) {
                    if (!image.getContentType().startsWith("image/")) {
                        throw new AppException(ErrorCode.HTTP_FILE_IS_NOT_IMAGE);
                    }
                    if (image.getSize() > 2 * 1024 * 1024) { // 2MB
                        throw new AppException(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT);
                    }
                }
                List<String> images = firebaseService.uploadMultipleFile(newImages, tracking.getTrackingId(), "project/tracking-images");
                images.forEach(imageUrl -> {
                    TrackingImage newTrackingImage = TrackingImage.builder()
                            .tracking(tracking)
                            .image(imageUrl)
                            .build();
                    trackingImageRepository.save(newTrackingImage);
                });
            }

            trackingRepository.save(tracking);
            return TrackingDTO.builder()
                    .trackingId(tracking.getTrackingId())
                    .title(tracking.getTitle())
                    .content(tracking.getContent())
                    .date(tracking.getDate())
                    .createdAt(tracking.getCreatedAt())
                    .updatedAt(tracking.getUpdatedAt())
                    .trackingImages(tracking.getTrackingImages() != null ? tracking.getTrackingImages().stream()
                            .map(trackingImage -> TrackingImageDTO.builder()
                                    .imageUrl(trackingImage.getImage())
                                    .build())
                            .collect(Collectors.toList()) : null)
                    .build();
        } catch (IOException e) {
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }



    @Override
    public PageResponse<TrackingDTO> viewByFilter(Integer page, Integer size, String title, BigInteger projectId) {
        Pageable pageable = PageRequest.of(page, size);
        Project project = projectRepository.findById(projectId).
                orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));

        Page<Tracking> listedTrackings = trackingRepository.findTrackingByFilterAndProjectId(title, projectId, pageable);

        List<TrackingDTO> trackingResponseDTOList = listedTrackings.stream()
                .map(tracking -> {
                    ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                            .projectId(tracking.getProject().getProjectId())
                            .title(tracking.getProject().getTitle())
                            .build();

                    return TrackingDTO.builder()
                            .trackingId(tracking.getTrackingId())
                            .title(tracking.getTitle())
                            .content(tracking.getContent())
                            .date(tracking.getDate())
                            .createdAt(tracking.getCreatedAt())
                            .updatedAt(tracking.getUpdatedAt())
                            .project(projectResponseDTO)
                            .build();
                })
                .toList();

        return PageResponse.<TrackingDTO>builder()
                .content(trackingResponseDTOList)
                .limit(size)
                .offset(page)
                .total((int) listedTrackings.getTotalElements())
                .build();
    }

    @Override
    @Transactional
    public void deleteTracking(BigInteger id) {
        try {
            Tracking tracking = trackingRepository.findById(id)
                    .orElseThrow(() -> new AppException(ErrorCode.HTTP_TRACKING_NOT_FOUND));

            //Check if the logged-in account is an admin or an employee assigned to the project
            CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername()).orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

            if (!loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin") &&
                    !tracking.getProject().getAssigns().stream().map(Assign::getAccount).toList().contains(loggedAccount)){
                throw new  AppException(ErrorCode.ACCESS_DENIED);
            }

            for (TrackingImage trackingImage : tracking.getTrackingImages()) {
                firebaseService.deleteFileByPath(trackingImage.getImage());
                trackingImageRepository.delete(trackingImage);
            }
            trackingRepository.delete(tracking);
        } catch (IOException e) {
            throw new AppException(ErrorCode.DELETE_FILE_FAILED);
        }
    }

    @Override
    public List<GroupedTrackingImageDTO> getImagesByProjectIdAndTitles(BigInteger projectId) {
        List<String> titles = Arrays.asList("Hiện trạng", "Tiến độ", "Hoàn thiện");
        List<Tracking> trackings = trackingRepository.findByProjectIdAndTitles(projectId, titles);

        Map<String, List<String>> groupedImages = new HashMap<>();
        for (Tracking tracking : trackings) {
            String title = tracking.getTitle();
            List<String> images = groupedImages.computeIfAbsent(title, k -> new ArrayList<>());
            for (TrackingImage trackingImage : tracking.getTrackingImages()) {
                images.add(trackingImage.getImage());
            }
        }

        List<GroupedTrackingImageDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : groupedImages.entrySet()) {
            GroupedTrackingImageDTO dto = GroupedTrackingImageDTO.builder()
                    .title(entry.getKey())
                    .imageUrls(entry.getValue())
                    .build();
            result.add(dto);
        }

        return result;
    }
}

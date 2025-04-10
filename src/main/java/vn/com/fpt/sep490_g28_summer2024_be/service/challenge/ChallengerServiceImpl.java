package vn.com.fpt.sep490_g28_summer2024_be.service.challenge;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.AppConfig;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.ChallengeRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.ChallengeResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.interfacedto.ChallengeInterfaceDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.interfacedto.ProjectInterfaceDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.*;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseService;
import vn.com.fpt.sep490_g28_summer2024_be.mapper.Mapper;
import vn.com.fpt.sep490_g28_summer2024_be.repository.*;
import vn.com.fpt.sep490_g28_summer2024_be.utils.CodeUtils;
import vn.com.fpt.sep490_g28_summer2024_be.utils.SlugUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengerServiceImpl implements ChallengeService {

    private final ProjectRepository projectRepository;
    private final ChallengeRepository challengeRepository;
    private final AccountRepository accountRepository;
    private final FirebaseService firebaseService;
    private final CodeUtils codeUtils;
    private final SlugUtils slugUtils;
    private final ChallengeProjectRepository challengeProjectRepository;
    private final DonationRepository donationRepository;
    private  static final BigInteger SYSTEM_USER_ROLE_ID = BigInteger.valueOf(4);

    @Override
    @Transactional
    public ChallengeResponseDTO addChallenge(ChallengeRequestDTO request, String email, MultipartFile thumbnail) {

        if (challengeRepository.existsByTitle(request.getTitle())) {
            throw new AppException(ErrorCode.DUPLICATE_TITLE);
        }

        Account loggedAccount = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

        if (!loggedAccount.getRole().getRoleId().equals(SYSTEM_USER_ROLE_ID)) {
            throw new AppException(ErrorCode.ADMIN_ACCESS_DENIED);
        }

        if (request.getFinishedAt().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.INVALID_FINISH_DATE);
        }

        Challenge challenge = Mapper.mapDtoToEntity(request, Challenge.class);
        challenge.setCreatedAt(LocalDateTime.now());
        challenge.setCreatedBy(loggedAccount);

        Challenge savedChallenge = challengeRepository.save(challenge);

        savedChallenge.setChallengeCode(codeUtils.genCode(AppConfig.CHALLENGE_PREFIX, savedChallenge.getChallengeId()));
        savedChallenge.setSlug(slugUtils.genSlug(String.format("%s %s", savedChallenge.getChallengeCode(), savedChallenge.getTitle())));

        if (thumbnail != null) {
            if (thumbnail.getContentType() == null || !thumbnail.getContentType().startsWith("image/")) {
                throw new AppException(ErrorCode.HTTP_FILE_IS_NOT_IMAGE);
            }

            if (thumbnail.getSize() > 2 * 1024 * 1024) { // 2MB
                throw new AppException(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT);
            }

            try {
                String thumbnailUrl = firebaseService.uploadOneFile(thumbnail,
                        savedChallenge.getChallengeId(), "challenge-images/thumbnail");
                savedChallenge.setThumbnail(thumbnailUrl);
            } catch (IOException e) {
                throw new AppException(ErrorCode.UPLOAD_FAILED);
            }
        }

        List<ChallengeProject> challengeProjects;

        if (request.getProjects() != null) {
            challengeProjects = request.getProjects().stream()
                    .map(projectRequest -> {

                        Project project = projectRepository.findById(projectRequest.getProjectId())
                                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));

                        return ChallengeProject.builder()
                                .challenge(savedChallenge)
                                .project(project)
                                .build();
                    })
                    .collect(Collectors.toList());
        } else {

            challengeProjects = projectRepository.findProjectByStatus(BigInteger.valueOf(2)).stream()
                    .map(project -> ChallengeProject.builder()
                            .challenge(savedChallenge)
                            .project(project)
                            .build())
                    .collect(Collectors.toList());
        }

        challengeProjectRepository.saveAll(challengeProjects);
        savedChallenge.setChallengeProjects(challengeProjects);

        challengeRepository.save(savedChallenge);

        return ChallengeResponseDTO.builder()
                .challengeId(savedChallenge.getChallengeId())
                .challengeCode(savedChallenge.getChallengeCode())
                .title(savedChallenge.getTitle())
                .slug(savedChallenge.getSlug())
                .thumbnail(savedChallenge.getThumbnail())
                .content(savedChallenge.getContent())
                .goal(savedChallenge.getGoal())
                .createdBy(savedChallenge.getCreatedBy() == null ? null : AccountDTO.builder()
                        .accountId(savedChallenge.getCreatedBy().getAccountId())
                        .fullname(savedChallenge.getCreatedBy().getFullname())
                        .build())
                .createdAt(savedChallenge.getCreatedAt())
                .finishedAt(savedChallenge.getFinishedAt())
                .build();
    }

    @Override
    public ChallengeResponseDTO updateChallenge(ChallengeRequestDTO request, BigInteger id, MultipartFile thumbnail, String email) {
        try {
            Challenge challenge = challengeRepository.findById(id).orElseThrow(() ->
                    new AppException(ErrorCode.CHALLENGE_NOT_FOUND));

            Account loggedAccount = accountRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

            //check createdBy
            if (!challenge.getCreatedBy().equals(loggedAccount)) {
                throw new AppException(ErrorCode.HTTP_UNAUTHORIZED);
            }

            if (challengeRepository.countChallengeByTitle(request.getTitle(), id) > 0) {
                throw new AppException(ErrorCode.DUPLICATE_TITLE);
            }


            if (challenge.getFinishedAt().isBefore(LocalDate.now())) {
                throw new AppException(ErrorCode.CHALLENGE_ALREADY_FINISHED);
            }

            if (request.getFinishedAt().isBefore(LocalDate.now())) {
                throw new AppException(ErrorCode.INVALID_FINISH_DATE);
            }


            challenge.setTitle(request.getTitle());
            challenge.setContent(request.getContent());
            challenge.setGoal(request.getGoal());
            challenge.setFinishedAt(request.getFinishedAt());

            if (request.getThumbnail() == null || request.getThumbnail().isBlank()) {
                if (challenge.getThumbnail() != null) {
                    firebaseService.deleteFileByPath(challenge.getThumbnail());
                }
                challenge.setThumbnail(null);
            }



            if (thumbnail != null) {
                if (thumbnail.getContentType() == null || !thumbnail.getContentType().startsWith("image/")) {
                    throw new AppException(ErrorCode.HTTP_FILE_IS_NOT_IMAGE);
                }

                if (thumbnail.getSize() > 2 * 1024 * 1024) { // 2MB
                    throw new AppException(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT);
                }

                String fileName = firebaseService.uploadOneFile(thumbnail, id, "challenge-images/thumbnail");
                challenge.setThumbnail(fileName);
            }

            Challenge savedChallenge = challengeRepository.save(challenge);
            return ChallengeResponseDTO.builder()
                    .challengeId(savedChallenge.getChallengeId())
                    .title(savedChallenge.getTitle())
                    .challengeCode(savedChallenge.getChallengeCode())
                    .thumbnail(savedChallenge.getThumbnail())
                    .content(savedChallenge.getContent())
                    .goal(savedChallenge.getGoal())
                    .createdAt(savedChallenge.getCreatedAt())
                    .finishedAt(savedChallenge.getFinishedAt())
                    .build();
        } catch (IOException e) {
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }


    @Override
    public PageResponse<ChallengeResponseDTO> viewActiveChallengesByFilter(Integer page, Integer size, String accountCode) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChallengeInterfaceDTO> challengePage = challengeRepository.findOngoingChallengesByAccountCode(accountCode, pageable);

        List<ChallengeResponseDTO> activeChallenges = challengePage.stream()
                .map(this::mapToChallengeResponseDTO)
                .collect(Collectors.toList());

        BigDecimal totalActiveDonations = activeChallenges.stream()
                .map(dto -> Optional.ofNullable(dto.getTotalDonation()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PageResponse.<ChallengeResponseDTO>builder()
                .content(activeChallenges)
                .limit(size)
                .summary(Map.of("total_donation", totalActiveDonations))
                .offset(page)
                .total((int) challengePage.getTotalElements())
                .build();
    }

    @Override
    public PageResponse<ChallengeResponseDTO> viewExpiredChallengesByFilter(Integer page, Integer size, String accountCode) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChallengeInterfaceDTO> challengePage = challengeRepository.findExpiredChallengesByAccountCode(accountCode, pageable);

        List<ChallengeResponseDTO> expiredChallenges = challengePage.stream()
                .map(this::mapToChallengeResponseDTO)
                .collect(Collectors.toList());

        BigDecimal totalExpiredDonations = expiredChallenges.stream()
                .map(dto -> Optional.ofNullable(dto.getTotalDonation()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PageResponse.<ChallengeResponseDTO>builder()
                .content(expiredChallenges)
                .limit(size)
                .summary(Map.of("total_donation", totalExpiredDonations))
                .offset(page)
                .total((int) challengePage.getTotalElements())
                .build();
    }

    @Override
    public PageResponse<ChallengeResponseDTO> viewChallengesAdminByFilter(Integer page, Integer size, String title, Integer year, BigDecimal minDonation, BigDecimal maxDonation) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChallengeInterfaceDTO> challengePage = challengeRepository.findChallengesByFilters(title, year, minDonation, maxDonation, pageable);

        List<ChallengeResponseDTO> challengeResponseDTOS = challengePage.stream()
                .map(this::mapToChallengeResponseDTO)
                .collect(Collectors.toList());


        return PageResponse.<ChallengeResponseDTO>builder()
                .content(challengeResponseDTOS)
                .limit(size)
                .offset(page)
                .total((int) challengePage.getTotalElements())
                .build();
    }


    @Transactional
    public ChallengeResponseDTO mapToChallengeResponseDTO(ChallengeInterfaceDTO challenge) {

        var projects = challengeProjectRepository.findByChallenge(Challenge.builder()
                        .challengeId(challenge.getChallengeId())
                .build()).stream().map(challengeProject -> ProjectResponseDTO.builder()
                        .projectId(challengeProject.getProject().getProjectId())
                        .code(challengeProject.getProject().getCode())
                        .title(challengeProject.getProject().getTitle())
                .build()).toList();

        return ChallengeResponseDTO.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .slug(challenge.getSlug())
                .challengeCode(challenge.getChallengeCode())
                .thumbnail(challenge.getThumbnail())
                .content(challenge.getContent())
                .goal(challenge.getGoal())
                .createdBy(challenge.getAccountId() == null ? null : AccountDTO.builder()
                        .accountId(challenge.getAccountId())
                        .code(challenge.getCode())
                        .fullname(challenge.getFullname())
                        .build())
                .createdAt(challenge.getCreatedAt())
                .finishedAt(challenge.getFinishedAt())
                .totalDonation(challenge.getTotalDonation())
                .projectResponseDTOS(projects)
                .build();
    }

    @Override
    public ChallengeResponseDTO getChallengeById(BigInteger id) {

        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHALLENGE_NOT_FOUND));

        BigDecimal totalDonation = donationRepository.sumDonationsByChallengeId(id);

        List<ProjectResponseDTO> projectResponseDTOS = challenge.getChallengeProjects().stream()
                .map(cp -> {
                    ProjectInterfaceDTO projectInterfaceDTO = projectRepository.getProjectDetailByProjectId(cp.getProject().getProjectId());
                    return ProjectResponseDTO.builder()
                            .projectId(cp.getProject().getProjectId())
                            .code(cp.getProject().getCode())
                            .amountNeededToRaise(cp.getProject().getAmountNeededToRaise())
                            .title(cp.getProject().getTitle())
                            .totalDonation(projectInterfaceDTO.getTotalDonation())
                            .build();
                })
                .toList();

        return ChallengeResponseDTO.builder()
                .challengeId(challenge.getChallengeId())
                .challengeCode(challenge.getChallengeCode())
                .title(challenge.getTitle())
                .slug(challenge.getSlug())
                .thumbnail(challenge.getThumbnail())
                .content(challenge.getContent())
                .goal(challenge.getGoal())
                .createdBy(challenge.getCreatedBy() == null ? null : AccountDTO.builder()
                        .accountId(challenge.getCreatedBy().getAccountId())
                        .fullname(challenge.getCreatedBy().getFullname())
                        .avatar(challenge.getCreatedBy().getAvatar())
                        .build())
                .createdAt(challenge.getCreatedAt())
                .finishedAt(challenge.getFinishedAt())
                .totalDonation(totalDonation)
                .projectResponseDTOS(projectResponseDTOS)
                .build();
    }


    @Override
    @Transactional
    public void deleteChallenge(BigInteger id) {
        try {
            Challenge challenge = challengeRepository.findById(id).orElseThrow(() ->
                    new AppException(ErrorCode.CHALLENGE_NOT_FOUND));

            List<ChallengeProject> challengeProjects = challengeProjectRepository.findByChallenge(challenge);
            challengeProjectRepository.deleteAll(challengeProjects);

            if (challenge.getThumbnail() != null && !challenge.getThumbnail().isEmpty()) {
                firebaseService.deleteFileByPath(challenge.getThumbnail());
            }

            challengeRepository.delete(challenge);

        } catch (IOException e) {
            throw new AppException(ErrorCode.DELETE_FILE_FAILED);
        }
    }

    @Override
    public List<ChallengeResponseDTO> getTopChallenges(Integer number) {
        var listChallenge = challengeRepository.getTopChallenge(number);
        if (listChallenge != null) return listChallenge.stream().map(topChallengeDTO -> ChallengeResponseDTO.builder()
                .challengeId(topChallengeDTO.getChallengeId())
                .challengeCode(topChallengeDTO.getChallengeCode())
                .title(topChallengeDTO.getTitle())
                .slug(topChallengeDTO.getSlug())
                .thumbnail(topChallengeDTO.getThumbnail())
                .content(topChallengeDTO.getContent())
                .goal(topChallengeDTO.getGoal())
                .createdAt(topChallengeDTO.getCreatedAt())
                .finishedAt(topChallengeDTO.getFinishedAt())
                .totalDonation(topChallengeDTO.getTotalDonation())
                .createdBy(AccountDTO.builder()
                        .accountId(topChallengeDTO.getAccountId())
                        .code(topChallengeDTO.getCode())
                        .fullname(topChallengeDTO.getFullname())
                        .avatar(topChallengeDTO.getAvatar())
                        .build())
                .build()).toList();
        return Collections.emptyList();
    }

    @Override
    public PageResponse<?> getChallenges(Integer page, Integer size, String title, String fullname, BigDecimal min, BigDecimal max) {
        Pageable pageable = PageRequest.of(page, size);
        var list = challengeRepository.getChallenges(title, fullname, min, max, pageable);
        var res = list.map(challenge -> ChallengeResponseDTO.builder()
                .challengeId(challenge.getChallengeId())
                .challengeCode(challenge.getChallengeCode())
                .title(challenge.getTitle())
                .slug(challenge.getSlug())
                .thumbnail(challenge.getThumbnail())
                .content(challenge.getContent())
                .goal(challenge.getGoal())
                .createdAt(challenge.getCreatedAt())
                .finishedAt(challenge.getFinishedAt())
                .totalDonation(challenge.getTotalDonation())
                .createdBy(AccountDTO.builder()
                        .accountId(challenge.getAccountId())
                        .code(challenge.getCode())
                        .fullname(challenge.getFullname())
                        .avatar(challenge.getAvatar())
                        .build())
                .build()).toList();
        return PageResponse.<ChallengeResponseDTO>builder()
                .limit(size)
                .offset(page)
                .total((int) list.getTotalElements())
                .content(res)
                .build();
    }
}

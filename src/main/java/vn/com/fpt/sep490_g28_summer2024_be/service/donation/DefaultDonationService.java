package vn.com.fpt.sep490_g28_summer2024_be.service.donation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.ChallengeResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.donation.DonationResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.interfacedto.ProjectDonationInformattionDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Donation;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.DonationRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultDonationService implements DonationService {

    private final DonationRepository donationRepository;
    private final AccountRepository accountRepository;
    private final ProjectRepository projectRepository;

    @Override
    public PageResponse<DonationResponseDTO> viewListDonations(Integer page, Integer size, BigInteger projectId, String description) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Donation> listDonations = donationRepository.findDonationsByFilters(projectId, description, pageable);

        ProjectDonationInformattionDTO projectDonationInformattionDTO = projectRepository.getDonationInformationTotal(projectId);

        List<DonationResponseDTO> donationResponseDTOS = listDonations.stream().map(donation -> DonationResponseDTO.builder()
                .donationId(donation.getDonationId())
                .id(donation.getId())
                .tid(donation.getTid())
                .createdAt(donation.getCreatedAt())
                .value(donation.getValue())
                .description(donation.getDescription())
                .bankSubAccId(donation.getBankSubAccId())
                .bankName(donation.getBankName())
                .corresponsiveName(donation.getCorresponsiveName())
                .corresponsiveAccount(donation.getCorresponsiveAccount())
                .corresponsiveBankId(donation.getCorresponsiveBankId())
                .corresponsiveBankName(donation.getCorresponsiveBankName())
                .note(donation.getNote())
                .project(donation.getProject() == null ? null : ProjectResponseDTO.builder()
                        .projectId(donation.getProject().getProjectId())
                        .title(donation.getProject().getTitle())
                        .slug(donation.getProject().getSlug())
                        .code(donation.getProject().getCode())
                        .build())
                .transferredProject(donation.getTransferredProject() == null ? null : ProjectResponseDTO.builder()
                        .projectId(donation.getTransferredProject().getProjectId())
                        .title(donation.getTransferredProject().getTitle())
                        .slug(donation.getTransferredProject().getSlug())
                        .code(donation.getTransferredProject().getCode())
                        .build())
                .refer(donation.getRefer() == null ? null : AccountDTO.builder()
                        .accountId(donation.getRefer().getAccountId())
                        .fullname(donation.getRefer().getFullname())
                        .build())
                .challenge(donation.getChallenge() == null ? null : ChallengeResponseDTO.builder()
                        .challengeId(donation.getChallenge().getChallengeId())
                        .title(donation.getChallenge().getTitle())
                        .build())
                .createdBy(donation.getCreatedBy() == null ? null : AccountDTO.builder()
                        .accountId(donation.getCreatedBy().getAccountId())
                        .fullname(donation.getCreatedBy().getFullname())
                        .build())
                .status(donation.getWrongDonation() != null ? "Pending" : "")
                .build()).toList();

        return PageResponse.<DonationResponseDTO>builder()
                .limit(size)
                .offset(page)
                .total((int) listDonations.getTotalElements())
                .content(donationResponseDTOS)
                .summary(Map.of("target", projectDonationInformattionDTO.getTarget(),
                        "total_donation", projectDonationInformattionDTO.getTotalDonation()))
                .build();
    }

    @Override
    public PageResponse<DonationResponseDTO> viewListDonationsAdmin(Integer page, Integer size, BigInteger projectId, String description) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Donation> listDonations = donationRepository.findDonationsAdminByFilters(projectId, description, pageable);

        List<DonationResponseDTO> donationResponseDTOS = listDonations.stream().map(donation -> DonationResponseDTO.builder()
                .donationId(donation.getDonationId())
                .id(donation.getId())
                .tid(donation.getTid())
                .createdAt(donation.getCreatedAt())
                .value(donation.getValue())
                .description(donation.getDescription())
                .bankSubAccId(donation.getBankSubAccId())
                .bankName(donation.getBankName())
                .corresponsiveName(donation.getCorresponsiveName())
                .corresponsiveAccount(donation.getCorresponsiveAccount())
                .corresponsiveBankId(donation.getCorresponsiveBankId())
                .corresponsiveBankName(donation.getCorresponsiveBankName())
                .note(donation.getNote())
                .project(donation.getProject() == null ? null : ProjectResponseDTO.builder()
                        .projectId(donation.getProject().getProjectId())
                        .title(donation.getProject().getTitle())
                        .slug(donation.getProject().getSlug())
                        .code(donation.getProject().getCode())
                        .build())
                .transferredProject(donation.getTransferredProject() == null ? null : ProjectResponseDTO.builder()
                        .projectId(donation.getTransferredProject().getProjectId())
                        .title(donation.getTransferredProject().getTitle())
                        .slug(donation.getTransferredProject().getSlug())
                        .code(donation.getTransferredProject().getCode())
                        .build())
                .refer(donation.getRefer() == null ? null : AccountDTO.builder()
                        .accountId(donation.getRefer().getAccountId())
                        .fullname(donation.getRefer().getFullname())
                        .build())
                .challenge(donation.getChallenge() == null ? null : ChallengeResponseDTO.builder()
                        .challengeId(donation.getChallenge().getChallengeId())
                        .title(donation.getChallenge().getTitle())
                        .build())
                .createdBy(donation.getCreatedBy() == null ? null : AccountDTO.builder()
                        .accountId(donation.getCreatedBy().getAccountId())
                        .fullname(donation.getCreatedBy().getFullname())
                        .build())
                .status(donation.getWrongDonation() != null ? "Pending" : "")
                .build()).toList();

        return PageResponse.<DonationResponseDTO>builder()
                .limit(size)
                .offset(page)
                .total((int) listDonations.getTotalElements())
                .content(donationResponseDTOS)
                .build();
    }

    @Override
    public PageResponse<DonationResponseDTO> viewDonationsByChallengeId(Integer page, Integer size, BigInteger challengeId, String description) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Donation> donationsPage = donationRepository.findDonationsByChallengeIdAndDescription(challengeId, description, pageable);

        List<DonationResponseDTO> donationDTOs = donationsPage.getContent().stream()
                .map(donation -> DonationResponseDTO.builder()
                        .donationId(donation.getDonationId())
                        .id(donation.getId())
                        .tid(donation.getTid())
                        .project(donation.getProject() != null ? ProjectResponseDTO.builder()
                                .projectId(donation.getProject().getProjectId())
                                .code(donation.getProject().getCode())
                                .slug(donation.getProject().getSlug())
                                .title(donation.getProject().getTitle())
                                .build() : null)
                        .refer(donation.getRefer() != null ? AccountDTO.builder()
                                .accountId(donation.getRefer().getAccountId())
                                .fullname(donation.getRefer().getFullname())
                                .build() : null)
                        .challenge(donation.getChallenge() != null ? ChallengeResponseDTO.builder()
                                .challengeId(donation.getChallenge().getChallengeId())
                                .challengeCode(donation.getChallenge().getChallengeCode())
                                .title(donation.getChallenge().getTitle())
                                .build() : null)
                        .transferredProject(donation.getTransferredProject() != null ? ProjectResponseDTO.builder()
                                .projectId(donation.getTransferredProject().getProjectId())
                                .code(donation.getTransferredProject().getCode())
                                .slug(donation.getTransferredProject().getSlug())
                                .title(donation.getTransferredProject().getTitle())
                                .build() : null)
                        .createdBy(donation.getCreatedBy() != null ? AccountDTO.builder()
                                .accountId(donation.getCreatedBy().getAccountId())
                                .fullname(donation.getCreatedBy().getFullname())
                                .build() : null)
                        .createdAt(donation.getCreatedAt())
                        .value(donation.getValue())
                        .description(donation.getDescription())
                        .bankSubAccId(donation.getBankSubAccId())
                        .bankName(donation.getBankName())
                        .corresponsiveName(donation.getCorresponsiveName())
                        .corresponsiveAccount(donation.getCorresponsiveAccount())
                        .corresponsiveBankId(donation.getCorresponsiveBankId())
                        .corresponsiveBankName(donation.getCorresponsiveBankName())
                        .note(donation.getNote())
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalDonations = donationDTOs.stream()
                .map(DonationResponseDTO::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        return PageResponse.<DonationResponseDTO>builder()
                .content(donationDTOs)
                .summary(Map.of("total_donation", totalDonations))
                .limit(size)
                .offset(page)
                .total((int) donationsPage.getTotalElements())
                .build();
    }


    @Override
    public PageResponse<DonationResponseDTO> viewDonationsByAccount(Integer page, Integer size, String email, String description) {
        Pageable pageable = PageRequest.of(page, size);

        Account loggedAccount = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

        Page<Donation> donationsPage = donationRepository.findByCreatedByAndDescription(loggedAccount.getAccountId(), description, pageable);

        List<DonationResponseDTO> donationDTOs = donationsPage.getContent().stream()
                .map(donation -> DonationResponseDTO.builder()
                        .donationId(donation.getDonationId())
                        .id(donation.getId())
                        .tid(donation.getTid())
                        .project(donation.getProject() != null ? ProjectResponseDTO.builder()
                                .projectId(donation.getProject().getProjectId())
                                .code(donation.getProject().getCode())
                                .slug(donation.getProject().getSlug())
                                .title(donation.getProject().getTitle())
                                .build() : null)
                        .refer(donation.getRefer() != null ? AccountDTO.builder()
                                .accountId(donation.getRefer().getAccountId())
                                .fullname(donation.getRefer().getFullname())
                                .build() : null)
                        .challenge(donation.getChallenge() != null ? ChallengeResponseDTO.builder()
                                .challengeId(donation.getChallenge().getChallengeId())
                                .challengeCode(donation.getChallenge().getChallengeCode())
                                .title(donation.getChallenge().getTitle())
                                .build() : null)
                        .transferredProject(donation.getTransferredProject() != null ? ProjectResponseDTO.builder()
                                .projectId(donation.getTransferredProject().getProjectId())
                                .code(donation.getTransferredProject().getCode())
                                .slug(donation.getTransferredProject().getSlug())
                                .title(donation.getTransferredProject().getTitle())
                                .build() : null)
                        .createdBy(donation.getCreatedBy() != null ? AccountDTO.builder()
                                .accountId(donation.getCreatedBy().getAccountId())
                                .fullname(donation.getCreatedBy().getFullname())
                                .build() : null)
                        .createdAt(donation.getCreatedAt())
                        .value(donation.getValue())
                        .description(donation.getDescription())
                        .bankSubAccId(donation.getBankSubAccId())
                        .bankName(donation.getBankName())
                        .corresponsiveName(donation.getCorresponsiveName())
                        .corresponsiveAccount(donation.getCorresponsiveAccount())
                        .corresponsiveBankId(donation.getCorresponsiveBankId())
                        .corresponsiveBankName(donation.getCorresponsiveBankName())
                        .note(donation.getNote())
                        .build())
                .collect(Collectors.toList());



        return PageResponse.<DonationResponseDTO>builder()
                .content(donationDTOs)
                .limit(size)
                .offset(page)
                .total((int) donationsPage.getTotalElements())
                .build();
    }

    @Override
    @Transactional
    public PageResponse<?> viewDonationsByReferCode(Integer page, Integer size, String code) {
        Account account = accountRepository.findSystemUserAccountByAccountCode(code).orElseThrow(() ->  new AppException(ErrorCode.ACCOUNT_NO_CONTENT));

        Pageable pageable = PageRequest.of(page, size);
        var listDonations = donationRepository.getDonationsByReferId(null, account.getAccountId(), pageable);

        var listDTO = listDonations.stream().map(donation -> DonationResponseDTO.builder()
                .donationId(donation.getDonationId())
                .id(donation.getId())
                .tid(donation.getTid())
                .project(donation.getProject() != null ? ProjectResponseDTO.builder()
                        .projectId(donation.getProject().getProjectId())
                        .code(donation.getProject().getCode())
                        .slug(donation.getProject().getSlug())
                        .title(donation.getProject().getTitle())
                        .build() : null)
                .refer(donation.getRefer() != null ? AccountDTO.builder()
                        .accountId(donation.getRefer().getAccountId())
                        .fullname(donation.getRefer().getFullname())
                        .build() : null)
                .challenge(donation.getChallenge() != null ? ChallengeResponseDTO.builder()
                        .challengeId(donation.getChallenge().getChallengeId())
                        .challengeCode(donation.getChallenge().getChallengeCode())
                        .title(donation.getChallenge().getTitle())
                        .build() : null)
                .transferredProject(donation.getTransferredProject() != null ? ProjectResponseDTO.builder()
                        .projectId(donation.getTransferredProject().getProjectId())
                        .code(donation.getTransferredProject().getCode())
                        .slug(donation.getTransferredProject().getSlug())
                        .title(donation.getTransferredProject().getTitle())
                        .build() : null)
                .createdBy(donation.getCreatedBy() != null ? AccountDTO.builder()
                        .accountId(donation.getCreatedBy().getAccountId())
                        .fullname(donation.getCreatedBy().getFullname())
                        .build() : null)
                .createdAt(donation.getCreatedAt())
                .value(donation.getValue())
                .description(donation.getDescription())
                .bankSubAccId(donation.getBankSubAccId())
                .bankName(donation.getBankName())
                .corresponsiveName(donation.getCorresponsiveName())
                .corresponsiveAccount(donation.getCorresponsiveAccount())
                .corresponsiveBankId(donation.getCorresponsiveBankId())
                .corresponsiveBankName(donation.getCorresponsiveBankName())
                .note(donation.getNote())
                .status(donation.getWrongDonation() != null ? "Pending" : "")
                .build()).toList();
        
        return PageResponse.<DonationResponseDTO>builder()
                .limit(size)
                .offset(page)
                .total((int) listDonations.getTotalElements())
                .content(listDTO)
                .summary(Map.of("total_donation_by_refer", donationRepository.getTotalDonationByReferId(account.getAccountId())))
                .build();
    }

    @Override
    @Transactional
    public PageResponse<?> viewAllDonations(Integer page, Integer size, String description) {
        Pageable pageable = PageRequest.of(page, size);
        var listDonations = donationRepository.findAllDonationsByFilters(description, pageable);

        List<DonationResponseDTO> list = listDonations.map(donation -> DonationResponseDTO.builder()
                .donationId(donation.getDonationId())
                .id(donation.getId())
                .tid(donation.getTid())
                .project(donation.getProject() == null ? null : ProjectResponseDTO.builder()
                        .projectId(donation.getProject().getProjectId())
                        .code(donation.getProject().getCode())
                        .slug(donation.getProject().getSlug())
                        .title(donation.getProject().getTitle())
                        .build())
                .refer(donation.getRefer() == null ? null : AccountDTO.builder()
                        .accountId(donation.getRefer().getAccountId())
                        .code(donation.getRefer().getCode())
                        .fullname(donation.getRefer().getFullname())
                        .email(donation.getRefer().getEmail())
                        .phone(donation.getRefer().getPhone())
                        .build())
                .challenge(donation.getChallenge() != null ? ChallengeResponseDTO.builder()
                        .challengeId(donation.getChallenge().getChallengeId())
                        .challengeCode(donation.getChallenge().getChallengeCode())
                        .title(donation.getChallenge().getTitle())
                        .build() : null)
                .transferredProject(donation.getTransferredProject() != null ? ProjectResponseDTO.builder()
                        .projectId(donation.getTransferredProject().getProjectId())
                        .code(donation.getTransferredProject().getCode())
                        .slug(donation.getTransferredProject().getSlug())
                        .title(donation.getTransferredProject().getTitle())
                        .build() : null)
                .createdBy(donation.getCreatedBy() != null ? AccountDTO.builder()
                        .accountId(donation.getCreatedBy().getAccountId())
                        .code(donation.getCreatedBy().getCode())
                        .fullname(donation.getCreatedBy().getFullname())
                        .email(donation.getCreatedBy().getEmail())
                        .build() : null)
                .createdAt(donation.getCreatedAt())
                .value(donation.getValue())
                .description(donation.getDescription())
                .bankSubAccId(donation.getBankSubAccId())
                .bankName(donation.getBankName())
                .corresponsiveName(donation.getCorresponsiveName())
                .corresponsiveAccount(donation.getCorresponsiveAccount())
                .corresponsiveBankId(donation.getCorresponsiveBankId())
                .corresponsiveBankName(donation.getCorresponsiveBankName())
                .note(donation.getNote())
                .status(donation.getWrongDonation() != null ? "Pending" : "")
                .build()).toList();

        return PageResponse.<DonationResponseDTO>builder()
                .limit(size)
                .offset(page)
                .total((int) listDonations.getTotalElements())
                .content(list)
                .build();
    }
}

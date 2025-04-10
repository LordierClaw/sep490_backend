package vn.com.fpt.sep490_g28_summer2024_be.service.sponsor;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.sponsor.SponsorRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.sponsor.SponsorResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.sponsor.SponsorUpdateRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Project;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Sponsor;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseService;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.SponsorRepository;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultSponsorService implements SponsorService{

    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;
    private final SponsorRepository sponsorRepository;
    private final FirebaseService firebaseService;


    @Override
    public SponsorResponseDTO addSponsorToProject(SponsorRequestDTO sponsorRequestDTO, BigInteger projectId, MultipartFile contract, MultipartFile logo) {
        CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername()).orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));

        if (project.getAssigns() != null && !project.getAssigns().isEmpty() && !loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin")){
            List<BigInteger> assignList = project.getAssigns().stream().map(assign -> assign.getAccount().getAccountId()).toList();
            if(!assignList.contains(loggedAccount.getAccountId())) throw new AppException(ErrorCode.HTTP_FORBIDDEN);
        }

        Sponsor sponsor = Sponsor.builder()
                .companyName(sponsorRequestDTO.getCompanyName())
                .businessField(sponsorRequestDTO.getBusinessField())
                .project(project)
                .representative(sponsorRequestDTO.getRepresentative())
                .representativeEmail(sponsorRequestDTO.getRepresentativeEmail())
                .phoneNumber(sponsorRequestDTO.getPhoneNumber())
                .value(new BigDecimal(sponsorRequestDTO.getValue()))
                .note(sponsorRequestDTO.getNote())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        sponsorRepository.save(sponsor);

        //update project
        BigDecimal totalSponsor = project.getSponsors().stream().map(Sponsor::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal newAmountNeededToRaise = project.getTotalBudget().subtract(totalSponsor)
                .compareTo(new BigDecimal(0)) <= 0 ? new BigDecimal(0) : project.getTotalBudget().subtract(totalSponsor);
        project.setAmountNeededToRaise(newAmountNeededToRaise);
        projectRepository.save(project);

        //upload file
        if(contract != null && contract.getSize() != 0){
            try {
                String contractPath = firebaseService.uploadOneFile(contract, sponsor.getSponsorId(), "project/sponsors");
                sponsor.setContract(contractPath);
                sponsorRepository.save(sponsor);
            }catch (IOException e){
                throw new AppException(ErrorCode.UPLOAD_FAILED);
            }
        }

        if(logo != null && logo.getSize() != 0){
            try {
                String logoPath = firebaseService.uploadOneFile(logo, sponsor.getSponsorId(), "project/sponsors/logo");
                sponsor.setLogo(logoPath);
                sponsorRepository.save(sponsor);
            }catch (IOException e){
                throw new AppException(ErrorCode.UPLOAD_FAILED);
            }
        }

        return SponsorResponseDTO.builder()
                .sponsorId(sponsor.getSponsorId())
                .build();
    }

    @Override
    public SponsorResponseDTO viewDetail(BigInteger sponsorId) {
        Sponsor sponsor = sponsorRepository.findById(sponsorId).orElseThrow(() -> new AppException(ErrorCode.SPONSOR_NOT_EXIST));
        return SponsorResponseDTO.builder()
                .sponsorId(sponsor.getSponsorId())
                .companyName(sponsor.getCompanyName())
                .businessField(sponsor.getBusinessField())
                .representative(sponsor.getRepresentative())
                .representativeEmail(sponsor.getRepresentativeEmail())
                .logo(sponsor.getLogo())
                .phoneNumber(sponsor.getPhoneNumber())
                .value(sponsor.getValue().toString())
                .note(sponsor.getNote())
                .contract(sponsor.getContract())
                .build();
    }

    @Override
    public PageResponse<?> viewListSponsorInProject(Integer page, Integer size, String companyName, BigInteger projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));
        Pageable pageable = PageRequest.of(page, size);

        Page<Sponsor> sponsors = sponsorRepository.findSponsorsByFilters(companyName, project.getProjectId(), pageable);

        List<SponsorResponseDTO> sponsorResponseDTOList = sponsors.map(sponsor -> SponsorResponseDTO.builder()
                .sponsorId(sponsor.getSponsorId())
                .companyName(sponsor.getCompanyName())
                .representative(sponsor.getRepresentative())
                .representativeEmail(sponsor.getRepresentativeEmail())
                .logo(sponsor.getLogo())
                .value(sponsor.getValue().toString())
                .createdAt(sponsor.getCreatedAt())
                .updatedAt(sponsor.getUpdatedAt())
                .build()).toList();

        return PageResponse.<SponsorResponseDTO>builder()
                .content(sponsorResponseDTOList)
                .limit(size)
                .offset(page)
                .total((int) sponsors.getTotalElements())
                .build();
    }

    @Override
    public SponsorResponseDTO update(BigInteger sponsorId, SponsorUpdateRequestDTO requestDTO, MultipartFile file, MultipartFile logo) {
        CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername()).orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

        Sponsor sponsor = sponsorRepository.findById(sponsorId).orElseThrow(() -> new AppException(ErrorCode.SPONSOR_NOT_EXIST));

        if (sponsor.getProject().getAssigns() != null && !sponsor.getProject().getAssigns().isEmpty() && !loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin")){
            Project project = sponsor.getProject();
            List<BigInteger> assignList = project.getAssigns().stream().map(assign -> assign.getAccount().getAccountId()).toList();
            if(!assignList.contains(loggedAccount.getAccountId())) throw new AppException(ErrorCode.HTTP_FORBIDDEN);
        }

        if ((requestDTO.getContract().isEmpty()) && file.isEmpty()){
            throw new AppException(ErrorCode.CONTRACT_NOT_NULL);
        }

        //update information about sponsor
        sponsor.setCompanyName(requestDTO.getCompanyName());
        sponsor.setBusinessField(requestDTO.getBusinessField());
        sponsor.setRepresentative(requestDTO.getRepresentative());
        sponsor.setRepresentativeEmail(requestDTO.getRepresentativeEmail());
        sponsor.setPhoneNumber(requestDTO.getPhoneNumber());
        sponsor.setNote(requestDTO.getNote());
        sponsor.setValue(new BigDecimal(requestDTO.getValue()));
        sponsorRepository.save(sponsor);

        //update value
        Project project = projectRepository.findById(sponsor.getProject().getProjectId()).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));
        BigDecimal newTotalSponsor = project.getSponsors().stream().map(Sponsor::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);

        project.setAmountNeededToRaise(project.getTotalBudget().subtract(newTotalSponsor)
                .compareTo(new BigDecimal(0)) <= 0 ? new BigDecimal(0) : project.getTotalBudget().subtract(newTotalSponsor));
        if(project.getAmountNeededToRaise().compareTo(new BigDecimal(0)) <= 0) project.setStatus(3);
        projectRepository.save(project);

        //update file contract
        if (requestDTO.getContract() == null || requestDTO.getContract().isEmpty()){
            try {
                if (sponsor.getContract() != null){
                    firebaseService.deleteFileByPath(sponsor.getContract());
                }
                sponsor.setContract(null);
            } catch (IOException e) {
                throw new AppException(ErrorCode.UPLOAD_FAILED);
            }
        }

        if(file != null && !file.isEmpty()){
            try {
                String fileString = firebaseService.uploadOneFile(file, sponsorId, "project/sponsor");
                sponsor.setContract(fileString);
            } catch (IOException e) {
                throw new AppException(ErrorCode.UPLOAD_FAILED);
            }
        }

        //update file logo
        if (requestDTO.getLogo() == null || requestDTO.getLogo().isEmpty()){
            try {
                if (sponsor.getLogo() != null){
                    firebaseService.deleteFileByPath(sponsor.getLogo());
                }
                sponsor.setLogo(null);
            } catch (IOException e) {
                throw new AppException(ErrorCode.UPLOAD_FAILED);
            }
        }


        if(logo != null && !logo.isEmpty()){
            try {
                String logoString = firebaseService.uploadOneFile(logo, sponsorId, "project/sponsor/logo");
                sponsor.setLogo(logoString);
            } catch (IOException e) {
                throw new AppException(ErrorCode.UPLOAD_FAILED);
            }
        }
        
        sponsor.setUpdatedAt(LocalDateTime.now());
        sponsorRepository.save(sponsor);

        return SponsorResponseDTO.builder()
                .sponsorId(sponsor.getSponsorId())
                .companyName(sponsor.getCompanyName())
                .build();
    }


}

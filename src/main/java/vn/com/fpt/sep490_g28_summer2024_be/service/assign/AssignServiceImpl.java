package vn.com.fpt.sep490_g28_summer2024_be.service.assign;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.assign.AssignResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Assign;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Project;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AssignRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class AssignServiceImpl implements AssignService {
    private final AccountRepository accountRepository;
    private final AssignRepository assignRepository;
    private final ProjectRepository projectRepository;

    @Override
    public PageResponse<AssignResponseDTO> viewMemberInProjectByFilter(Integer page, Integer size, BigInteger projectId, BigInteger roleId, String email, String name) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Assign> assignPage = assignRepository.findMembersInProject(projectId, roleId, email, name, pageable);

        List<AssignResponseDTO> assignResponseDTOS = assignPage.stream()
                .map(assign -> AssignResponseDTO.builder()
                        .assignId(assign.getAssignId())
                        .accountDTO(AccountDTO.builder()
                                .accountId(assign.getAccount().getAccountId())
                                .email(assign.getAccount().getEmail())
                                .phone(assign.getAccount().getPhone())
                                .dob(assign.getAccount().getDob())
                                .fullname(assign.getAccount().getFullname())
                                .build())
                        .build())
                .toList();


        return PageResponse.<AssignResponseDTO>builder()
                .limit(size)
                .offset(page)
                .total((int) assignPage.getTotalElements())
                .content(assignResponseDTOS)
                .build();
    }

    @Override
    public List<AccountDTO> viewMembersNotAssignedToProject(BigInteger id) {
        projectRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));
        List<Account> accounts = accountRepository.findActiveAccountsNotAssignedToProject(id);

        return accounts.stream()
                .map(account -> AccountDTO.builder()
                        .accountId(account.getAccountId())
                        .email(account.getEmail())
                        .build())
                .toList();
    }


    @Override
    public List<Assign> addMembersToProject(List<BigInteger> accountIds, BigInteger projectId) {
        CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername()).orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

        Project project = projectRepository.findById(projectId).
                orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));

        List<Account> accounts = accountRepository.findAllById(accountIds);

        if (!loggedAccount.getRole().getRoleName().equalsIgnoreCase("Admin")) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        
        for (Account account : accounts) {
            if (!account.getRole().getRoleName().equalsIgnoreCase("project manager")) {
                throw new AppException(ErrorCode.ROLE_MEMBER_NOT_VALID);
            }
        }

        List<Assign> existingAssignments = assignRepository.findByProject_ProjectIdAndAccount_AccountIdIn(projectId, accountIds);
        if (!existingAssignments.isEmpty()) {
            throw new AppException(ErrorCode.MEMBER_ALREADY_ASSIGNED);
        }

        List<Assign> assigns = accounts.stream()
                .map(account -> Assign.builder()
                        .account(account)
                        .project(project)
                        .createdBy(loggedAccount)
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();
        return assignRepository.saveAll(assigns);
    }

    @Override
    public void removeMember(BigInteger assignId) {
        Assign assign = assignRepository.findById(assignId).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NO_CONTENT));
        assignRepository.delete(assign);
    }
}

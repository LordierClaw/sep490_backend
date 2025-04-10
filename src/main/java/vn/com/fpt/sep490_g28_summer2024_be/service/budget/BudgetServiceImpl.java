package vn.com.fpt.sep490_g28_summer2024_be.service.budget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.budget.BudgetRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.budget.BudgetResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.entity.*;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.BudgetRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {
    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;
    private final BudgetRepository budgetRepository;

    @Override
    public PageResponse<BudgetResponseDTO> viewBudgetByFilter(Integer page, Integer size, String title, BigInteger projectId) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Budget> listedBudgets = budgetRepository.findBudgetByFilterAndProjectId(title, projectId, pageable);
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));

        List<BudgetResponseDTO> budgetResponseDTOS = listedBudgets.stream()
                .map(budget -> BudgetResponseDTO.builder()
                        .budgetId(budget.getBudgetId())
                        .title(budget.getTitle())
                        .unitPrice(budget.getUnitPrice())
                        .note(budget.getNote())
                        .status(budget.getStatus())
                        .createdAt(budget.getCreatedAt())
                        .updatedAt(budget.getUpdatedAt())
                        .totalBudget(project.getTotalBudget())
                        .build())
                .collect(Collectors.toList());

        return PageResponse.<BudgetResponseDTO>builder()
                .limit(size)
                .offset(page)
                .total((int) listedBudgets.getTotalElements())
                .content(budgetResponseDTOS)
                .build();
    }

    @Override
    public BudgetResponseDTO getBudgetById(BigInteger budgetId) {
        Optional<Budget> budgetOptional = budgetRepository.findById(budgetId);
        Budget budget = budgetOptional.orElseThrow(() -> new AppException(ErrorCode.BUDGET_NOT_FOUND));
        BudgetResponseDTO budgetResponseDTO = BudgetResponseDTO.builder()
                .budgetId(budget.getBudgetId())
                .title(budget.getTitle())
                .unitPrice(budget.getUnitPrice())
                .note(budget.getNote())
                .status(budget.getStatus())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();

        return budgetResponseDTO;
    }


    @Override
    @Transactional
    public List<BudgetResponseDTO> addBudgetsToProject(List<BudgetRequestDTO> budgetRequestDTOs, BigInteger projectId) {
        CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername()).orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));
        Project project = projectRepository.findById(projectId).
                orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));

        //Check if the logged-in account is an admin or an employee assigned to the project
        if (!loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin") &&
                !project.getAssigns().stream().map(Assign::getAccount).toList().contains(loggedAccount)){
            throw new  AppException(ErrorCode.ACCESS_DENIED);
        }

        List<BudgetResponseDTO> budgetResponseDTOs = new ArrayList<>();
        BigDecimal totalBudget = project.getTotalBudget(); // Lấy giá trị totalBudget hiện tại
        BigDecimal amountNeededToRaise = project.getAmountNeededToRaise();

        for (BudgetRequestDTO budgetRequestDTO : budgetRequestDTOs) {
            Budget budget = new Budget();
            budget.setTitle(budgetRequestDTO.getTitle());
            budget.setUnitPrice(new BigDecimal(budgetRequestDTO.getUnitPrice()));
            budget.setNote(budgetRequestDTO.getNote());
            budget.setStatus(loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin") ? 2 : 1);
            budget.setProject(project);
            budget.setCreatedAt(LocalDateTime.now());
            budget = budgetRepository.save(budget);

            totalBudget = totalBudget.add(budget.getUnitPrice()); // Cập nhật totalBudget
            project.setTotalBudget(totalBudget);
            amountNeededToRaise = amountNeededToRaise.add(budget.getUnitPrice()); // Cap nhat so tien can quyen gop
            project.setAmountNeededToRaise(amountNeededToRaise);
            projectRepository.save(project);


            BudgetResponseDTO budgetResponseDTO = BudgetResponseDTO.builder()
                    .budgetId(budget.getBudgetId())
                    .title(budget.getTitle())
                    .unitPrice(budget.getUnitPrice())
                    .note(budget.getNote())
                    .status(budget.getStatus())
                    .createdAt(budget.getCreatedAt())
                    .build();

            budgetResponseDTOs.add(budgetResponseDTO);
        }
        return budgetResponseDTOs;
    }


    @Override
    @Transactional
    public BudgetResponseDTO updateBudget(BigInteger budgetId, BudgetRequestDTO budgetRequestDTO) {
        CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new AppException(ErrorCode.BUDGET_NOT_FOUND));

        //Check if the logged-in account is an admin or an employee assigned to the project
        if (!loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin") &&
                !budget.getProject().getAssigns().stream().map(Assign::getAccount).toList().contains(loggedAccount)){
            throw new  AppException(ErrorCode.ACCESS_DENIED);
        }

        Project project = budget.getProject();
        BigDecimal oldUnitPrice = budget.getUnitPrice();
        BigDecimal newUnitPrice = new BigDecimal(budgetRequestDTO.getUnitPrice());
        BigDecimal amountNeededToRaise = project.getAmountNeededToRaise();
        // Cập nhật budget
        budget.setTitle(budgetRequestDTO.getTitle());
        budget.setUnitPrice(newUnitPrice);
        budget.setNote(budgetRequestDTO.getNote());
        budget.setStatus(loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin") ? 2 : 1);
        budget.setUpdatedAt(LocalDateTime.now());
        budget = budgetRepository.save(budget);

        BigDecimal updatedTotalBudget = project.getTotalBudget().subtract(oldUnitPrice).add(newUnitPrice);
        project.setTotalBudget(updatedTotalBudget);
        amountNeededToRaise = project.getAmountNeededToRaise().subtract(oldUnitPrice).add(newUnitPrice); // Cap nhat so tien can quyen gop
        project.setAmountNeededToRaise(amountNeededToRaise);

        projectRepository.save(project);

        BudgetResponseDTO budgetResponseDTO = BudgetResponseDTO.builder()
                .budgetId(budget.getBudgetId())
                .title(budget.getTitle())
                .unitPrice(budget.getUnitPrice())
                .note(budget.getNote())
                .status(budget.getStatus())
                .build();

        return budgetResponseDTO;
    }




    @Override
    public void deleteBudget(BigInteger budgetId) {
        CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));


        Budget budget = budgetRepository.findById(budgetId).orElseThrow(() -> new AppException(ErrorCode.BUDGET_NOT_FOUND));
        Project project = budget.getProject();

        //Check if the logged-in account is an admin or an employee assigned to the project
        if (!loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin") &&
                !project.getAssigns().stream().map(Assign::getAccount).toList().contains(loggedAccount)){
            throw new  AppException(ErrorCode.ACCESS_DENIED);
        }

        BigDecimal updatedTotalBudget = project.getTotalBudget().subtract(budget.getUnitPrice());
        BigDecimal amountNeededToRaise = project.getAmountNeededToRaise().subtract(budget.getUnitPrice());
        project.setTotalBudget(updatedTotalBudget);
        project.setAmountNeededToRaise(amountNeededToRaise);

        projectRepository.save(project);

        budgetRepository.delete(budget);
    }
}

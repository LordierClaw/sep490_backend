package vn.com.fpt.sep490_g28_summer2024_be.service.expense;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;

import vn.com.fpt.sep490_g28_summer2024_be.dto.expense.ExpenseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.expense.ExpenseFileDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;

import vn.com.fpt.sep490_g28_summer2024_be.entity.*;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseServiceImpl;
import vn.com.fpt.sep490_g28_summer2024_be.mapper.Mapper;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ExpenseFileRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ExpenseRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final FirebaseServiceImpl firebaseService;
    private final ExpenseRepository expenseRepository;
    private final ExpenseFileRepository expenseFileRepository;
    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;


    @Override
    public PageResponse<ExpenseDTO> viewByFilter(Integer page, Integer size, String title, BigInteger projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));
        Pageable pageable = PageRequest.of(page, size);
        Page<Expense> listedExpenses = expenseRepository.findExpenseByFilterAndProjectId(title, projectId, pageable);

        List<ExpenseDTO> expenseResponseDTOList = listedExpenses.stream()
                .map(expense -> {
                    ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                            .projectId(expense.getProject().getProjectId())
                            .title(expense.getProject().getTitle())
                            .build();

                    return ExpenseDTO.builder()
                            .expenseId(expense.getExpenseId())
                            .title(expense.getTitle())
                            .unitPrice(expense.getUnitPrice())
                            .createdAt(expense.getCreatedAt())
                            .updatedAt(expense.getUpdatedAt())
                            .project(projectResponseDTO)
                            .build();
                })
                .toList();  // Use Stream.toList() instead of Collectors.toList()

        return PageResponse.<ExpenseDTO>builder()
                .limit(size)
                .offset(page)
                .total((int) listedExpenses.getTotalElements())
                .content(expenseResponseDTOList)
                .build();
    }

    @Override
    public ExpenseDTO addExpense(ExpenseDTO expenseDTO, MultipartFile[] newFiles) {
        try {

            Project project = projectRepository.findById(expenseDTO.getProject().getProjectId()).
                    orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));

            //Check if the logged-in account is an admin or an employee assigned to the project
            CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername()).orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

            if (!loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin") &&
                    !project.getAssigns().stream().map(Assign::getAccount).toList().contains(loggedAccount)){
                throw new  AppException(ErrorCode.ACCESS_DENIED);
            }

            Expense expense = Mapper.mapDtoToEntity(expenseDTO, Expense.class);
            expense.setCreatedAt(LocalDateTime.now());
            expense.setProject(project);
            Expense savedExpense = expenseRepository.save(expense);

            if (newFiles != null) {
                List<String> fileUrls = firebaseService.uploadMultipleFile(newFiles, savedExpense.getExpenseId(), "project/expense-files");
                fileUrls.forEach(fileUrl -> {
                    ExpenseFile expenseFile = ExpenseFile.builder()
                            .expense(savedExpense)
                            .file(fileUrl)
                            .build();
                    expenseFileRepository.save(expenseFile);
                });
            }

            ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                    .projectId(project.getProjectId())
                    .title(project.getTitle())
                    .build();

            return ExpenseDTO.builder()
                    .expenseId(savedExpense.getExpenseId())
                    .title(savedExpense.getTitle())
                    .unitPrice(savedExpense.getUnitPrice())
                    .createdAt(savedExpense.getCreatedAt())
                    .updatedAt(savedExpense.getUpdatedAt())
                    .project(projectResponseDTO)
                    .build();
        } catch (IOException e) {
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }


    @Override
    public ExpenseDTO getExpenseById(BigInteger id) {
        Expense expense = expenseRepository.findById(id).orElseThrow(()
                -> new AppException(ErrorCode.EXPENSE_NOT_FOUND));


        ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                .projectId(expense.getProject().getProjectId())
                .title(expense.getProject().getTitle())
                .build();

        return ExpenseDTO.builder()
                .expenseId(expense.getExpenseId())
                .title(expense.getTitle())
                .unitPrice(expense.getUnitPrice())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .project(projectResponseDTO)
                .expenseFiles(expense.getExpenseFiles() != null ? expense.getExpenseFiles().stream()
                        .map(expenseFile -> ExpenseFileDTO.builder()
                                .file(expenseFile.getFile())
                                .build())
                        .toList() : null)
                .build();
    }

    @Override
    public ExpenseDTO updateExpense(ExpenseDTO expenseDTO, BigInteger id, MultipartFile[] newFiles) {
        try {
            Expense expense = expenseRepository.findById(id).orElseThrow(()
                    -> new AppException(ErrorCode.EXPENSE_NOT_FOUND));

            //Check if the logged-in account is an admin or an employee assigned to the project
            CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername()).orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

            if (!loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin") &&
                    !expense.getProject().getAssigns().stream().map(Assign::getAccount).toList().contains(loggedAccount)){
                throw new  AppException(ErrorCode.ACCESS_DENIED);
            }

            expense.setTitle(expenseDTO.getTitle());
            expense.setUnitPrice(expenseDTO.getUnitPrice());
            expense.setUpdatedAt(LocalDateTime.now());


            List<ExpenseFile> currentExpenseFiles = expenseFileRepository.findByExpenseId(id);

            if (expenseDTO.getExpenseFiles() != null) {
                List<String> remainFiles = expenseDTO.getExpenseFiles().stream()
                        .map(ExpenseFileDTO::getFile)
                        .toList();

                for (ExpenseFile currentFile : currentExpenseFiles) {
                    if (currentFile != null && !remainFiles.contains(currentFile.getFile())) {
                        firebaseService.deleteFileByPath(currentFile.getFile());
                        expenseFileRepository.delete(currentFile);
                    }
                }
            } else {
                for (ExpenseFile currentFile : currentExpenseFiles) {
                    if (currentFile != null) {
                        firebaseService.deleteFileByPath(currentFile.getFile());
                        expenseFileRepository.delete(currentFile);
                    }
                }
            }

            if (newFiles != null) {
                List<String> fileUrls = firebaseService.uploadMultipleFile(newFiles, id, "project/expense-files");
                fileUrls.forEach(fileUrl -> {
                    ExpenseFile newExpenseFile = ExpenseFile.builder()
                            .expense(expense)
                            .file(fileUrl)
                            .build();
                    expenseFileRepository.save(newExpenseFile);
                });
            }


            expenseRepository.save(expense);

            return ExpenseDTO.builder()
                    .expenseId(expense.getExpenseId())
                    .title(expense.getTitle())
                    .unitPrice(expense.getUnitPrice())
                    .createdAt(expense.getCreatedAt())
                    .updatedAt(expense.getUpdatedAt())
                    .expenseFiles(expense.getExpenseFiles() != null ?
                            expense.getExpenseFiles().stream()
                                    .map(expenseFile -> ExpenseFileDTO.builder()
                                            .file(expenseFile.getFile())
                                            .build())
                                    .toList() : null)
                    .build();
        } catch (IOException e) {
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }

    @Override
    public void deleteExpense(BigInteger id) {
        try {
            Expense expense = expenseRepository.findById(id).orElseThrow(()
                    -> new AppException(ErrorCode.EXPENSE_NOT_FOUND));

            //Check if the logged-in account is an admin or an employee assigned to the project
            CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername()).orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

            if (!loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin") &&
                    !expense.getProject().getAssigns().stream().map(Assign::getAccount).toList().contains(loggedAccount)){
                throw new  AppException(ErrorCode.ACCESS_DENIED);
            }

            for (ExpenseFile expenseFile : expense.getExpenseFiles()) {
                firebaseService.deleteFileByPath(expenseFile.getFile());
                expenseFileRepository.delete(expenseFile);
            }
            expenseRepository.delete(expense);
        } catch (IOException e) {
            throw new AppException(ErrorCode.DELETE_FILE_FAILED);
        }
    }

}

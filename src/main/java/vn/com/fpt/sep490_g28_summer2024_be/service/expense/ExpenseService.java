package vn.com.fpt.sep490_g28_summer2024_be.service.expense;

import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.dto.expense.ExpenseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;

import java.math.BigInteger;

public interface ExpenseService {
    PageResponse<ExpenseDTO> viewByFilter(Integer page, Integer size, String title,BigInteger projectId);
    ExpenseDTO addExpense(ExpenseDTO expenseDTO, MultipartFile[] newFiles);

    ExpenseDTO getExpenseById(BigInteger id);

    ExpenseDTO updateExpense(ExpenseDTO expenseDTO, BigInteger id, MultipartFile[] newFiles);

    void deleteExpense(BigInteger id);
}

package vn.com.fpt.sep490_g28_summer2024_be.service.budget;

import vn.com.fpt.sep490_g28_summer2024_be.dto.budget.BudgetRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.budget.BudgetResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;

import java.math.BigInteger;
import java.util.List;

public interface BudgetService {
    PageResponse<BudgetResponseDTO> viewBudgetByFilter(Integer page, Integer size, String title, BigInteger projectId);

    BudgetResponseDTO getBudgetById(BigInteger budgetId);

    List<BudgetResponseDTO> addBudgetsToProject(List<BudgetRequestDTO> budgetRequestDTOs, BigInteger projectId);

    BudgetResponseDTO updateBudget(BigInteger budgetId, BudgetRequestDTO budgetRequestDTO);

    void deleteBudget(BigInteger budgetId);
}

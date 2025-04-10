package vn.com.fpt.sep490_g28_summer2024_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Expense;


import java.math.BigInteger;

public interface ExpenseRepository extends JpaRepository<Expense, BigInteger> {
    @Query(value = """
        SELECT e.* 
        FROM expense e 
        WHERE e.project_id = :projectId 
        AND (:title IS NULL OR e.title LIKE CONCAT('%', :title, '%')) 
        ORDER BY e.expense_id DESC
        """,
            countQuery = """
        SELECT COUNT(e.expense_id) 
        FROM expense e 
        WHERE e.project_id = :projectId 
        AND (:title IS NULL OR e.title LIKE CONCAT('%', :title, '%'))
        """,
            nativeQuery = true)
    Page<Expense> findExpenseByFilterAndProjectId(@Param("title") String title,
                                                  @Param("projectId") BigInteger projectId,
                                                  Pageable pageable);
}

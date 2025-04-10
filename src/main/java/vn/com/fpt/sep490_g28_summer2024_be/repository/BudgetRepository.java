package vn.com.fpt.sep490_g28_summer2024_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Budget;


import java.math.BigInteger;

public interface BudgetRepository extends JpaRepository<Budget, BigInteger> {
    @Query(value = """
            SELECT e.* 
            FROM budget e 
            WHERE e.project_id = :projectId 
            AND (:title IS NULL OR e.title LIKE CONCAT('%', :title, '%')) 
            ORDER BY e.budget_id DESC
            """,
            countQuery = """
            SELECT COUNT(e.budget_id) 
            FROM budget e 
            WHERE e.project_id = :projectId 
            AND (:title IS NULL OR e.title LIKE CONCAT('%', :title, '%'))
            """,
            nativeQuery = true)
    Page<Budget> findBudgetByFilterAndProjectId(@Param("title") String title,
                                                @Param("projectId") BigInteger projectId,
                                                Pageable pageable);
}

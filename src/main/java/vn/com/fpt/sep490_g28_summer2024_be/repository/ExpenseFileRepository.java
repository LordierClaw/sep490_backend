package vn.com.fpt.sep490_g28_summer2024_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fpt.sep490_g28_summer2024_be.entity.ExpenseFile;

import java.math.BigInteger;
import java.util.List;


public interface ExpenseFileRepository extends JpaRepository<ExpenseFile, BigInteger> {
    @Query(value = """
        SELECT ef.* 
        FROM expense_file ef 
        WHERE ef.expense_id = :expenseId
        """, nativeQuery = true)
    List<ExpenseFile> findByExpenseId(@Param("expenseId") BigInteger expenseId);
}

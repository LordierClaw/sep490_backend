package vn.com.fpt.sep490_g28_summer2024_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Assign;

import java.math.BigInteger;
import java.util.List;

public interface AssignRepository extends JpaRepository<Assign, BigInteger> {
    List<Assign> findByProject_ProjectIdAndAccount_AccountIdIn(BigInteger projectId, List<BigInteger> accountIds);

    @Query(value = """
    SELECT ass.* 
    FROM assign ass
    LEFT JOIN account a ON a.account_id = ass.account_id
    WHERE ass.project_id = :projectId
    AND (:role IS NULL OR a.role_id = :role)
    AND (:email IS NULL OR a.email LIKE %:email%)
    AND (:fullname IS NULL OR a.fullname LIKE %:fullname%)
    ORDER BY ass.assign_id DESC
    """,
            countQuery = """
    SELECT COUNT(ass.assign_id)
    FROM assign ass
    LEFT JOIN account a ON a.account_id = ass.account_id
    WHERE ass.project_id = :projectId
    AND (:role IS NULL OR a.role_id = :role)
    AND (:email IS NULL OR a.email LIKE %:email%)
    AND (:fullname IS NULL OR a.fullname LIKE %:fullname%)
    """,
            nativeQuery = true)
    Page<Assign> findMembersInProject(@Param("projectId") BigInteger projectId,
                                      @Param("role") BigInteger role,
                                      @Param("email") String email,
                                      @Param("fullname") String fullname,
                                      Pageable pageable);


}

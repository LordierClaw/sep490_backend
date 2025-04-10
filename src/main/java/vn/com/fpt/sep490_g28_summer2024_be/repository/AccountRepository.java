package vn.com.fpt.sep490_g28_summer2024_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.client.interfacedto.AmbassadorInterfaceDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.client.interfacedto.TopAmbassadorInterfaceDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, BigInteger> {

    Boolean existsAccountByEmail(String email);

    Optional<Account> findByEmail(String email);

    @Query(value = """
            SELECT DISTINCT a.*
            FROM account a
            JOIN news n ON a.account_id = n.created_by
            """, nativeQuery = true)
    List<Account> findAccountsByNewsCreatedBy();

    Optional<Account> findByAccountId(BigInteger id);

    Optional<Account> findByCode(String accountCode);

    @Query(value = """
            SELECT a.*
            FROM account a
            JOIN role r ON a.role_id = r.role_id
            WHERE r.role_name = 'project manager'
            ORDER BY a.account_id ASC
            """, nativeQuery = true)
    List<Account> findProjectManagerAccounts();

    @Query(value = """
            SELECT *
            FROM account a
            WHERE a.account_id NOT IN (
                SELECT assign.account_id
                FROM assign assign
                WHERE assign.project_id = :projectId
            )
            AND a.is_active = TRUE
            AND a.role_id = (
                SELECT role_id FROM role WHERE role_name = 'project manager'
            )
            """, nativeQuery = true)
    List<Account> findActiveAccountsNotAssignedToProject(@Param("projectId") BigInteger projectId);

    @Query(value = """
            SELECT a.*
            FROM account a
            LEFT JOIN role r ON a.role_id = r.role_id
            WHERE (:email IS NULL OR a.email LIKE %:email%)
            AND (:is_active IS NULL OR a.is_active = :is_active)
            AND (:role IS NULL OR r.role_id = :role)
            AND a.account_id <> :currentAccountId
            AND NOT EXISTS (
                SELECT 1
                FROM role r2
                WHERE r2.role_name = 'system user' AND r2.role_id = a.role_id
            )
            ORDER BY a.account_id DESC
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM account a
                    LEFT JOIN role r ON a.role_id = r.role_id
                    WHERE (:email IS NULL OR a.email LIKE %:email%)
                    AND (:is_active IS NULL OR a.is_active = :is_active)
                    AND (:role IS NULL OR r.role_id = :role)
                    AND a.account_id <> :currentAccountId
                    AND NOT EXISTS (
                        SELECT 1
                        FROM role r2
                        WHERE r2.role_name = 'system user' AND r2.role_id = a.role_id
                    )
                    """,
            nativeQuery = true)
    Page<Account> findAccountsByFilters(@Param("email") String email,
                                        @Param("is_active") Boolean is_active,
                                        @Param("role") BigInteger role,
                                        @Param("currentAccountId") BigInteger currentAccountId,
                                        Pageable pageable);


    @Query(value = """
            SELECT a.*, COALESCE(SUM(d.value), 0) AS total_donations
            FROM account a
            JOIN role r ON a.role_id = r.role_id
            LEFT JOIN donation d ON a.account_id = d.created_by
            WHERE r.role_name = 'system user'
            AND (:fullname IS NULL OR a.fullname LIKE %:fullname%)
            AND (:email IS NULL OR a.email LIKE %:email%)
            AND (:phone IS NULL OR a.phone LIKE %:phone%)
            GROUP BY a.account_id, a.code, a.refer_code, a.email, a.password, a.fullname, a.gender, a.phone, a.address, a.avatar, a.dob, a.created_at, a.updated_at, a.is_active, a.role_id
            HAVING (:minDonation IS NULL OR COALESCE(SUM(d.value), 0) >= :minDonation)
                   AND (:maxDonation IS NULL OR COALESCE(SUM(d.value), 0) <= :maxDonation)
            ORDER BY total_donations DESC
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM (
                        SELECT a.account_id
                        FROM account a
                        JOIN role r ON a.role_id = r.role_id
                        LEFT JOIN donation d ON a.account_id = d.created_by
                        WHERE r.role_name = 'system user'
                        AND (:fullname IS NULL OR a.fullname LIKE %:fullname%)
                        AND (:email IS NULL OR a.email LIKE %:email%)
                        AND (:phone IS NULL OR a.phone LIKE %:phone%)
                        GROUP BY a.account_id, a.code, a.refer_code, a.email, a.password, a.fullname, a.gender, a.phone, a.address, a.avatar, a.dob, a.created_at, a.updated_at, a.is_active, a.role_id
                        HAVING (:minDonation IS NULL OR COALESCE(SUM(d.value), 0) >= :minDonation)
                               AND (:maxDonation IS NULL OR COALESCE(SUM(d.value), 0) <= :maxDonation)
                    ) AS count_query
                    """,
            nativeQuery = true)
    Page<Account> findSystemUsersAccountsByFilters(
            @Param("fullname") String fullname,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("minDonation") BigDecimal minDonation,
            @Param("maxDonation") BigDecimal maxDonation,
            Pageable pageable
    );

    @Query(value = """
    SELECT a1.accountId, a1.code, a1.fullname, a1.avatar, a1.createdAt, 
           a1.totalReferDonation + IFNULL(SUM(d.value), 0) AS totalDonation
    FROM (SELECT a.account_id AS accountId, a.code, a.fullname, a.avatar, 
                 a.created_at AS createdAt, a.role_id, a.email, a.phone,
                 IFNULL(SUM(d.value), 0) AS totalReferDonation
          FROM account a
          LEFT JOIN donation d ON d.refer_id = a.account_id
          GROUP BY a.account_id, a.code, a.fullname, a.avatar, a.created_at, a.role_id, a.email, a.phone) a1
    LEFT JOIN challenge ch ON ch.created_by = a1.accountId
    LEFT JOIN donation d ON d.challenge_id = ch.challenge_id
    WHERE a1.role_id = 4
    AND (:fullname IS NULL OR a1.fullname LIKE CONCAT('%', :fullname, '%'))
    AND (:code IS NULL OR a1.code LIKE CONCAT('%', :code, '%'))
    AND (:email IS NULL OR a1.email LIKE CONCAT('%', :email, '%'))
    AND (:phone IS NULL OR a1.phone LIKE CONCAT('%', :phone, '%'))
    GROUP BY a1.accountId, a1.code, a1.fullname, a1.avatar, a1.createdAt
    HAVING (:minTotalDonation IS NULL OR totalDonation >= :minTotalDonation)
    AND (:maxTotalDonation IS NULL OR totalDonation <= :maxTotalDonation)
    """, countQuery = """
    SELECT COUNT(DISTINCT a.account_id)
    FROM account a
    LEFT JOIN challenge ch ON ch.created_by = a.account_id
    LEFT JOIN donation d ON d.refer_id = a.account_id OR d.challenge_id = ch.challenge_id
    WHERE a.role_id = 4
    AND (:fullname IS NULL OR a.fullname LIKE CONCAT('%', :fullname, '%'))
    AND (:code IS NULL OR a.code LIKE CONCAT('%', :code, '%'))
    AND (:email IS NULL OR a.email LIKE CONCAT('%', :email, '%'))
    AND (:phone IS NULL OR a.phone LIKE CONCAT('%', :phone, '%'))
    GROUP BY a.account_id
    HAVING (:minTotalDonation IS NULL OR IFNULL(SUM(d.value), 0) >= :minTotalDonation)
    AND (:maxTotalDonation IS NULL OR IFNULL(SUM(d.value), 0) <= :maxTotalDonation)
    """, nativeQuery = true)
    Page<AmbassadorInterfaceDTO> findAmbassadorsByFilters(
            @Param("fullname") String fullname,
            @Param("code") String code,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("minTotalDonation") BigDecimal min,
            @Param("maxTotalDonation") BigDecimal max,
            Pageable pageable
    );




    Page<Account> findAll(Pageable pageable);

    @Query(value = """
            SELECT *
            FROM account a
            WHERE :description LIKE CONCAT('%', a.refer_code, '%')
            LIMIT 1
            """, nativeQuery = true)
    Account findReferAccountByDescription(@Param("description") String description);

    @Query(value = """
            SELECT *
            FROM account a
            WHERE :description LIKE CONCAT('%', a.code, '%')
            LIMIT 1
            """, nativeQuery = true)
    Account findAccountByDescription(@Param("description") String description);

    @Query(value = """
            SELECT *
            FROM account a
            WHERE a.code = :code
            AND a.role_id = (
                SELECT role_id FROM role WHERE role_name = 'system user'
            )
            """, nativeQuery = true)
    Optional<Account> findSystemUserAccountByAccountCode(@Param("code") String code);

    @Query(value = """
    SELECT a.accountId, a.code, a.fullname, a.avatar, 
           a.totalReferDonation + b.totalChallengeDonation AS totalDonation, 
           a.countReferDonations + b.countChallengeDonations AS countDonations
    FROM (SELECT a.account_id AS accountId, a.code, a.fullname, a.avatar, 
                 IFNULL(SUM(d1.value), 0) AS totalReferDonation, 
                 COUNT(DISTINCT d1.donation_id) AS countReferDonations
          FROM account a 
          LEFT JOIN donation d1 ON d1.refer_id = a.account_id 
          WHERE a.role_id = 4 
          GROUP BY a.account_id, a.code, a.fullname, a.avatar) a
    LEFT JOIN (SELECT a.account_id AS accountId, 
                      IFNULL(SUM(d.value), 0) AS totalChallengeDonation, 
                      COUNT(DISTINCT d.donation_id) AS countChallengeDonations
               FROM account a 
               LEFT JOIN challenge ch ON ch.created_by = a.account_id 
               LEFT JOIN donation d ON d.challenge_id = ch.challenge_id 
               WHERE a.role_id = 4 
               GROUP BY a.account_id) b 
    ON a.accountId = b.accountId 
    ORDER BY totalDonation DESC 
    LIMIT :limit
    """, nativeQuery = true)
    List<TopAmbassadorInterfaceDTO> getTopAmbassador(@Param("limit") Integer limit);


    @Query(value = """
    SELECT  a.account_id AS accountId,
            a.code,
            a.fullname,
            a.avatar,
            IFNULL((SUM(d.value)), 0) AS totalDonation,
            COUNT(DISTINCT d.donation_id) AS countDonations
    FROM account a
    LEFT JOIN donation d ON d.created_by = a.account_id
    WHERE a.role_id = 4
    GROUP BY a.account_id, a.code, a.fullname, a.avatar
    ORDER BY totalDonation DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<TopAmbassadorInterfaceDTO> getTopDonors(@Param("limit") Integer limit);

}

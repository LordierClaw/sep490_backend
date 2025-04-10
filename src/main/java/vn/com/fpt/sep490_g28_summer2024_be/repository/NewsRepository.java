package vn.com.fpt.sep490_g28_summer2024_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fpt.sep490_g28_summer2024_be.entity.News;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, BigInteger> {

    Optional<News> findByTitle(String title);


    @Query(value = """
        SELECT *
        FROM news
        WHERE (:category_id IS NULL OR category_id = :category_id)
        AND (:title IS NULL OR title LIKE %:title%)
        AND (:authorId IS NULL OR created_by = :authorId)
        AND (:status IS NULL OR status = :status)
        AND (:startDate IS NULL OR created_at >= :startDate)
        AND (:endDate IS NULL OR created_at <= :endDate)
        ORDER BY news_id DESC
        """, nativeQuery = true)
    Page<News> findNewsByFilters(@Param("category_id") BigInteger category_id,
                                 @Param("title") String title,
                                 @Param("authorId") BigInteger authorId,
                                 @Param("status") Integer status,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 Pageable pageable);

    @Query(value = """
        SELECT n.category_id, COUNT(*) 
        FROM news n 
        WHERE n.status = 2 
        GROUP BY n.category_id
        """, nativeQuery = true)
    List<Object[]> countNewsByCategoryAndStatus();



    @Query(value = """
        SELECT * 
        FROM news 
        WHERE (:category_id IS NULL OR category_id = :category_id) 
        AND (:title IS NULL OR title LIKE CONCAT('%', :title, '%')) 
        AND status = 2 
        ORDER BY created_at DESC
        """,
            countQuery = """
        SELECT COUNT(*) 
        FROM news 
        WHERE (:category_id IS NULL OR category_id = :category_id) 
        AND (:title IS NULL OR title LIKE CONCAT('%', :title, '%')) 
        AND status = 2
        """,
            nativeQuery = true)
    Page<News> findNewsByTitleAndCategories(@Param("category_id") BigInteger category_id,
                                            @Param("title") String title,
                                            Pageable pageable);

    @Query(value = """
        SELECT n.* 
        FROM news n 
        JOIN account a ON n.created_by = a.account_id 
        WHERE a.account_id = :accountId 
        AND (:category_id IS NULL OR n.category_id = :category_id) 
        AND (:title IS NULL OR n.title LIKE CONCAT('%', :title, '%')) 
        AND (:status IS NULL OR n.status = :status) 
        AND (:startDate IS NULL OR n.created_at >= :startDate) 
        AND (:endDate IS NULL OR n.created_at <= :endDate) 
        ORDER BY n.news_id DESC
        """,
            countQuery = """
        SELECT COUNT(n.news_id) 
        FROM news n 
        JOIN account a ON n.created_by = a.account_id 
        WHERE a.account_id = :accountId 
        AND (:category_id IS NULL OR n.category_id = :category_id) 
        AND (:title IS NULL OR n.title LIKE CONCAT('%', :title, '%')) 
        AND (:status IS NULL OR n.status = :status) 
        AND (:startDate IS NULL OR n.created_at >= :startDate) 
        AND (:endDate IS NULL OR n.created_at <= :endDate)
        """,
            nativeQuery = true)
    Page<News> findNewsByAccount(@Param("accountId") BigInteger accountId,
                                 @Param("category_id") BigInteger category_id,
                                 @Param("title") String title,
                                 @Param("status") Integer status,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 Pageable pageable);


    Page<News> findAll(Pageable pageable);
}

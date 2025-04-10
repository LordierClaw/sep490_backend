package vn.com.fpt.sep490_g28_summer2024_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Category;

import java.math.BigInteger;


public interface CategoryRepository extends JpaRepository<Category, BigInteger> {
    Boolean existsCategoriesByTitle(String title);
}

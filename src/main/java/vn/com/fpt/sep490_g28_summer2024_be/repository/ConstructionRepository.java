package vn.com.fpt.sep490_g28_summer2024_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Construction;

import java.math.BigInteger;

public interface ConstructionRepository extends JpaRepository<Construction, BigInteger> {
}

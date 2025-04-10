package vn.com.fpt.sep490_g28_summer2024_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Challenge;
import vn.com.fpt.sep490_g28_summer2024_be.entity.ChallengeProject;

import java.math.BigInteger;
import java.util.List;

public interface ChallengeProjectRepository extends JpaRepository<ChallengeProject, BigInteger> {
    List<ChallengeProject> findByChallenge(Challenge challenge);
}

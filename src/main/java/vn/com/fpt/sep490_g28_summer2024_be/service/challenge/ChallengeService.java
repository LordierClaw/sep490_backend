package vn.com.fpt.sep490_g28_summer2024_be.service.challenge;

import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.ChallengeRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.ChallengeResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public interface ChallengeService {
    ChallengeResponseDTO addChallenge(ChallengeRequestDTO request, String email, MultipartFile newImage);

    ChallengeResponseDTO updateChallenge(ChallengeRequestDTO request, BigInteger id, MultipartFile thumbnail, String email);

    PageResponse<ChallengeResponseDTO> viewActiveChallengesByFilter(Integer page, Integer size, String accountCode);

    PageResponse<ChallengeResponseDTO> viewExpiredChallengesByFilter(Integer page, Integer size, String accountCode);

    PageResponse<ChallengeResponseDTO> viewChallengesAdminByFilter(Integer page, Integer size, String title, Integer year, BigDecimal minDonation, BigDecimal maxDonation);

    ChallengeResponseDTO getChallengeById(BigInteger id);

    void deleteChallenge(BigInteger id);

    List<ChallengeResponseDTO> getTopChallenges(Integer number);
    PageResponse<?> getChallenges(Integer page, Integer size, String title, String fullname, BigDecimal min, BigDecimal max);
}

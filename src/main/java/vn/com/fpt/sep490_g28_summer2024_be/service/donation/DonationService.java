package vn.com.fpt.sep490_g28_summer2024_be.service.donation;


import vn.com.fpt.sep490_g28_summer2024_be.dto.donation.DonationResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponseDTO;

import java.math.BigInteger;


public interface DonationService {

    PageResponse<DonationResponseDTO> viewListDonations(Integer page, Integer size, BigInteger projectId, String description);

    PageResponse<DonationResponseDTO> viewListDonationsAdmin(Integer page, Integer size, BigInteger projectId, String description);

    PageResponse<DonationResponseDTO> viewDonationsByChallengeId(Integer page, Integer size, BigInteger challengeId, String description);

    PageResponse<DonationResponseDTO> viewDonationsByAccount(Integer page, Integer size, String email, String description);

    PageResponse<?> viewDonationsByReferCode(Integer page, Integer size, String code);

    PageResponse<?> viewAllDonations(Integer page, Integer size, String description);
}

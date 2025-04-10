package vn.com.fpt.sep490_g28_summer2024_be.service.casso;

import vn.com.fpt.sep490_g28_summer2024_be.dto.donation.DonationResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.casso.TransactionDataDTO;



public interface CassoService {
    DonationResponseDTO handleInPayment(TransactionDataDTO transactionDataDTO);

    DonationResponseDTO handleOutPayment(TransactionDataDTO transactionDataDTO);

    void initMissingDonation() throws Exception;
}

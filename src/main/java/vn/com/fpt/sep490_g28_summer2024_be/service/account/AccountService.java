package vn.com.fpt.sep490_g28_summer2024_be.service.account;


import jakarta.mail.MessagingException;

import org.springframework.data.repository.query.Param;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountForgotPasswordDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountProfilePageDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountRegisterDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.client.AmbassadorResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.client.TopAmbassadorResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.utils.Email;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;


public interface AccountService {
    AccountDTO createAccount(AccountDTO request);

    PageResponse<AccountDTO> viewSystemUserAccountsByFilter(Integer page, Integer size, String fullname, String email, String phone, BigDecimal minDonation, BigDecimal maxDonation);

    PageResponse<?> viewByFilter(Integer page, Integer size, String email, Boolean isActive, BigInteger roleId);

    AccountDTO getAccountById(BigInteger id);

    AccountProfilePageDTO getTotalDonateAndCountDonateByAccountCode(String accountCode);

    AccountDTO updateAccountByAdmin(AccountDTO accountDTO, BigInteger id);

    AccountDTO deactivateAccount(BigInteger id);

    List<AccountDTO> getAuthorNewsAccounts();

    List<AccountDTO> getIdAndEmailProjectManagerAccounts();

    AccountDTO changePassword(String email, String oldPassword, String newPassword);

    ApiResponse<?> register(AccountRegisterDTO registerDTO);

    AccountDTO updateProfile(String email, AccountDTO accountDTO, MultipartFile avatarFile);

    ApiResponse<?> sendOtp(Email email);

    ApiResponse<?> forgot(AccountForgotPasswordDTO request) throws MessagingException;

    AccountDTO findAccountByEmail(String email);

    List<TopAmbassadorResponseDTO> getTopAmbassador(Integer limit);

    List<TopAmbassadorResponseDTO> getTopDonors(Integer limit);

    PageResponse<?> getAmbassadors(Integer page, Integer size, String fullname, String code, String email, String phone, BigDecimal min, BigDecimal max);

}

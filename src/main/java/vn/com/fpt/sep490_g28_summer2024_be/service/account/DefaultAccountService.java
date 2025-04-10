package vn.com.fpt.sep490_g28_summer2024_be.service.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.AppConfig;
import vn.com.fpt.sep490_g28_summer2024_be.common.CommonMessage;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountForgotPasswordDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountProfilePageDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountRegisterDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.client.AmbassadorResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.client.TopAmbassadorResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.client.interfacedto.AmbassadorInterfaceDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.role.RoleDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Role;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseService;
import vn.com.fpt.sep490_g28_summer2024_be.mapper.Mapper;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.DonationRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.RoleRepository;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.service.email.EmailService;
import vn.com.fpt.sep490_g28_summer2024_be.utils.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultAccountService implements AccountService {
    private final AccountRepository accountRepository;
    private final DonationRepository donationRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpUtils otpUtils;
    private final RoleRepository roleRepository;
    private final CodeUtils codeUtils;
    private final EmailService emailService;
    private final FirebaseService firebaseService;
    private  static final BigInteger SYSTEM_USER_ROLE_ID = BigInteger.valueOf(4);

    @Override
    public AccountDTO createAccount(AccountDTO request) {

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(AppConfig.BCRYPT_PASSWORD_ENCODER);

        if (Boolean.TRUE.equals(accountRepository.existsAccountByEmail(request.getEmail()))) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }


        Role existingRole = roleRepository.findById(request.getRole().getRoleId()).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        Account account = Account.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullname(request.getFullname())
                .gender(request.getGender())
                .phone(request.getPhone())
                .address(request.getAddress())
                .dob(request.getDob())
                .createdAt(LocalDate.now().atStartOfDay())
                .isActive(true)
                .role(existingRole)
                .build();

        Account savedAccount = accountRepository.save(account);


        RoleDTO roleDTO = RoleDTO.builder()
                .roleId(savedAccount.getRole().getRoleId())
                .roleName(savedAccount.getRole().getRoleName())
                .build();

        return AccountDTO.builder()
                .accountId(savedAccount.getAccountId())
                .code(savedAccount.getCode())
                .referCode(savedAccount.getReferCode())
                .email(savedAccount.getEmail())
                .password(savedAccount.getPassword())
                .fullname(savedAccount.getFullname())
                .gender(savedAccount.getGender())
                .phone(savedAccount.getPhone())
                .address(savedAccount.getAddress())
                .dob(savedAccount.getDob())
                .createdAt(savedAccount.getCreatedAt())
                .isActive(savedAccount.getIsActive())
                .role(roleDTO)
                .build();
    }

    @Override
    public PageResponse<AccountDTO> viewSystemUserAccountsByFilter(Integer page, Integer size, String fullname, String email, String phone, BigDecimal minDonation, BigDecimal maxDonation) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accountPage = accountRepository.findSystemUsersAccountsByFilters(fullname, email, phone, minDonation, maxDonation, pageable);

        List<AccountDTO> accountDTOs = accountPage.stream().map(account -> {

            BigDecimal totalDonations = donationRepository.sumDonationsByAccountCode(account.getCode());
            totalDonations = totalDonations != null ? totalDonations : BigDecimal.ZERO;

            Role role = account.getRole();
            RoleDTO roleDTO = RoleDTO.builder()
                    .roleId(role.getRoleId())
                    .roleName(role.getRoleName())
                    .build();

            return AccountDTO.builder()
                    .accountId(account.getAccountId())
                    .email(account.getEmail())
                    .fullname(account.getFullname())
                    .avatar(account.getAvatar())
                    .phone(account.getPhone())
                    .dob(account.getDob())
                    .totalDonations(totalDonations)
                    .createdAt(account.getCreatedAt())
                    .updatedAt(account.getUpdatedAt())
                    .isActive(account.getIsActive())
                    .role(roleDTO)
                    .build();
        }).collect(Collectors.toList());

        return PageResponse.<AccountDTO>builder()
                .content(accountDTOs)
                .limit(size)
                .offset(page)
                .total((int) accountPage.getTotalElements())
                .build();
    }

    @Override
    public PageResponse<AccountDTO> viewByFilter(Integer page, Integer size, String email, Boolean isActive, BigInteger roleId) {
        CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accountPage = accountRepository.findAccountsByFilters(email, isActive, roleId, loggedAccount.getAccountId(), pageable);

        List<AccountDTO> accountDTOs = accountPage.stream().map(account -> {
            Role role = account.getRole();
            RoleDTO roleDTO = RoleDTO.builder()
                    .roleId(role.getRoleId())
                    .roleName(role.getRoleName())
                    .build();

            return AccountDTO.builder()
                    .accountId(account.getAccountId())
                    .email(account.getEmail())
                    .fullname(account.getFullname())
                    .avatar(account.getAvatar())
                    .phone(account.getPhone())
                    .dob(account.getDob())
                    .createdAt(account.getCreatedAt())
                    .updatedAt(account.getUpdatedAt())
                    .isActive(account.getIsActive())
                    .role(roleDTO)
                    .build();
        }).toList();

        return PageResponse.<AccountDTO>builder()
                .content(accountDTOs)
                .limit(size)
                .offset(page)
                .total((int) accountPage.getTotalElements())
                .build();
    }


    @Override
    public AccountDTO getAccountById(BigInteger id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NO_CONTENT));

        return Mapper.mapEntityToDto(account, AccountDTO.class);
    }

    @Override
    public AccountProfilePageDTO getTotalDonateAndCountDonateByAccountCode(String accountCode) {
        Account account = accountRepository.findByCode(accountCode)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NO_CONTENT));

        if (!account.getRole().getRoleId().equals(SYSTEM_USER_ROLE_ID)) {
            throw new AppException(ErrorCode.ADMIN_ACCESS_DENIED);
        }

        BigDecimal totalDonations = donationRepository.sumDonationsByAccountCode(accountCode);

        BigDecimal totalDonationsRefer = donationRepository.getTotalDonationByReferId(account.getAccountId());
        BigDecimal totalDonationsChallenge = donationRepository.getTotalDonationsChallengeByCreatedBy(account.getAccountId());
        Long donationCount = donationRepository.countDonationsByAccountCode(accountCode);

        Long donationCountRefer = donationRepository.countDonationsByReferId(account.getAccountId());
        Long donationCountChallenge = donationRepository.countDonationsChallengeByCreatedBy(account.getAccountId());

        totalDonations = totalDonations != null ? totalDonations : BigDecimal.ZERO;
        totalDonationsRefer = totalDonationsRefer != null ? totalDonationsRefer : BigDecimal.ZERO;
        totalDonationsChallenge = totalDonationsChallenge != null ? totalDonationsChallenge : BigDecimal.ZERO;

        BigDecimal totalSumOfDonations = totalDonationsRefer.add(totalDonationsChallenge);
        Long totalDonationReferCount = donationCountRefer + donationCountChallenge;

        return AccountProfilePageDTO.builder()
                .code(account.getCode())
                .referCode(account.getReferCode())
                .fullname(account.getFullname())
                .avatar(account.getAvatar())
                .totalDonations(totalDonations)
                .totalDonationReferCount(totalDonationReferCount)
                .totalDonationsRefer(totalSumOfDonations)
                .donationCount(donationCount)
                .build();
    }

    @Override
    public AccountDTO updateAccountByAdmin(AccountDTO accountDTO, BigInteger id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NO_CONTENT));

        account.setUpdatedAt(LocalDate.now().atStartOfDay());
        account.setIsActive(accountDTO.getIsActive());
        account.setRole(Mapper.mapDtoToEntity(accountDTO.getRole(), Role.class));
        accountRepository.save(account);

        return Mapper.mapEntityToDto(account, AccountDTO.class);
    }

    @Override
    public AccountDTO deactivateAccount(BigInteger id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NO_CONTENT));
        account.setIsActive(false);
        accountRepository.save(account);

        return Mapper.mapEntityToDto(account, AccountDTO.class);
    }

    @Override
    public List<AccountDTO> getAuthorNewsAccounts() {
        List<Account> accounts = accountRepository.findAccountsByNewsCreatedBy();
        if (accounts.isEmpty()) {
            throw new AppException(ErrorCode.ACCOUNT_NO_CONTENT);
        }
        return accounts.stream()
                .map(account -> AccountDTO.builder()
                        .accountId(account.getAccountId())
                        .fullname(account.getFullname())
                        .build())
                .toList();
    }

    @Override
    public List<AccountDTO> getIdAndEmailProjectManagerAccounts() {
        List<Account> accounts = accountRepository.findProjectManagerAccounts();
        if (accounts.isEmpty()) {
            throw new AppException(ErrorCode.ACCOUNT_NO_CONTENT);
        }
        return accounts.stream()
                .map(account -> AccountDTO.builder()
                        .accountId(account.getAccountId())
                        .email(account.getEmail())
                        .build())
                .toList();
    }

    @Override
    public AccountDTO changePassword(String email, String oldPassword, String newPassword) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
            throw new AppException(ErrorCode.OLD_PASSWORD_INCORRECT);
        }

        if (passwordEncoder.matches(newPassword, account.getPassword())) {
            throw new AppException(ErrorCode.NEW_PASSWORD_MUST_BE_DIFFERENT);
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);

        return AccountDTO.builder()
                .email(account.getEmail())
                .password(newPassword)
                .build();
    }

    @Override
    public AccountDTO updateProfile(String email, AccountDTO accountDTO, MultipartFile avatarFile) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        account.setFullname(accountDTO.getFullname());
        account.setDob(accountDTO.getDob());
        account.setPhone(accountDTO.getPhone());
        account.setAddress(accountDTO.getAddress());
        account.setUpdatedAt(LocalDateTime.now());
        account.setGender(accountDTO.getGender());

        if (accountDTO.getAvatar() == null || accountDTO.getAvatar().isBlank()) {
            try {
                if (account.getAvatar() != null) {
                    firebaseService.deleteFileByPath(account.getAvatar());
                    account.setAvatar(null);
                }
            } catch (IOException e) {
                throw new AppException(ErrorCode.DELETE_FILE_FAILED);
            }
        }

        if (avatarFile != null) {
            try {
                // Kiểm tra loại tệp
                if (!Objects.requireNonNull(avatarFile.getContentType()).startsWith("image/")) {
                    throw new AppException(ErrorCode.HTTP_FILE_IS_NOT_IMAGE);
                }

                // Kiểm tra kích thước tệp
                if (avatarFile.getSize() > 2 * 1024 * 1024) { // 2MB
                    throw new AppException(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT);
                }

                String fileName = firebaseService.uploadOneFile(avatarFile, account.getAccountId(), "account-avatars");
                account.setAvatar(fileName);
            } catch (IOException e) {
                throw new AppException(ErrorCode.UPLOAD_FAILED);
            }
        }

        accountRepository.save(account);
        return AccountDTO.builder()
                .email(account.getEmail())
                .fullname(account.getFullname())
                .phone(account.getPhone())
                .address(account.getAddress())
                .avatar(account.getAvatar())
                .dob(account.getDob())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    @Override
    public ApiResponse<?> register(AccountRegisterDTO registerDTO) {
        if (otpUtils.get(registerDTO.getEmail()) == null) {
            throw new AppException(ErrorCode.HTTP_OTP_INVALID);
        }
        if (!registerDTO.getOtp().equals(otpUtils.get(registerDTO.getEmail()))) {
            throw new AppException(ErrorCode.HTTP_OTP_INVALID);
        }
        //encode password;
        registerDTO.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        Account user = Mapper.mapDtoToEntity(registerDTO, Account.class);

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setIsActive(true);
        user.setRole(roleRepository.findRoleByRoleName("system user").orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED)));
        //save in database
        accountRepository.save(user);

        //gen code
        user.setCode(codeUtils.genCode(AppConfig.ACCOUNT_PREFIX, user.getAccountId()));
        user.setReferCode(codeUtils.genCode(AppConfig.REFER_PREFIX, user.getAccountId()));
        accountRepository.save(user);

        //delete cache otp
        otpUtils.delete(registerDTO.getEmail());
        return ApiResponse.builder().code("200").message("Register successfully!").build();
    }

    @Override
    public ApiResponse<?> sendOtp(Email email) {
        //validate email is exist or not
        if (accountRepository.existsAccountByEmail(email.getEmail())) throw new AppException(ErrorCode.EXIST_EMAIL);
        //gen otp
        String otp = otpUtils.generateOtp();

        String emailTemplate = String.format(EmailTemplate.otpEmailTemplate, email.getEmail(),
                otp, TimeUnit.MINUTES.toMinutes(AppConfig.VALID_OTP_TIME / 60), LocalDate.now().getYear());

        email.setBody(emailTemplate);
        email.setTitle("Thông báo mã xác thực tài khoản người dùng trên hệ thống Góp Lẻ là %s".formatted(otp));

        emailService.sendEmail(email)
                .thenAccept(v -> otpUtils.add(email.getEmail(), otp));

        return ApiResponse.builder()
                .code("200")
                .message("Successfully!")
                .build();
    }

    @Override
    public ApiResponse<?> forgot(AccountForgotPasswordDTO request) {
        Account userRef = accountRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        //gen password random
        String randomPassword = generatePassword(12);

        //init email
        String emailTemplate = String.format(EmailTemplate.resetPasswordEmailTemplate, userRef.getFullname(), userRef.getEmail(), randomPassword, LocalDate.now().getYear());


        Email email = Email.builder()
                .email(request.getEmail())
                .title("Thông báo thay đổi mật khẩu tài khoản người dùng trên hệ thống Góp Lẻ")
                .body(emailTemplate)
                .build();

        emailService.sendEmail(email).thenAccept(v -> {
            userRef.setPassword(passwordEncoder.encode(randomPassword));
            accountRepository.save(userRef);
        });

        return ApiResponse.builder().code(ErrorCode.HTTP_OK.getCode()).message(ErrorCode.HTTP_OK.getMessage()).data(CommonMessage.UPDATE_SUCCESSFULLY).build();
    }

    @Override
    public AccountDTO findAccountByEmail(String email) {
        Account account = accountRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return AccountDTO.builder()
                .fullname(account.getFullname())
                .referCode(account.getReferCode())
                .code(account.getCode())
                .email(account.getEmail())
                .phone(account.getPhone())
                .address(account.getAddress())
                .gender(account.getGender())
                .dob(account.getDob())
                .avatar(account.getAvatar())
                .build();
    }

    @Override
    public List<TopAmbassadorResponseDTO> getTopAmbassador(Integer limit) {
        var listDTO = accountRepository.getTopAmbassador(limit);

        if (listDTO == null || listDTO.isEmpty()) return Collections.emptyList();

        return listDTO.stream().map(top -> TopAmbassadorResponseDTO.builder()
                .accountId(top.getAccountId())
                .fullname(top.getFullname())
                .code(top.getCode())
                .avatar(top.getAvatar())
                .countDonations(top.getCountDonations())
                .totalDonation(top.getTotalDonation())
                .build()).toList();
    }

    @Override
    public List<TopAmbassadorResponseDTO> getTopDonors(Integer limit) {
        var listDTO = accountRepository.getTopDonors(limit);

        if (listDTO == null || listDTO.isEmpty()) return Collections.emptyList();

        return listDTO.stream().map(top -> TopAmbassadorResponseDTO.builder()
                .accountId(top.getAccountId())
                .fullname(top.getFullname())
                .code(top.getCode())
                .avatar(top.getAvatar())
                .countDonations(top.getCountDonations())
                .totalDonation(top.getTotalDonation())
                .build()).toList();
    }

    @Override
    public PageResponse<?> getAmbassadors(Integer page, Integer size, String fullname, String code, String email, String phone, BigDecimal min, BigDecimal max) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AmbassadorInterfaceDTO> accountPage = accountRepository.findAmbassadorsByFilters(fullname, code, email, phone, min, max, pageable);
        var res = accountPage.map(ambassador -> AmbassadorResponseDTO.builder()
                .accountId(ambassador.getAccountId())
                .avatar(ambassador.getAvatar())
                .code(ambassador.getCode())
                .fullname(ambassador.getFullname())
                .totalDonation(ambassador.getTotalDonation())
                .createdAt(ambassador.getCreatedAt())
                .countChallenges(ambassador.getCountChallenges())
                .build()).toList();

        return PageResponse.<AmbassadorResponseDTO>builder()
                .limit(size)
                .offset(page)
                .total((int) accountPage.getTotalElements())
                .content(res)
                .build();
    }

    public String generatePassword(int length) {
        StringBuilder password = new StringBuilder(12);
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            password.append(AppConfig.STRING.charAt(random.nextInt(AppConfig.STRING.length())));
        }
        return password.toString();
    }
}

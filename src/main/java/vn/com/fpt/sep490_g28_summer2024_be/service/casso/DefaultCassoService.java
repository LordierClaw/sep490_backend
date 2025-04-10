package vn.com.fpt.sep490_g28_summer2024_be.service.casso;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fpt.sep490_g28_summer2024_be.common.AppConfig;
import vn.com.fpt.sep490_g28_summer2024_be.common.DonationStatus;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.casso.ApiCassoResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.casso.PageInfoDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.ChallengeResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.donation.DonationResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.casso.TransactionDataDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.interfacedto.ProjectTransactionDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.*;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.repository.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class DefaultCassoService implements CassoService {
    private final ProjectRepository projectRepository;
    private final DonationRepository donationRepository;
    private final WrongDonationRepository wrongDonationRepository;
    private final AccountRepository accountRepository;
    private final ChallengeRepository challengeRepository;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final Executor executor;

    public DefaultCassoService(ProjectRepository projectRepository,
                               DonationRepository donationRepository,
                               WrongDonationRepository wrongDonationRepository,
                               AccountRepository accountRepository,
                               ChallengeRepository challengeRepository,
                               OkHttpClient okHttpClient,
                               ObjectMapper objectMapper,
                               @Qualifier("initExecutor") Executor executor) {
        this.projectRepository = projectRepository;
        this.donationRepository = donationRepository;
        this.wrongDonationRepository = wrongDonationRepository;
        this.accountRepository = accountRepository;
        this.challengeRepository = challengeRepository;
        this.okHttpClient = okHttpClient;
        this.objectMapper = objectMapper;
        this.executor = executor;
    }

    @Override
    @Transactional
    public DonationResponseDTO handleInPayment(TransactionDataDTO transactionDataDTO) {
        //donation request base init
        Donation savedDonation = donationRepository.save(initDonationByTransaction(transactionDataDTO));

        if(savedDonation.getDescription().toUpperCase().contains(AppConfig.REFER_PREFIX)){
            savedDonation = addReferToDonation(savedDonation);
        } else if (savedDonation.getDescription().toUpperCase().contains(AppConfig.CHALLENGE_PREFIX)) {
            savedDonation = addChallengeToDonation(savedDonation);
        }else {
            savedDonation = addProjectToDonation(savedDonation);
        }

        if (savedDonation.getDescription().toUpperCase().contains(AppConfig.ACCOUNT_PREFIX)){
            savedDonation = addAccountToDonation(savedDonation);
        }

        donationRepository.save(savedDonation);
        return DonationResponseDTO.builder()
                .donationId(savedDonation.getDonationId())
                .refer(savedDonation.getRefer() == null ? null : AccountDTO.builder()
                        .accountId(savedDonation.getRefer().getAccountId())
                        .build())
                .challenge(savedDonation.getChallenge() == null ? null : ChallengeResponseDTO.builder()
                        .challengeId(savedDonation.getChallenge().getChallengeId())
                        .build())
                .project(savedDonation.getProject() == null ? null : ProjectResponseDTO.builder()
                        .projectId(savedDonation.getProject().getProjectId())
                        .build())
                .transferredProject(savedDonation.getTransferredProject() == null ? null : ProjectResponseDTO.builder()
                        .projectId(savedDonation.getTransferredProject().getProjectId())
                        .build())
                .createdBy(savedDonation.getCreatedBy() == null ? null : AccountDTO.builder()
                        .accountId(savedDonation.getCreatedBy().getAccountId())
                        .build())
                .description(savedDonation.getDescription())
                .value(savedDonation.getValue())
                .build();
    }

    @Override
    @Transactional
    public DonationResponseDTO handleOutPayment(TransactionDataDTO transactionDataDTO) {
        Donation baseDonation = initDonationByTransaction(transactionDataDTO);
        Donation referDonation = donationRepository.getDonationByTid(transactionDataDTO.getDescription());
        if (referDonation != null){
            baseDonation.setProject(referDonation.getProject() == null ? null : referDonation.getProject());
            baseDonation.setChallenge(referDonation.getChallenge() == null ? null : referDonation.getChallenge());
            baseDonation.setTransferredProject(referDonation.getTransferredProject() == null ? null : referDonation.getTransferredProject());
            baseDonation.setRefer(referDonation.getRefer() == null ? null : referDonation.getRefer());
            baseDonation.setNote("Chuyển tiền lại cho chuyển khoản có mã "+referDonation.getTid());
            donationRepository.save(baseDonation);
            if (referDonation.getWrongDonation() != null) {
                wrongDonationRepository.save(WrongDonation.builder().donation(baseDonation).build());
            }
        }
        return DonationResponseDTO.builder()
                .donationId(baseDonation.getDonationId())
                .value(baseDonation.getValue())
                .build();
    }

    @Override
    @PostConstruct
    public void initMissingDonation() {
        handleInitMissingDonation();
    }

    @Async("initExecutor")
    public void handleInitMissingDonation() {
        CompletableFuture.runAsync(() -> {
            try {
                LocalDateTime fromDate = donationRepository.getLastDonationDate() == null ? LocalDateTime.now() : donationRepository.getLastDonationDate();
                int pageSize = 10;
                PageInfoDTO pageInfoDTO = new PageInfoDTO(1, 0);
                do {
                    String url = String.format(AppConfig.CASSO_URL, fromDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                            pageInfoDTO.getPage(), pageSize);

                    // Fetch data asynchronously
                    fetchApiCasso(url).thenAccept(apiCassoResponseDTO -> {
                        handleApiCasso(apiCassoResponseDTO);
                        pageInfoDTO.setTotalPage(apiCassoResponseDTO.getData().getTotalPages());
                    }).join();


                    int page = pageInfoDTO.getPage() + 1;
                    pageInfoDTO.setPage(page);
                } while (pageInfoDTO.getPage() <= pageInfoDTO.getTotalPage());
            } catch (Exception e) {
                // Handle any exceptions that occur
                e.printStackTrace();
            }
        }, executor);
    }

    @Async("initExecutor")
    public CompletableFuture<ApiCassoResponseDTO> fetchApiCasso(String url){
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Apikey AK_CS.8e4670404c6a11ef9068f9e08e26656f.jIAdCzNome8LEwqUIYGh65XdcPcYloPybfsTgdZrfiCqK6BC2lYfenHYzuHp26Qo6MwQ9rRx")
                    .build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                // fetch response (body)
                String responseBody = response.body() == null ? null : response.body().string();

                // Parse the JSON response and map it to your entity
                return  objectMapper.readValue(responseBody, ApiCassoResponseDTO.class);
            } catch (IOException e) {
                throw new AppException(ErrorCode.HTTP_FETCH_FAILED);
            } catch (Exception e){
                throw new AppException(ErrorCode.HTTP_MAPPING_FAILED);
            }
        }, executor);
    }

    @Transactional
    public void handleApiCasso(ApiCassoResponseDTO apiCassoResponseDTO){
        apiCassoResponseDTO.getData().getRecords().forEach(cassoRecordDTO -> {
            if (!donationRepository.existsDonationById(cassoRecordDTO.getId().toString())){
                DonationStatus status = cassoRecordDTO.getAmount().compareTo(BigDecimal.ZERO) > 0 ? DonationStatus.IN : DonationStatus.OUT;
                switch (status) {
                    case IN ->
                        handleInPayment(TransactionDataDTO.builder()
                                .id(cassoRecordDTO.getId())
                                .tid(cassoRecordDTO.getTid())
                                .description(cassoRecordDTO.getDescription())
                                .amount(cassoRecordDTO.getAmount())
                                .cusumBalance(cassoRecordDTO.getCusumBalance())
                                .when(cassoRecordDTO.getWhen())
                                .bankSubAccId(cassoRecordDTO.getBankSubAccId())
                                .virtualAccount(cassoRecordDTO.getVirtualAccount())
                                .virtualAccountName(cassoRecordDTO.getVirtualAccountName())
                                .corresponsiveName(cassoRecordDTO.getCorresponsiveName())
                                .corresponsiveAccount(cassoRecordDTO.getCorresponsiveAccount())
                                .corresponsiveBankName(cassoRecordDTO.getCorresponsiveBankName())
                                .corresponsiveBankId(cassoRecordDTO.getCorresponsiveBankId())
                                .build());

                    case OUT ->
                        handleOutPayment(TransactionDataDTO.builder()
                                .id(cassoRecordDTO.getId())
                                .tid(cassoRecordDTO.getTid())
                                .description(cassoRecordDTO.getDescription())
                                .amount(cassoRecordDTO.getAmount())
                                .cusumBalance(cassoRecordDTO.getCusumBalance())
                                .when(cassoRecordDTO.getWhen())
                                .bankSubAccId(cassoRecordDTO.getBankSubAccId())
                                .virtualAccount(cassoRecordDTO.getVirtualAccount())
                                .virtualAccountName(cassoRecordDTO.getVirtualAccountName())
                                .corresponsiveName(cassoRecordDTO.getCorresponsiveName())
                                .corresponsiveAccount(cassoRecordDTO.getCorresponsiveAccount())
                                .corresponsiveBankName(cassoRecordDTO.getCorresponsiveBankName())
                                .corresponsiveBankId(cassoRecordDTO.getCorresponsiveBankId())
                                .build());
                }
            }
        });
    }

    public Donation initDonationByTransaction(TransactionDataDTO transactionDataDTO){
        return Donation.builder()
                .id(transactionDataDTO.getId().toString())
                .tid(transactionDataDTO.getTid())
                .value(transactionDataDTO.getAmount())
                .createdAt(transactionDataDTO.getWhen())
                .description(transactionDataDTO.getDescription())
                .bankSubAccId(transactionDataDTO.getBankSubAccId())
                .bankName(transactionDataDTO.getBankName())
                .corresponsiveName(transactionDataDTO.getCorresponsiveName())
                .corresponsiveAccount(transactionDataDTO.getCorresponsiveAccount())
                .corresponsiveBankId(transactionDataDTO.getCorresponsiveBankId())
                .corresponsiveBankName(transactionDataDTO.getCorresponsiveBankName())
                .build();
    }

    public Donation addReferToDonation(Donation baseDonation){
        Account refer = accountRepository.findReferAccountByDescription(baseDonation.getDescription());
        if (refer != null){
            baseDonation.setRefer(refer);
            return addProjectToDonation(baseDonation);
        }
        return addChallengeToDonation(baseDonation);
    }

    public Donation addChallengeToDonation(Donation baseDonation){
        Challenge challenge = challengeRepository.findChallengeByDescription(baseDonation.getDescription());
        if(challenge != null){
            baseDonation.setChallenge(challenge);
            ProjectTransactionDTO projectInChallenge = projectRepository.findValidProjectByChallengeIdAndDescription(challenge.getChallengeId(), baseDonation.getDescription(), true);
            if (projectInChallenge != null){
                baseDonation.setProject(Project.builder()
                                .projectId(projectInChallenge.getProjectId())
                        .build());
                if (isEnoughDonation(projectInChallenge)){
                    baseDonation = handleNotValidProjectInChallenge(baseDonation, challenge.getChallengeId());
                }
            }else {
                baseDonation = handleNotValidProjectInChallenge(baseDonation, challenge.getChallengeId());
            }
            return baseDonation;
        }else {
            return addProjectToDonation(baseDonation);
        }
    }

    public Donation addProjectToDonation(Donation baseDonation){
        baseDonation = donationRepository.save(baseDonation);
        ProjectTransactionDTO ref = projectRepository.findProjectByCampaignIdAndDonationDescriptionAndStatus(baseDonation.getDescription(), null, null, true);
        if (ref != null){
            baseDonation.setProject(Project.builder()
                    .projectId(ref.getProjectId())
                    .build());
        }else{
            baseDonation.setProject(null);
            baseDonation = handleNoneProject(baseDonation, "Mã dự án không hợp lệ");
        }

        if (ref != null && (ref.getStatus() != 2 || ref.getTotalDonation().compareTo(ref.getGoal()) >= 0)){
            Project refProject = projectRepository.findById(ref.getProjectId()).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));
            ProjectTransactionDTO minTotalDonationInCampaignProject = projectRepository.findProjectByCampaignIdAndDonationDescriptionAndStatus(null, refProject.getCampaign().getCampaignId(), 2, false);
            if (minTotalDonationInCampaignProject != null){
                baseDonation.setTransferredProject(Project.builder()
                        .projectId(minTotalDonationInCampaignProject.getProjectId())
                        .build());
                baseDonation.setNote("Mã dự án mà bạn quyên góp hiện tại đã đủ quyên góp, hoặc dự án đang trong trạng thái không cần quyên góp nên hệ thống sẽ chuyển sang dự án "+minTotalDonationInCampaignProject.getCode());
            }else {
                baseDonation = handleNoneProject(baseDonation, "Mã dự án mà bạn quyên góp hiện tại đã đủ quyên góp, hoặc dự án đang trong trạng thái không cần quyên góp");
            }
        }
        return baseDonation;
    }

    public Donation handleNoneProject(Donation baseDonation, String reason){
        donationRepository.save(baseDonation);
        ProjectTransactionDTO minTotalDonationProject = projectRepository.findProjectByCampaignIdAndDonationDescriptionAndStatus(null, null, 2, false);
        if (minTotalDonationProject != null){
            baseDonation.setTransferredProject(Project.builder()
                    .projectId(minTotalDonationProject.getProjectId())
                    .build());
            baseDonation.setNote(reason+" "+minTotalDonationProject.getCode());
        }else{
            baseDonation.setNote(reason + " và hiện không có dự án nào có thể chuyển nên chuyển khoản nhận trạng thái pending");
            wrongDonationRepository.save(WrongDonation.builder()
                            .donation(baseDonation)
                    .build());
        }
        return baseDonation;
    }

    public Donation handleNotValidProjectInChallenge(Donation baseDonation, BigInteger challengeId){
        ProjectTransactionDTO validProjectInChallenge = projectRepository.findValidProjectByChallengeIdAndDescription(challengeId, null, false);
        if (validProjectInChallenge != null){
            baseDonation.setProject(Project.builder()
                    .projectId(validProjectInChallenge.getProjectId())
                    .build());
            baseDonation.setNote("Không có mã dự án hợp lệ trong thử thách nên chuyển sang dự án "+ validProjectInChallenge.getCode());
        }else {
            baseDonation = handleNoneProject(baseDonation,"Mã dự án trong nội dung chuyển khoản không hợp lệ");
        }
        return baseDonation;
    }

    public Donation addAccountToDonation(Donation baseDonation){
        Account account = accountRepository.findAccountByDescription(baseDonation.getDescription());
        if (account != null){
            baseDonation.setCreatedBy(account);
        }
        return baseDonation;
    }

    public Boolean isEnoughDonation(ProjectTransactionDTO projectTransactionDTO){
        return projectTransactionDTO.getGoal().compareTo(projectTransactionDTO.getTotalDonation()) <= 0;
    }

}

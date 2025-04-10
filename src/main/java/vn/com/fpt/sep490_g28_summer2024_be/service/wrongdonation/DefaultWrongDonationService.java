package vn.com.fpt.sep490_g28_summer2024_be.service.wrongdonation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.interfacedto.ProjectTransactionDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Donation;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Project;
import vn.com.fpt.sep490_g28_summer2024_be.repository.DonationRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.WrongDonationRepository;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class DefaultWrongDonationService implements WrongDonationService{

    private final WrongDonationRepository wrongDonationRepository;
    private final ProjectRepository projectRepository;
    private final DonationRepository donationRepository;
    private final Executor executor;

    public DefaultWrongDonationService(WrongDonationRepository wrongDonationRepository, ProjectRepository projectRepository,
                                       DonationRepository donationRepository,
                                       @Qualifier("scheduleExecutor") Executor executor) {
        this.wrongDonationRepository = wrongDonationRepository;
        this.projectRepository = projectRepository;
        this.donationRepository = donationRepository;
        this.executor = executor;
    }

    @Override
    @Async("scheduleExecutor")
    @Transactional
    public CompletableFuture<Void> updateWrongDonation() {
        return CompletableFuture.runAsync(() -> {
            wrongDonationRepository.findAll().forEach(wrongDonation -> {
                Donation donation = wrongDonation.getDonation();
                if (donation.getValue().compareTo(BigDecimal.ZERO) < 0) {
                    Donation referDonation = donationRepository.getDonationByTid(donation.getDescription());
                    System.out.println("refer-donation: "+referDonation);

                    if (referDonation != null) {
                        donation.setTransferredProject(referDonation.getTransferredProject());
                        donation.setProject(referDonation.getProject());
                        donation.setCreatedBy(referDonation.getCreatedBy());
                        donation.setChallenge(referDonation.getChallenge());
                        donationRepository.save(donation);
                        if (referDonation.getWrongDonation() == null && donation.getWrongDonation() != null) {
                            wrongDonationRepository.delete(donation.getWrongDonation());
                        }

                    }
                } else {
                    Project project = donation.getProject();
                    if (project != null) {
                        ProjectTransactionDTO validProject = projectRepository.findProjectByCampaignIdAndDonationDescriptionAndStatus(
                                project.getCode(), project.getCampaign().getCampaignId(), 2, false);

                        validProject = validProject != null ? validProject : projectRepository.findProjectByCampaignIdAndDonationDescriptionAndStatus(
                                null, project.getCampaign().getCampaignId(), 2, false);

                        validProject = validProject != null ? validProject : projectRepository.findProjectByCampaignIdAndDonationDescriptionAndStatus(
                                null, null, 2, false);

                        if (validProject != null) {
                            donation.setTransferredProject(Project.builder()
                                    .projectId(validProject.getProjectId())
                                    .code(validProject.getCode())
                                    .build());

                            donationRepository.save(donation);
                            if (donation.getWrongDonation() != null) {
                                wrongDonationRepository.delete(donation.getWrongDonation());
                            }
                        }

                    } else {
                        ProjectTransactionDTO validProject = projectRepository.findProjectByCampaignIdAndDonationDescriptionAndStatus(
                                null, null, 2, false);

                        if (validProject != null) {
                            donation.setTransferredProject(Project.builder()
                                    .projectId(validProject.getProjectId())
                                    .code(validProject.getCode())
                                    .build());
                            donationRepository.save(donation);
                            if (donation.getWrongDonation() != null) {
                                wrongDonationRepository.delete(donation.getWrongDonation());
                            }
                        }
                    }
                }
            });
        }, executor);
    }


}

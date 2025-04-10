package vn.com.fpt.sep490_g28_summer2024_be.cron;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.com.fpt.sep490_g28_summer2024_be.repository.WrongDonationRepository;
import vn.com.fpt.sep490_g28_summer2024_be.service.wrongdonation.WrongDonationService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WrongDonationCron {

    private final WrongDonationRepository wrongDonationRepository;
    private final WrongDonationService wrongDonationService;

    @Scheduled(cron = "0 0 0 * * SUN") // 24h sunday each week
    public void scheduleUpdateWrongDonationsByCron() {
        wrongDonationService.updateWrongDonation().join();
    }

}

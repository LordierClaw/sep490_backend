package vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.interfacedto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ChallengeInterfaceDTO {

    BigInteger getChallengeId();
    String getTitle();
    String getChallengeCode();
    String getSlug();
    String getThumbnail();
    String getContent();
    BigInteger getAccountId();
    String getCode();
    String getFullname();
    String getAvatar();
    BigDecimal getGoal();
    LocalDateTime getCreatedAt();
    LocalDate getFinishedAt();
    BigDecimal getTotalDonation();
}

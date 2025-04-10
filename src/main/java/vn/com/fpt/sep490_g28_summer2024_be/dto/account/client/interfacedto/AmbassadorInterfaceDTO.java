package vn.com.fpt.sep490_g28_summer2024_be.dto.account.client.interfacedto;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

public interface AmbassadorInterfaceDTO {
    BigInteger getAccountId();
    String getCode();
    String getFullname();
    String getAvatar();
    LocalDateTime getCreatedAt();
    BigDecimal getTotalDonation();
    Long getCountChallenges();
}

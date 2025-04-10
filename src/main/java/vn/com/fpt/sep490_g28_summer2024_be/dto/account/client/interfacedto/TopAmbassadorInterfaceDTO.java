package vn.com.fpt.sep490_g28_summer2024_be.dto.account.client.interfacedto;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface TopAmbassadorInterfaceDTO {
    BigInteger getAccountId();
    String getCode();
    String getFullname();
    String getAvatar();
    BigDecimal getTotalDonation();
    Long getCountDonations();
}

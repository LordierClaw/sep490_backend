package vn.com.fpt.sep490_g28_summer2024_be.dto.project.interfacedto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

public interface ProjectInterfaceDTO {
    BigInteger getProjectId();
    String getTitle();
    String getCode();
    BigInteger getCampaignId();
    String getCampaignTitle();
    String getAddress();
    String getWard();
    String getBackground();
    String getDistrict();
    String getProvince();
    BigDecimal getTotalBudget();
    BigDecimal getAmountNeededToRaise();
    BigDecimal getTotalDonation();
    LocalDateTime getCreatedAt();
    Integer getStatus();
}

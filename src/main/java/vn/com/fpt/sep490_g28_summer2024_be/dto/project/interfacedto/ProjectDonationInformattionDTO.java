package vn.com.fpt.sep490_g28_summer2024_be.dto.project.interfacedto;


import java.math.BigDecimal;
import java.math.BigInteger;

public interface ProjectDonationInformattionDTO {

    BigInteger getProjectId();
    BigDecimal getTarget();
    BigDecimal getTotalDonation();
}

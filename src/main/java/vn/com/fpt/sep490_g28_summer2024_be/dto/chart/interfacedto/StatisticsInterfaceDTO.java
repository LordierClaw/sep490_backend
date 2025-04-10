package vn.com.fpt.sep490_g28_summer2024_be.dto.chart.interfacedto;

import java.math.BigDecimal;

public interface StatisticsInterfaceDTO {
    BigDecimal getTotalDonation();
    Long getTotalNumberDonations();
    Long getTotalProjects();
    Long getTotalOnGoingProjects();
    Long getTotalProcessingProjects();
    Long getTotalDoneProjects();

}

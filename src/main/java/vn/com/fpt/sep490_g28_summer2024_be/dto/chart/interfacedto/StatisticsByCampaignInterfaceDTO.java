package vn.com.fpt.sep490_g28_summer2024_be.dto.chart.interfacedto;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface StatisticsByCampaignInterfaceDTO {

    BigInteger getId();
    BigDecimal getValue();
    String getLabel();

}

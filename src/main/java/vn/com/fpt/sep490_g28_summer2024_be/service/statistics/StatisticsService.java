package vn.com.fpt.sep490_g28_summer2024_be.service.statistics;


import vn.com.fpt.sep490_g28_summer2024_be.dto.chart.ChartResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.chart.interfacedto.StatisticsInterfaceDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public interface StatisticsService {
    BigDecimal getTotalSponsorValue(Integer month, Integer year);

    BigDecimal getTotalDonation(Integer month, Integer year);

    BigDecimal getTotalWrongDonation(Integer month, Integer year);

    Long getTotalCountDonations(Integer month, Integer year);

    List<ChartResponseDTO> getDataPieChart(Integer month, Integer year);

    List<ChartResponseDTO> getDataLineChart(Integer year);

    List<ChartResponseDTO> getDataBarChart(Integer month, Integer year);

    StatisticsInterfaceDTO getDonationStaticDataByCampaign(BigInteger campaignId, Integer year);

    StatisticsInterfaceDTO getProjectStaticDataByCampaign(BigInteger campaignId, Integer year);
}

package vn.com.fpt.sep490_g28_summer2024_be.service.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.fpt.sep490_g28_summer2024_be.dto.chart.ChartResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.chart.interfacedto.StatisticsByCampaignInterfaceDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.chart.interfacedto.StatisticsInterfaceDTO;
import vn.com.fpt.sep490_g28_summer2024_be.repository.CampaignRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.DonationRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.SponsorRepository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultStatisticsService implements StatisticsService {
    public final ProjectRepository projectRepository;
    public final DonationRepository donationRepository;
    public final CampaignRepository campaignRepository;
    public final SponsorRepository sponsorRepository;

    @Override
    public BigDecimal getTotalSponsorValue(Integer month, Integer year) {
        return sponsorRepository.getAllSponsorValueByMonth(month, year);
    }

    @Override
    public BigDecimal getTotalDonation(Integer month, Integer year) {
        return donationRepository.getTotalDonationByMonth(year, month);
    }

    @Override
    public BigDecimal getTotalWrongDonation(Integer month, Integer year) {
        return donationRepository.getTotalWrongDonationByMonth(year, month);
    }

    @Override
    public Long getTotalCountDonations(Integer month, Integer year) {
        return donationRepository.getTotalCountDonationByMonth(year, month);
    }

    @Override
    public List<ChartResponseDTO> getDataPieChart(Integer month, Integer year) {
        List<StatisticsByCampaignInterfaceDTO> campaign = campaignRepository.getCampaignPieChart(month, year);
        return campaign.stream().map(c -> ChartResponseDTO.builder()
                .id(c.getId())
                .value(c.getValue())
                .label(c.getLabel())
                .build()).toList();
    }

    @Override
    public List<ChartResponseDTO> getDataLineChart(Integer year) {
        var dataChart = donationRepository.getTotalDonationByYear(year);

        return dataChart == null ? Collections.emptyList() : dataChart.stream().map(data -> ChartResponseDTO.builder()
                .label("Tháng "+data.getMonthNumber().toString())
                .value(data.getValue())
                .build()).toList();
    }

    @Override
    public List<ChartResponseDTO> getDataBarChart(Integer month, Integer year) {
        var dataChart = donationRepository.getTotalDonationByWeekOfMonth(year, month);
        return dataChart == null ? Collections.emptyList() : dataChart.stream().map(data -> ChartResponseDTO.builder()
                .label("Tuần "+data.getWeekOfMonth().toString())
                .value(data.getValue())
                .build()).toList();
    }

    @Override
    public StatisticsInterfaceDTO getDonationStaticDataByCampaign(BigInteger campaignId, Integer year) {
        return campaignRepository.getDonationStaticByCampaign(campaignId, year);
    }

    @Override
    public StatisticsInterfaceDTO getProjectStaticDataByCampaign(BigInteger campaignId, Integer year) {
        return projectRepository.getProjectStaticByCampaign(campaignId, year);
    }


}

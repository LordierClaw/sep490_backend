package vn.com.fpt.sep490_g28_summer2024_be.service.exportFile;


import java.math.BigInteger;
import java.util.List;

public interface ExportFileService {
//    byte[] exportDataToExcel(List<?> dataList, String[] fieldNames, String sheetName);

    byte[] exportCampaignReportToPdf(BigInteger campaignId, String title);

    byte[] exportTransferStatements(BigInteger projectId);
}

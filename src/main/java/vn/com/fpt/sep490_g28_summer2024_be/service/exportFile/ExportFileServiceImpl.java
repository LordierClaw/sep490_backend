package vn.com.fpt.sep490_g28_summer2024_be.service.exportFile;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;

import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.repository.DonationRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import java.time.format.DateTimeFormatter;


@Service
@Slf4j
@RequiredArgsConstructor
public class ExportFileServiceImpl implements ExportFileService {

    private final ProjectRepository projectRepository;
    private final DonationRepository donationRepository;

//    @Override
//    public byte[] exportDataToExcel(List<?> dataList, String[] fieldNames, String sheetName) {
//        try {
//            // Tạo một workbook mới sử dụng thư viện Apache POI
//            Workbook workbook = new XSSFWorkbook();
//            // Tạo một sheet mới với tên được chỉ định
//            Sheet sheet = workbook.createSheet(sheetName);
//
//            // Tạo hàng đầu tiên cho tiêu đề cột
//            Row headerRow = sheet.createRow(0);
//            for (int i = 0; i < fieldNames.length; i++) {
//                // Thêm tiêu đề vào từng cột
//                headerRow.createCell(i).setCellValue(fieldNames[i]);
//            }
//            // Tạo các hàng dữ liệu từ danh sách đối tượng được truyền vào
//            int rowNum = 1;
//            for (Object data : dataList) {
//                Row row = sheet.createRow(rowNum++);
//                for (int i = 0; i < fieldNames.length; i++) {
//                    // Truy cập đến trường dữ liệu qua reflection
//                    Field field = data.getClass().getDeclaredField(fieldNames[i]);
//                    field.setAccessible(true);
//                    Object value = field.get(data);
//                    if (value != null) {
//                        // Đặt giá trị cho từng ô trong hàng, chuyển đổi tất cả thành chuỗi
//                        row.createCell(i).setCellValue(value.toString());
//                    } else {
//                        // Nếu giá trị là null, đặt ô trống
//                        row.createCell(i).setCellValue("");
//                    }
//                }
//            }
//
//            // Viết dữ liệu vào một luồng đầu ra
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            workbook.write(outputStream);
//            workbook.close();
//
//            return outputStream.toByteArray();
//        } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
//            throw new AppException(ErrorCode.EXPORT_EXCEL_FAILED);
//        }
//    }

    @Override
    public byte[] exportCampaignReportToPdf(BigInteger campaignId, String title) {
        try {
            // Tạo tài liệu PDF mới
            PDDocument document = new PDDocument();

            // Lấy thông tin cho status = 2 (Dự án đang chạy)
//            BigDecimal totalBudget = projectRepository.getTotalBudgetByCampaignIdAndStatus(campaignId, 2);
//            BigDecimal totalDonation = projectRepository.getTotalDonationByCampaignId(campaignId, 2);
//            BigDecimal remainingAmount = totalBudget.subtract(totalDonation);
//            BigDecimal excessAmount = totalDonation.subtract(totalBudget).max(BigDecimal.ZERO);
//            int numberProjectRunning = projectRepository.countProjectsByCampaignIdAndStatus(campaignId, 2);

            // Lấy thông tin cho status = 3 (Dự án đã hoàn thành)
//            BigDecimal totalBudgetStatus3 = projectRepository.getTotalBudgetByCampaignIdAndStatus(campaignId, 3);
//            BigDecimal totalDonationStatus3 = projectRepository.getTotalDonationByCampaignId(campaignId, 3);
//            BigDecimal remainingAmountStatus3 = totalBudgetStatus3.subtract(totalDonationStatus3);
//            BigDecimal excessAmountStatus3 = totalDonationStatus3.subtract(totalBudgetStatus3).max(BigDecimal.ZERO);
//            int numberProjectCompleted = projectRepository.countProjectsByCampaignIdAndStatus(campaignId, 3);

            // Tải font để hỗ trợ tiếng Việt và các ký tự đặc biệt
            PDType0Font font = loadFont(document, "/OpenSans-VariableFont_wdth,wght.ttf");
            PDType0Font boldFont = loadFont(document, "/OpenSans_Condensed-Bold.ttf");

            // Tạo trang PDF mới và thêm vào tài liệu
            PDPage page = new PDPage();
            document.addPage(page);

            // Tạo luồng nội dung để ghi vào trang PDF
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(font, 12);

            // Thiết lập các thông số lề và vị trí bắt đầu
            int margin = 50;
            int yStart = 750;
            int yOffset = yStart;
            int rowHeight = 20;

            // Ghi tiêu đề vào tài liệu PDF
            contentStream.beginText();
            contentStream.setFont(boldFont, 16);
            contentStream.newLineAtOffset(margin, yOffset);
            contentStream.showText(title);
            contentStream.endText();
            yOffset -= rowHeight * 2;

            // Tạo bảng cho các dự án đang chạy (status = 2) và các dự án đã hoàn thành (status = 3)
//            String[][] data = {
//                    {"Thông tin", "Giá trị"},
//                    {"Số dự án đang chạy", String.valueOf(numberProjectRunning)},
//                    {"Tổng số tiền đã donate", String.valueOf(totalDonation)},
//                    {"Tổng Số tiền cần donate ", String.valueOf(totalBudget)},
//                    {"Tổng số tiền còn thiếu ", String.valueOf(remainingAmount)},
//                    {"Tổng số tiền thừa ", String.valueOf(excessAmount)},
//                    {""}, // Chèn một hàng trống giữa hai phần
//                    {"Số dự án đã hoàn thành", String.valueOf(numberProjectCompleted)},
//                    {"Tổng số tiền đã donate ", String.valueOf(totalDonationStatus3)},
//                    {"Tổng Số tiền cần donate ", String.valueOf(totalBudgetStatus3)},
//                    {"Tổng số tiền còn thiếu ", String.valueOf(remainingAmountStatus3)},
//                    {"Tổng số tiền thừa ", String.valueOf(excessAmountStatus3)}
//            };

//            drawTable(document, page, yOffset, margin, data, font, boldFont);

            contentStream.close();
            // Lưu tài liệu PDF vào luồng byte
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            document.close();

            // Trả về mảng byte của tài liệu PDF
            return baos.toByteArray();
        } catch (IOException e) {
            throw new AppException(ErrorCode.EXPORT_EXCEL_FAILED);
        }
    }

    @Override
    @Transactional
    public byte[] exportTransferStatements(BigInteger projectId) {
        try {
            var listDonations = donationRepository.getAllDonationByProjectId(projectId);

            // Tạo Workbook và Sheet
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("BÁO CÁO TÀI CHÍNH DỰ ÁN");

            if (listDonations != null || !listDonations.isEmpty()){
                // Tạo tiêu đề cột với màu nền
                Row overviewRow1 = sheet.createRow(0);

                // Tạo CellStyle cho phần báo cáo
                XSSFCellStyle totalDonationCellStyle = workbook.createCellStyle();
                totalDonationCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                totalDonationCellStyle.setAlignment(HorizontalAlignment.CENTER);
                totalDonationCellStyle.setBorderTop(BorderStyle.THIN);
                totalDonationCellStyle.setBorderBottom(BorderStyle.THIN);
                totalDonationCellStyle.setBorderLeft(BorderStyle.THIN);
                totalDonationCellStyle.setBorderRight(BorderStyle.THIN);


                //Tạo CellStyle cho phần sao kê
                XSSFCellStyle transactionCellStyle = workbook.createCellStyle();
                transactionCellStyle.setBorderTop(BorderStyle.THIN);
                transactionCellStyle.setBorderBottom(BorderStyle.THIN);
                transactionCellStyle.setBorderLeft(BorderStyle.THIN);
                transactionCellStyle.setBorderRight(BorderStyle.THIN);

                //Tạo CellStyle cho phần header sao kê
                XSSFCellStyle transactionHeaderCellStyle = workbook.createCellStyle();
                transactionHeaderCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                transactionHeaderCellStyle.setAlignment(HorizontalAlignment.CENTER);
                transactionHeaderCellStyle.setBorderTop(BorderStyle.THIN);
                transactionHeaderCellStyle.setBorderBottom(BorderStyle.THIN);
                transactionHeaderCellStyle.setBorderLeft(BorderStyle.THIN);
                transactionHeaderCellStyle.setBorderRight(BorderStyle.THIN);


                String[] headerDonations = {"STT","THỜI GIAN", "MÃ GIAO DỊCH", "SỐ TIỀN", "NỘI DUNG CHUYỂN KHOẢN", "TIỀN THỪA ĐƯỢC CHUYỂN ĐẾN", "GHI CHÚ", "WRONG"};
                Row donationHeaderRow = sheet.createRow(5);
                for (int i = 0; i < headerDonations.length; i++) {
                    Cell cell = donationHeaderRow.createCell(i);
                    cell.setCellValue(headerDonations[i]);
                    cell.setCellStyle(transactionCellStyle);
                }

                int[] rowNum = {6};
                int[] stt = {1};
                listDonations.forEach(donation -> {
                    Row donationRow = sheet.createRow(rowNum[0]);

                    Cell cell0 = donationRow.createCell(0);
                    cell0.setCellValue(stt[0]);
                    cell0.setCellStyle(transactionCellStyle);

                    Cell cell1 = donationRow.createCell(1);
                    cell1.setCellValue(donation.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    cell1.setCellStyle(transactionCellStyle);

                    Cell cell2 = donationRow.createCell(2);
                    cell2.setCellValue(donation.getTid());
                    cell2.setCellStyle(transactionCellStyle);

                    Cell cell3 = donationRow.createCell(3);
                    cell3.setCellValue(donation.getValue().toString());
                    cell3.setCellStyle(transactionCellStyle);

                    Cell cell4 = donationRow.createCell(4);
                    cell4.setCellValue(donation.getDescription());
                    cell4.setCellStyle(transactionCellStyle);

                    Cell cell5 = donationRow.createCell(5);
                    cell5.setCellValue(donation.getTransferredProject() == null ? "" : "https://fpt.sucmanh2000.com/du-an/%s".formatted(donation.getTransferredProject().getSlug()));
                    cell5.setCellStyle(transactionCellStyle);

                    Cell cell6 = donationRow.createCell(6);
                    cell6.setCellValue(donation.getNote());
                    cell6.setCellStyle(transactionCellStyle);

                    Cell cell7 = donationRow.createCell(7);
                    cell7.setCellValue(donation.getWrongDonation() == null ? "" : "PENDING");
                    cell7.setCellStyle(transactionCellStyle);

                    stt[0]++;
                    rowNum[0]++;
                });

                // Tự động điều chỉnh kích thước cột
                for (int i = 0; i < headerDonations.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }



            // Ghi Workbook vào ByteArrayOutputStream
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            return out.toByteArray();
        } catch (IOException e) {
            throw new AppException(ErrorCode.EXPORT_EXCEL_FAILED);
        }
    }

    // Phương thức để tải font
    private PDType0Font loadFont(PDDocument document, String fontPath) throws IOException {
        InputStream fontStream = getClass().getResourceAsStream(fontPath);
        if (fontStream == null) {
            throw new IOException("Font file not found: " + fontPath);
        }
        return PDType0Font.load(document, fontStream);
    }

    // Phương thức để vẽ bảng
    private void drawTable(PDDocument doc, PDPage page, float y, float margin, String[][] content, PDType0Font font, PDType0Font boldFont) throws IOException {
        PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true);
        float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
        float yPosition = y;
        float cellMargin = 5f;

        for (int i = 0; i < content.length; i++) {
            for (int j = 0; j < content[i].length; j++) {
                float cellWidth = tableWidth / content[i].length;
                float textX = margin + j * cellWidth + cellMargin;
                float textY = yPosition - 15;

                if (i == 0 || (i == 7 && content[i][0].isEmpty())) {
                    // Đặt màu nền cho tiêu đề hoặc các hàng đặc biệt
                    contentStream.setNonStrokingColor(255, 204, 153); // Màu cam nhạt
                    contentStream.addRect(margin + j * cellWidth, yPosition - 20, cellWidth, 20);
                    contentStream.fill();
                    contentStream.setNonStrokingColor(0, 0, 0); // Đặt lại màu chữ là màu đen
                    contentStream.setFont(boldFont, 12);
                } else {
                    contentStream.setFont(font, 12);
                }

                contentStream.beginText();
                contentStream.newLineAtOffset(textX, textY);
                contentStream.showText(content[i][j]);
                contentStream.endText();

                // Draw cell borders
                contentStream.setStrokingColor(0, 0, 0);
                contentStream.addRect(margin + j * cellWidth, yPosition - 20, cellWidth, 20);
                contentStream.stroke();
            }
            yPosition -= 20;
        }
        contentStream.close();
    }


}



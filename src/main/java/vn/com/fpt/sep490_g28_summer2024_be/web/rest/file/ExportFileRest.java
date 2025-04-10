package vn.com.fpt.sep490_g28_summer2024_be.web.rest.file;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.fpt.sep490_g28_summer2024_be.service.exportFile.ExportFileService;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportFileRest {
    private final ExportFileService exportFileService;

    @GetMapping("/excel/{id}")
    public ResponseEntity<byte[]> exportProjectByIdToPdf(@PathVariable BigInteger id) {
        byte[] pdfBytes = exportFileService.exportTransferStatements(id);
        return ResponseEntity.ok().body(pdfBytes);
    }
}

package vn.com.fpt.sep490_g28_summer2024_be.web.rest.file;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;

import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseServiceImpl;

import java.io.IOException;
import java.math.BigInteger;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileUpload {

    private final FirebaseServiceImpl firebaseServiceImpl;

    @PostMapping("/upload")
    public ApiResponse<?> upload(@RequestBody MultipartFile[] file) throws IOException {
        return ApiResponse.builder()
                .message("Upload thành viên!")
                .data(firebaseServiceImpl.uploadMultipleFile(file, BigInteger.valueOf(1), "campaign-images"))
                .build();
    }

    @DeleteMapping("/delete")
    public ApiResponse<?> delete(@RequestParam String filepath) throws IOException {
        return ApiResponse.builder()
                .message(filepath)
                .data(firebaseServiceImpl.deleteFileByPath(filepath))
                .build();
    }
}

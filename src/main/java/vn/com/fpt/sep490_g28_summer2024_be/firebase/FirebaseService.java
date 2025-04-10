package vn.com.fpt.sep490_g28_summer2024_be.firebase;

import com.google.cloud.storage.Storage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public interface FirebaseService {
    Storage createStorage() throws IOException;
    String uploadOneFile(MultipartFile file, BigInteger id, String folder) throws IOException;
    List<String> uploadMultipleFile(MultipartFile[] file, BigInteger id, String folder) throws IOException;
    Boolean deleteFileByPath(String path) throws IOException;
    Boolean filesIsImage(MultipartFile[] files);
}

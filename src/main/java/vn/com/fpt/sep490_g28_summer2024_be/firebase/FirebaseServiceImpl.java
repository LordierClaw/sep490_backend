package vn.com.fpt.sep490_g28_summer2024_be.firebase;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FirebaseServiceImpl implements FirebaseService {
    private final String bucketName = "sep490-g28-summer24.appspot.com";

    @Override
    public Storage createStorage() throws IOException {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(getClass().getResourceAsStream("/"+
                        "sep490-g28-summer24-firebase-adminsdk-do2q8-901df31a42.json")))
                .build()
                .getService();
        return storage;
    }

    @Override
    public String uploadOneFile(MultipartFile file, BigInteger id, String folder) throws IOException {
        byte[] fileContent = file.getBytes();
        String originalFilename = file.getOriginalFilename();


        int maxFilenameLength = 250;

        // Tính toán độ dài của phần tiền tố đường dẫn (thư mục + id + dấu gạch chéo "/")
        String pathPrefix = "%s/%s/".formatted(folder, id);
        int pathPrefixLength = pathPrefix.length();

        // Lấy đuôi file (ví dụ: ".jpg", ".png")
        String fileExtension = "";
        int extensionIndex = originalFilename.lastIndexOf(".");

        if (extensionIndex > 0) {
            fileExtension = originalFilename.substring(extensionIndex);
            originalFilename = originalFilename.substring(0, extensionIndex); // Cắt bỏ đuôi file khỏi tên gốc để xử lý cắt ngắn
        }


        String uuid = UUID.randomUUID().toString();

        // Điều chỉnh độ dài tối đa của tên file để đảm bảo không vượt quá giới hạn
        int availableFilenameLength = maxFilenameLength - pathPrefixLength;
        int maxOriginalFilenameLength = availableFilenameLength - uuid.length() - 1 - fileExtension.length(); // Trừ đi độ dài của UUID, dấu gạch dưới, và đuôi file

        // Nếu tên file gốc dài hơn mức cho phép, cắt bớt nó
        if (originalFilename.length() > maxOriginalFilenameLength) {
            originalFilename = originalFilename.substring(0, maxOriginalFilenameLength);
        }

        // Tạo tên file duy nhất bằng cách thêm UUID trước tên file gốc
        String uniqueFilename = uuid + "_" + originalFilename + fileExtension;
        String fullPath = pathPrefix + uniqueFilename;

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fullPath)
                .setContentType(file.getContentType())
                .build();
        try (WriteChannel writer = createStorage().writer(blobInfo)) {
            writer.write(ByteBuffer.wrap(fileContent, 0, fileContent.length));
        } catch (IOException e) {
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }

        return fullPath;
    }



    @Override
    public List<String> uploadMultipleFile(MultipartFile[] files, BigInteger id, String folder) throws IOException {
        List<String> uploadedFilesPath = new ArrayList<>();
        int maxFilenameLength = 250;

        // Tính toán độ dài của phần tiền tố đường dẫn (thư mục + id + dấu gạch chéo "/")
        String pathPrefix = "%s/%s/".formatted(folder, id);
        int pathPrefixLength = pathPrefix.length();

        for (MultipartFile rawFile : files) {
            byte[] fileContent = rawFile.getBytes();
            String originalFilename = rawFile.getOriginalFilename();

            // Lấy đuôi file (ví dụ: ".jpg", ".png")
            String fileExtension = "";
            int extensionIndex = originalFilename.lastIndexOf(".");
            if (extensionIndex > 0) {
                fileExtension = originalFilename.substring(extensionIndex);
                originalFilename = originalFilename.substring(0, extensionIndex); // Cắt bỏ đuôi file khỏi tên gốc để xử lý cắt ngắn
            }

            String uuid = UUID.randomUUID().toString();

            // Điều chỉnh độ dài tối đa của tên file để đảm bảo không vượt quá giới hạn
            int availableFilenameLength = maxFilenameLength - pathPrefixLength;
            int maxOriginalFilenameLength = availableFilenameLength - uuid.length() - 1 - fileExtension.length(); // Trừ đi độ dài của UUID, dấu gạch dưới, và đuôi file

            // Nếu tên file gốc dài hơn mức cho phép, cắt bớt nó
            if (originalFilename.length() > maxOriginalFilenameLength) {
                originalFilename = originalFilename.substring(0, maxOriginalFilenameLength);
            }

            // Tạo tên file duy nhất bằng cách thêm UUID trước tên file gốc
            String uniqueFilename = uuid + "_" + originalFilename + fileExtension; // Thêm lại đuôi file
            String fullPath = pathPrefix + uniqueFilename;

            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fullPath)
                    .setContentType(rawFile.getContentType())
                    .build();
            try (WriteChannel writer = createStorage().writer(blobInfo)) {
                writer.write(ByteBuffer.wrap(fileContent, 0, fileContent.length));
            } catch (IOException e) {
                throw new AppException(ErrorCode.UPLOAD_FAILED);
            }

            uploadedFilesPath.add(fullPath);
        }
        return uploadedFilesPath;
    }



    @Override
    public Boolean deleteFileByPath(String path) throws IOException {
        try {
            Storage storage = createStorage();
            Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(path));
            for (Blob blob : blobs.iterateAll()) {
                BlobId blobId = BlobId.of(bucketName, blob.getName());
                storage.delete(blobId);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean filesIsImage(MultipartFile[] files) {
        for (MultipartFile file : files) {
            if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) return false;
        }
        return true;
    }
}

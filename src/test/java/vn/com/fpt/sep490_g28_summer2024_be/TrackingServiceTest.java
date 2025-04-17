package vn.com.fpt.sep490_g28_summer2024_be;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.authentication.UserResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.tracking.GroupedTrackingImageDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.tracking.TrackingDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.tracking.TrackingImageDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.*;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseServiceImpl;
import vn.com.fpt.sep490_g28_summer2024_be.repository.*;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.service.tracking.TrackingService;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@MockBean(FirebaseServiceImpl.class)
public class TrackingServiceTest {
    @Autowired
    private FirebaseServiceImpl firebaseService;

    @Autowired
    private TrackingRepository trackingRepository;

    @Autowired
    private TrackingImageRepository trackingImageRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AssignRepository assignRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TrackingService trackingService;

    private BigInteger existingProjectId;

    private BigInteger existingAssignId;

    @BeforeEach
    void prepareForEachTest() {
        entityManager.createNativeQuery(
                        "INSERT INTO role (role_id, role_name, role_description, created_at, updated_at, is_active) VALUES " +
                                "(1, N'Admin', N'Quản lý dự án', '2024-06-05 11:00:00', '2024-06-05 11:00:00', TRUE)," +
                                "(2, N'Project Manager', N'Quản lí dự án ', '2024-06-06 12:00:00', '2024-06-06 12:00:00', TRUE)," +
                                "(3, N'Social Staff', N'Quản lí tin tức', '2024-06-07 13:00:00', '2024-06-07 13:00:00', TRUE)," +
                                "(4, N'System User', N'Khách', '2024-06-08 14:00:00', '2024-06-08 14:00:00', TRUE);")
                .executeUpdate();

        List<Role> roles = roleRepository.findAll();
        Account savedAccount = Account.builder()
                .email("newuser@example.com")
                .password(passwordEncoder.encode("123456"))
                .fullname("Nguyen Van A")
                .gender(1)
                .code("ACC001")
                .phone("0912345678")
                .address("123 Đường ABC")
                .dob(LocalDate.of(2003, 12, 31))
                .createdAt(LocalDate.now().atStartOfDay())
                .isActive(true)
                .role(roles.getFirst())
                .build();
        savedAccount = accountRepository.save(savedAccount);

        // Tạo một Project và Assign mới bằng cách sử dụng entityManager
        entityManager.createNativeQuery(
                        "INSERT INTO project (title, ward, district, province, created_at, updated_at) VALUES " +
                                "(N'Test Project', N'Test Ward', N'Test District', N'Test Province', '2024-06-05 11:00:00', '2024-06-05 11:00:00');")
                .executeUpdate();

        // Lấy ID của Project vừa tạo
        List<Project> projects = projectRepository.findAll();
        existingProjectId = projects.getFirst().getProjectId();

        entityManager.createNativeQuery(
                        "INSERT INTO assign (project_id, account_id, created_by, created_at, updated_by, updated_at) VALUES " +
                                "(?, ?, ?, '2024-06-05 11:00:00', ?, '2024-06-05 11:00:00');")
                .setParameter(1, existingProjectId) // account_id
                .setParameter(2, savedAccount.getAccountId()) // account_id
                .setParameter(3, savedAccount.getAccountId()) // created_by
                .setParameter(4, savedAccount.getAccountId()) // updated_by
                .executeUpdate();

        // Lấy ID của Assign vừa tạo
        List<Assign> assigns = assignRepository.findAll();
        existingAssignId = assigns.getFirst().getAssignId();

        savedAccount = Account.builder()
                .email("newuser1@example.com")
                .password(passwordEncoder.encode("123456"))
                .fullname("Nguyen Van B")
                .gender(1)
                .code("ACC002")
                .phone("0912345678")
                .address("123 Đường ABC")
                .dob(LocalDate.of(2003, 12, 31))
                .createdAt(LocalDate.now().atStartOfDay())
                .isActive(true)
                .role(roles.get(1))
                .build();
        accountRepository.save(savedAccount);
    }

    void setupValidAdminCustomAccountDetails() {
        // Mock CustomAccountDetails và UserResponse
        CustomAccountDetails mockDetails = mock(CustomAccountDetails.class);
        UserResponse mockUserResponse = mock(UserResponse.class);

        when(mockDetails.getUsername()).thenReturn("newuser@example.com");
        when(mockDetails.getUserResponse()).thenReturn(mockUserResponse);

        // Mock SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken(mockDetails, null);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    void setupValidNotAdminCustomAccountDetails() {
        // Mock CustomAccountDetails và UserResponse
        CustomAccountDetails mockDetails = mock(CustomAccountDetails.class);
        UserResponse mockUserResponse = mock(UserResponse.class);

        when(mockDetails.getUsername()).thenReturn("newuser1@example.com");
        when(mockDetails.getUserResponse()).thenReturn(mockUserResponse);

        // Mock SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken(mockDetails, null);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    void setupInvalidCustomAccountDetails() {
        // Mock CustomAccountDetails và UserResponse
        CustomAccountDetails mockDetails = mock(CustomAccountDetails.class);
        UserResponse mockUserResponse = mock(UserResponse.class);

        when(mockDetails.getUsername()).thenReturn("c@example.com");
        when(mockDetails.getUserResponse()).thenReturn(mockUserResponse);

        // Mock SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken(mockDetails, null);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("TS_addTracking_01")
    void addTracking_shouldThrowException_whenProjectNotExist() {
        ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                .projectId(BigInteger.valueOf(0))
                .title("Test Project")
                .build();
        TrackingDTO trackingDTO = TrackingDTO.builder()
                .project(projectResponseDTO)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = null;

        // Kiểm tra kết quả đúng
        AppException exception = assertThrows(AppException.class, () -> {
            trackingService.addTracking(trackingDTO, newImages);
        });
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, exception.getErrorCode());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(0, trackingList.size());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(0, trackingImageList.size());
    }

    @Test
    @DisplayName("TS_addTracking_02")
    void addTracking_shouldThrowException_whenAccountNotFound() {
        setupInvalidCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .build();
        TrackingDTO trackingDTO = TrackingDTO.builder()
                .project(projectResponseDTO)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = null;

        // Kiểm tra kết quả đúng
        AppException exception = assertThrows(AppException.class, () -> {
            trackingService.addTracking(trackingDTO, newImages);
        });
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, exception.getErrorCode());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(0, trackingList.size());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(0, trackingImageList.size());
    }

    @Test
    @DisplayName("TS_addTracking_03")
    void addTracking_shouldThrowException_whenLoggedAccountIsNotAdminAndProjectNotBelongToLoggedAccount() {
        setupValidNotAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .build();
        TrackingDTO trackingDTO = TrackingDTO.builder()
                .project(projectResponseDTO)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = null;

        // Kiểm tra kết quả đúng
        AppException exception = assertThrows(AppException.class, () -> {
            trackingService.addTracking(trackingDTO, newImages);
        });
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(0, trackingList.size());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(0, trackingImageList.size());
    }

    @Test
    @DisplayName("TS_addTracking_04")
    void addTracking_shouldAddAndReturnAddedDTO_whenAdminLoggedInAndNewImageIsNull() {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .build();
        TrackingDTO trackingDTO = TrackingDTO.builder()
                .project(projectResponseDTO)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = null;

        TrackingDTO addedTrackingDTO = trackingService.addTracking(trackingDTO, newImages);

        // Kiểm tra kết quả đúng
        assertNotNull(addedTrackingDTO);
        assertEquals(trackingDTO.getProject().getProjectId(), addedTrackingDTO.getProject().getProjectId());
        assertEquals(trackingDTO.getTitle(), addedTrackingDTO.getTitle());
        assertEquals(trackingDTO.getContent(), addedTrackingDTO.getContent());
        assertEquals(trackingDTO.getDate(), addedTrackingDTO.getDate());
        assertNull(addedTrackingDTO.getTrackingImages());
        assertNotNull(addedTrackingDTO.getTrackingId());
        assertNotNull(addedTrackingDTO.getCreatedAt());
        assertNotNull(addedTrackingDTO.getUpdatedAt());
        assertEquals(addedTrackingDTO.getProject(), projectResponseDTO);

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(0, trackingImageList.size());

        // Kiểm tra thông tin của Tracking đã thêm
        Tracking addedTracking = trackingList.getFirst();
        assertNotNull(addedTracking);
        assertEquals(addedTrackingDTO.getProject().getProjectId(), addedTracking.getProject().getProjectId());
        assertEquals(addedTrackingDTO.getTitle(), addedTracking.getTitle());
        assertEquals(addedTrackingDTO.getContent(), addedTracking.getContent());
        assertEquals(addedTrackingDTO.getDate(), addedTracking.getDate());
        assertNull(addedTracking.getTrackingImages());
        assertNotNull(addedTracking.getTrackingId());
        assertNotNull(addedTracking.getCreatedAt());
        assertNotNull(addedTracking.getUpdatedAt());
        assertEquals(addedTracking.getProject().getProjectId(), project.getProjectId());
    }

    @Test
    @DisplayName("TS_addTracking_05")
    void addTracking_shouldThrowException_whenAdminLoggedInAndNewImageInvalidType() {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .build();
        TrackingDTO trackingDTO = TrackingDTO.builder()
                .project(projectResponseDTO)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = new MultipartFile[1];
        newImages[0] = new MockMultipartFile("file", "test.txt", "text/plain", "Test content".getBytes());

        // Kiểm tra kết quả đúng
        AppException exception = assertThrows(AppException.class, () -> {
            trackingService.addTracking(trackingDTO, newImages);
        });
        assertEquals(ErrorCode.HTTP_FILE_IS_NOT_IMAGE, exception.getErrorCode());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(0, trackingList.size());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(0, trackingImageList.size());
    }

    @Test
    @DisplayName("TS_addTracking_06")
    void addTracking_shouldThrowException_whenAdminLoggedInAndNewImageInvalidSize() {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .build();
        TrackingDTO trackingDTO = TrackingDTO.builder()
                .project(projectResponseDTO)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = new MultipartFile[1];
        newImages[0] = new MockMultipartFile("file", "image.png", "image/png", new byte[3 * 1024 * 1024]);

        // Kiểm tra kết quả đúng
        AppException exception = assertThrows(AppException.class, () -> {
            trackingService.addTracking(trackingDTO, newImages);
        });
        assertEquals(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT, exception.getErrorCode());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(0, trackingList.size());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(0, trackingImageList.size());
    }

    @Test
    @DisplayName("TS_addTracking_07")
    void addTracking_shouldThrowException_whenAdminLoggedInAndNewImageValidAndUploadFail() throws IOException {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .build();
        TrackingDTO trackingDTO = TrackingDTO.builder()
                .project(projectResponseDTO)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = new MultipartFile[1];
        newImages[0] = new MockMultipartFile("file", "image.png", "image/png", "image".getBytes());

        when(firebaseService.uploadMultipleFile(any(), any(), any())).thenThrow(IOException.class);

        // Kiểm tra kết quả đúng
        AppException exception = assertThrows(AppException.class, () -> {
            trackingService.addTracking(trackingDTO, newImages);
        });
        assertEquals(ErrorCode.UPLOAD_FAILED, exception.getErrorCode());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(0, trackingList.size());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(0, trackingImageList.size());
    }

    @Test
    @DisplayName("TS_addTracking_08")
    void addTracking_shouldThrowException_whenAdminLoggedInAndNewImageValidAndUploadSuccess() throws IOException {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .build();
        TrackingDTO trackingDTO = TrackingDTO.builder()
                .project(projectResponseDTO)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = new MultipartFile[1];
        newImages[0] = new MockMultipartFile("file", "image.png", "image/png", "image".getBytes());

        List<String> uploadedImageUrls = List.of("https://example.com/image.png");
        when(firebaseService.uploadMultipleFile(any(), any(), any())).thenReturn(uploadedImageUrls);

        TrackingDTO addedTrackingDTO = trackingService.addTracking(trackingDTO, newImages);

        // Kiểm tra kết quả đúng
        assertNotNull(addedTrackingDTO);
        assertEquals(trackingDTO.getProject().getProjectId(), addedTrackingDTO.getProject().getProjectId());
        assertEquals(trackingDTO.getTitle(), addedTrackingDTO.getTitle());
        assertEquals(trackingDTO.getContent(), addedTrackingDTO.getContent());
        assertEquals(trackingDTO.getDate(), addedTrackingDTO.getDate());
        assertNotNull(addedTrackingDTO.getTrackingImages());
        assertEquals(1, addedTrackingDTO.getTrackingImages().size());
        assertEquals(uploadedImageUrls.getFirst(), addedTrackingDTO.getTrackingImages().getFirst().getImageUrl());
        assertNotNull(addedTrackingDTO.getTrackingId());
        assertNotNull(addedTrackingDTO.getCreatedAt());
        assertNotNull(addedTrackingDTO.getUpdatedAt());
        assertEquals(addedTrackingDTO.getProject(), projectResponseDTO);

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(1, trackingImageList.size());

        // Kiểm tra thông tin của Tracking đã thêm
        Tracking addedTracking = trackingList.getFirst();
        assertNotNull(addedTracking);
        assertEquals(addedTrackingDTO.getProject().getProjectId(), addedTracking.getProject().getProjectId());
        assertEquals(addedTrackingDTO.getTitle(), addedTracking.getTitle());
        assertEquals(addedTrackingDTO.getContent(), addedTracking.getContent());
        assertEquals(addedTrackingDTO.getDate(), addedTracking.getDate());
        assertNotNull(addedTrackingDTO.getTrackingImages());
        assertEquals(1, addedTrackingDTO.getTrackingImages().size());
        assertEquals(uploadedImageUrls.getFirst(), addedTrackingDTO.getTrackingImages().getFirst().getImageUrl());
        assertNotNull(addedTracking.getTrackingId());
        assertNotNull(addedTracking.getCreatedAt());
        assertNotNull(addedTracking.getUpdatedAt());
        assertEquals(addedTracking.getProject().getProjectId(), project.getProjectId());
    }

    @Test
    @DisplayName("TS_getTrackingById_01")
    void getTrackingById_shouldThrowException_whenTrackingNotFound() {
        BigInteger trackingId = BigInteger.valueOf(0);

        // Kiểm tra kết quả đúng
        AppException exception = assertThrows(AppException.class, () -> {
            trackingService.getTrackingById(trackingId);
        });
        assertEquals(ErrorCode.HTTP_TRACKING_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("TS_getTrackingById_02")
    void getTrackingById_shouldReturnTracking_whenNoTrackingImage() {
        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .trackingImages(List.of())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();

        tracking = trackingRepository.save(tracking);

        TrackingDTO trackingDTO = trackingService.getTrackingById(tracking.getTrackingId());

        // Kiểm tra kết quả đúng
        assertNotNull(trackingDTO);
        assertEquals(tracking.getTrackingId(), trackingDTO.getTrackingId());
        assertEquals(tracking.getProject().getProjectId(), trackingDTO.getProject().getProjectId());
        assertEquals(tracking.getTitle(), trackingDTO.getTitle());
        assertEquals(tracking.getContent(), trackingDTO.getContent());
        assertEquals(tracking.getDate(), trackingDTO.getDate());
        assertNotNull(trackingDTO.getTrackingImages());
        assertEquals(0, trackingDTO.getTrackingImages().size());
        assertNotNull(trackingDTO.getCreatedAt());
        assertNotNull(trackingDTO.getUpdatedAt());
    }

    @Test
    @DisplayName("TS_getTrackingById_03")
    void getTrackingById_shouldReturnTracking_whenHasTrackingImage() {
        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();

        tracking = trackingRepository.save(tracking);

        TrackingImage trackingImage = TrackingImage.builder()
                .tracking(tracking)
                .image("https://example.com/image.png")
                .build();
        trackingImage = trackingImageRepository.save(trackingImage);
        tracking.setTrackingImages(new ArrayList<>(List.of(trackingImage)));
        tracking = trackingRepository.save(tracking);

        TrackingDTO trackingDTO = trackingService.getTrackingById(tracking.getTrackingId());

        // Kiểm tra kết quả đúng
        assertNotNull(trackingDTO);
        assertEquals(tracking.getTrackingId(), trackingDTO.getTrackingId());
        assertEquals(tracking.getProject().getProjectId(), trackingDTO.getProject().getProjectId());
        assertEquals(tracking.getTitle(), trackingDTO.getTitle());
        assertEquals(tracking.getContent(), trackingDTO.getContent());
        assertEquals(tracking.getDate(), trackingDTO.getDate());
        assertNotNull(trackingDTO.getTrackingImages());
        assertEquals(1, trackingDTO.getTrackingImages().size());
        assertEquals(trackingImage.getImage(), trackingDTO.getTrackingImages().getFirst().getImageUrl());
        assertNotNull(trackingDTO.getCreatedAt());
        assertNotNull(trackingDTO.getUpdatedAt());
    }

    @Test
    @DisplayName("TS_updateTracking_01")
    void updateTracking_shouldThrowException_whenTrackingNotFound() {
        ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                .projectId(BigInteger.valueOf(0))
                .title("Test Project")
                .build();
        TrackingDTO trackingDTO = TrackingDTO.builder()
                .project(projectResponseDTO)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = null;
        BigInteger notExistId = BigInteger.valueOf(0);

        // Kiểm tra kết quả đúng
        AppException exception = assertThrows(AppException.class, () -> {
            trackingService.updateTracking(trackingDTO, notExistId, newImages);
        });
        assertEquals(ErrorCode.HTTP_TRACKING_NOT_FOUND, exception.getErrorCode());

        // Kiểm tra trong DB không có tracking nào
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(0, trackingList.size());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(0, trackingImageList.size());
    }

    @Test
    @DisplayName("TS_updateTracking_02")
    void updateTracking_shouldThrowException_whenAccountNotFound() {
        setupInvalidCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);
        BigInteger existingTrackingId = tracking.getTrackingId();

        TrackingDTO trackingDTO = TrackingDTO.builder()
                .trackingId(existingTrackingId)
                .title("Updated Title")
                .content("Updated Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = null;

        AppException exception = assertThrows(AppException.class, () -> {
            trackingService.updateTracking(trackingDTO, existingTrackingId, newImages);
        });
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, exception.getErrorCode());

        // Kiểm tra trong DB không có tracking nào
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());
        assertEquals(tracking.getTrackingId(), trackingList.getFirst().getTrackingId());
        assertEquals(tracking.getTitle(), trackingList.getFirst().getTitle());
        assertEquals(tracking.getContent(), trackingList.getFirst().getContent());
        assertEquals(tracking.getDate(), trackingList.getFirst().getDate());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(0, trackingImageList.size());
    }

    @Test
    @DisplayName("TS_updateTracking_03")
    void updateTracking_shouldThrowException_whenLoggedAccountIsNotAdminAndProjectNotBelongToLoggedAccount() {
        setupValidNotAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);
        BigInteger existingTrackingId = tracking.getTrackingId();

        TrackingDTO trackingDTO = TrackingDTO.builder()
                .trackingId(existingTrackingId)
                .title("Updated Title")
                .content("Updated Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = null;

        AppException exception = assertThrows(AppException.class, () -> {
            trackingService.updateTracking(trackingDTO, existingTrackingId, newImages);
        });
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());

        // Kiểm tra trong DB không có tracking nào
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());
        assertEquals(tracking.getTrackingId(), trackingList.getFirst().getTrackingId());
        assertEquals(tracking.getTitle(), trackingList.getFirst().getTitle());
        assertEquals(tracking.getContent(), trackingList.getFirst().getContent());
        assertEquals(tracking.getDate(), trackingList.getFirst().getDate());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(0, trackingImageList.size());
    }

    @Test
    @DisplayName("TS_updateTracking_04")
    void updateTracking_shouldUpdateAndReturnUpdatedDTO_whenAdminLoggedInAndNewImageIsNull() {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);
        BigInteger existingTrackingId = tracking.getTrackingId();

        TrackingDTO trackingDTO = TrackingDTO.builder()
                .trackingId(existingTrackingId)
                .title("Updated Title")
                .content("Updated Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = null;

        TrackingDTO updatedTrackingDTO = trackingService.updateTracking(trackingDTO, existingTrackingId, newImages);

        // Kiểm tra kết quả đúng
        assertNotNull(updatedTrackingDTO);
        assertEquals(existingTrackingId, updatedTrackingDTO.getTrackingId());
        assertEquals("Updated Title", updatedTrackingDTO.getTitle());
        assertEquals("Updated Content", updatedTrackingDTO.getContent());
        assertEquals(LocalDate.now(), updatedTrackingDTO.getDate());
        assertEquals(LocalDate.now().atStartOfDay(), updatedTrackingDTO.getCreatedAt());
        assertNotNull(updatedTrackingDTO.getUpdatedAt());
        assertNull(updatedTrackingDTO.getTrackingImages());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());
        assertEquals(existingTrackingId, trackingList.getFirst().getTrackingId());
        assertEquals("Updated Title", trackingList.getFirst().getTitle());
        assertEquals("Updated Content", trackingList.getFirst().getContent());
        assertEquals(LocalDate.now(), trackingList.getFirst().getDate());
        assertEquals(LocalDate.now().atStartOfDay(), trackingList.getFirst().getCreatedAt());
        assertNotNull(trackingList.getFirst().getUpdatedAt());
        assertNull(trackingList.getFirst().getTrackingImages());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(0, trackingImageList.size());
    }

    @Test
    @DisplayName("TS_updateTracking_05")
    void updateTracking_shouldThrowException_whenAdminLoggedInAndOldTrackingHasImagesAndNewImageDTODifferAndDeleteFail() throws IOException {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);
        BigInteger existingTrackingId = tracking.getTrackingId();

        TrackingImage trackingImage = TrackingImage.builder()
                .tracking(tracking)
                .image("https://example.com/image.png")
                .build();
        trackingImage = trackingImageRepository.save(trackingImage);
        tracking.setTrackingImages(new ArrayList<>(List.of(trackingImage)));
        tracking = trackingRepository.save(tracking);

        TrackingImageDTO trackingImageDTO = TrackingImageDTO.builder()
                .imageUrl("https://example.com/image1.png")
                .build();
        
        TrackingDTO trackingDTO = TrackingDTO.builder()
                .trackingId(existingTrackingId)
                .title("Updated Title")
                .trackingImages(new ArrayList<>(List.of(trackingImageDTO)))
                .content("Updated Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = null;
        when(firebaseService.deleteFileByPath(any())).thenThrow(IOException.class);

        // Kiểm tra kết quả đúng
        AppException exception = assertThrows(AppException.class, () -> {
            trackingService.updateTracking(trackingDTO, existingTrackingId, newImages);
        });
        assertEquals(ErrorCode.UPLOAD_FAILED, exception.getErrorCode());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());
        assertEquals(existingTrackingId, trackingList.getFirst().getTrackingId());
        assertEquals("Test Title", trackingList.getFirst().getTitle());
        assertEquals("Test Content", trackingList.getFirst().getContent());
        assertEquals(LocalDate.now(), trackingList.getFirst().getDate());
        assertEquals(LocalDate.now().atStartOfDay(), trackingList.getFirst().getCreatedAt());
        assertNotNull(trackingList.getFirst().getUpdatedAt());
        assertNotNull(trackingList.getFirst().getTrackingImages());
        assertEquals(1, trackingList.getFirst().getTrackingImages().size());
        assertEquals("https://example.com/image.png", trackingList.getFirst().getTrackingImages().getFirst().getImage());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(1, trackingImageList.size());
        assertEquals("https://example.com/image.png", trackingImageList.getFirst().getImage());
    }

    @Test
    @DisplayName("TS_updateTracking_06")
    void updateTracking_shouldUpdateAndReturnUpdatedDTO_whenAdminLoggedInAndOldTrackingHasImagesAndNewImageDTODifferAndDeleteSuccess() throws IOException {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);
        BigInteger existingTrackingId = tracking.getTrackingId();

        TrackingImage trackingImage = TrackingImage.builder()
                .tracking(tracking)
                .image("https://example.com/image.png")
                .build();
        trackingImage = trackingImageRepository.save(trackingImage);
        tracking.setTrackingImages(new ArrayList<>(List.of(trackingImage)));
        tracking = trackingRepository.save(tracking);

        TrackingImageDTO trackingImageDTO = TrackingImageDTO.builder()
                .imageUrl("https://example.com/image1.png")
                .build();

        TrackingDTO trackingDTO = TrackingDTO.builder()
                .trackingId(existingTrackingId)
                .title("Updated Title")
                .trackingImages(new ArrayList<>(List.of(trackingImageDTO)))
                .content("Updated Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = null;
        when(firebaseService.deleteFileByPath(any())).thenReturn(true);

        TrackingDTO updatedTrackingDTO = trackingService.updateTracking(trackingDTO, existingTrackingId, newImages);

        // Kiểm tra kết quả đúng
        assertNotNull(updatedTrackingDTO);
        assertEquals(existingTrackingId, updatedTrackingDTO.getTrackingId());
        assertEquals("Updated Title", updatedTrackingDTO.getTitle());
        assertEquals("Updated Content", updatedTrackingDTO.getContent());
        assertEquals(LocalDate.now(), updatedTrackingDTO.getDate());
        assertNotNull(updatedTrackingDTO.getTrackingImages());
        assertEquals(1, updatedTrackingDTO.getTrackingImages().size());
        assertEquals("https://example.com/image1.png", updatedTrackingDTO.getTrackingImages().getFirst().getImageUrl());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());
        assertEquals(existingTrackingId, trackingList.getFirst().getTrackingId());
        assertEquals("Updated Title", trackingList.getFirst().getTitle());
        assertEquals("Updated Content", trackingList.getFirst().getContent());
        assertEquals(LocalDate.now(), trackingList.getFirst().getDate());
        assertNotNull(trackingList.getFirst().getUpdatedAt());
        assertNotNull(trackingList.getFirst().getCreatedAt());
        assertNotNull(trackingList.getFirst().getTrackingImages());
        assertEquals(1, trackingList.getFirst().getTrackingImages().size());
        assertEquals("https://example.com/image1.png", trackingList.getFirst().getTrackingImages().getFirst().getImage());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(1, trackingImageList.size());
        assertEquals("https://example.com/image1.png", trackingImageList.getFirst().getImage());
    }

    @Test
    @DisplayName("TS_updateTracking_07")
    void updateTracking_shouldThrowException_whenAdminLoggedInAndOldTrackingHasImagesAndNewImageDTOIsNullAndDeleteFail() throws IOException {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);
        BigInteger existingTrackingId = tracking.getTrackingId();

        TrackingImage trackingImage = TrackingImage.builder()
                .tracking(tracking)
                .image("https://example.com/image.png")
                .build();
        trackingImage = trackingImageRepository.save(trackingImage);
        tracking.setTrackingImages(new ArrayList<>(List.of(trackingImage)));
        tracking = trackingRepository.save(tracking);

        TrackingDTO trackingDTO = TrackingDTO.builder()
                .trackingId(existingTrackingId)
                .title("Updated Title")
                .trackingImages(null)
                .content("Updated Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = null;
        when(firebaseService.deleteFileByPath(any())).thenThrow(IOException.class);

        // Kiểm tra kết quả đúng
        AppException exception = assertThrows(AppException.class, () -> {
            trackingService.updateTracking(trackingDTO, existingTrackingId, newImages);
        });
        assertEquals(ErrorCode.UPLOAD_FAILED, exception.getErrorCode());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());
        assertEquals(existingTrackingId, trackingList.getFirst().getTrackingId());
        assertEquals("Test Title", trackingList.getFirst().getTitle());
        assertEquals("Test Content", trackingList.getFirst().getContent());
        assertEquals(LocalDate.now(), trackingList.getFirst().getDate());
        assertEquals(LocalDate.now().atStartOfDay(), trackingList.getFirst().getCreatedAt());
        assertNotNull(trackingList.getFirst().getUpdatedAt());
        assertNotNull(trackingList.getFirst().getTrackingImages());
        assertEquals(1, trackingList.getFirst().getTrackingImages().size());
        assertEquals("https://example.com/image.png", trackingList.getFirst().getTrackingImages().getFirst().getImage());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(1, trackingImageList.size());
        assertEquals("https://example.com/image.png", trackingImageList.getFirst().getImage());
    }

    @Test
    @DisplayName("TS_updateTracking_08")
    void updateTracking_shouldUpdateAndReturnUpdatedDTO_whenAdminLoggedInAndOldTrackingHasImagesAndNewImageDTOIsNullAndDeleteSuccess() throws IOException {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);
        BigInteger existingTrackingId = tracking.getTrackingId();

        TrackingImage trackingImage = TrackingImage.builder()
                .tracking(tracking)
                .image("https://example.com/image.png")
                .build();
        trackingImage = trackingImageRepository.save(trackingImage);
        tracking.setTrackingImages(new ArrayList<>(List.of(trackingImage)));
        tracking = trackingRepository.save(tracking);

        TrackingDTO trackingDTO = TrackingDTO.builder()
                .trackingId(existingTrackingId)
                .title("Updated Title")
                .trackingImages(null)
                .content("Updated Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = null;
        when(firebaseService.deleteFileByPath(any())).thenReturn(true);

        TrackingDTO updatedTrackingDTO = trackingService.updateTracking(trackingDTO, existingTrackingId, newImages);

        // Kiểm tra kết quả đúng
        assertNotNull(updatedTrackingDTO);
        assertEquals(existingTrackingId, updatedTrackingDTO.getTrackingId());
        assertEquals("Updated Title", updatedTrackingDTO.getTitle());
        assertEquals("Updated Content", updatedTrackingDTO.getContent());
        assertEquals(LocalDate.now(), updatedTrackingDTO.getDate());
        assertEquals(LocalDate.now().atStartOfDay(), updatedTrackingDTO.getCreatedAt());
        assertNotNull(updatedTrackingDTO.getUpdatedAt());
        assertEquals(0, updatedTrackingDTO.getTrackingImages().size());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());
        assertEquals(existingTrackingId, trackingList.getFirst().getTrackingId());
        assertEquals("Updated Title", trackingList.getFirst().getTitle());
        assertEquals("Updated Content", trackingList.getFirst().getContent());
        assertEquals(LocalDate.now(), trackingList.getFirst().getDate());
        assertEquals(LocalDate.now().atStartOfDay(), trackingList.getFirst().getCreatedAt());
        assertNotNull(trackingList.getFirst().getUpdatedAt());
        assertNull(trackingList.getFirst().getTrackingImages());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(0, trackingImageList.size());
    }

    @Test
    @DisplayName("TS_updateTracking_09")
    void updateTracking_shouldThrowException_whenAdminLoggedInAndNewImageInvalidType() {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);
        BigInteger existingTrackingId = tracking.getTrackingId();

        TrackingImage trackingImage = TrackingImage.builder()
                .tracking(tracking)
                .image("https://example.com/image.png")
                .build();
        trackingImage = trackingImageRepository.save(trackingImage);
        tracking.setTrackingImages(new ArrayList<>(List.of(trackingImage)));
        tracking = trackingRepository.save(tracking);

        TrackingDTO trackingDTO = TrackingDTO.builder()
                .trackingId(existingTrackingId)
                .title("Updated Title")
                .content("Updated Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = new MultipartFile[1];
        newImages[0] = new MockMultipartFile("image", "not-image-type", "not-image-type", "image".getBytes());

        // Kiểm tra kết quả đúng
        AppException exception = assertThrows(AppException.class, () -> {
            trackingService.updateTracking(trackingDTO, existingTrackingId, newImages);
        });
        assertEquals(ErrorCode.HTTP_FILE_IS_NOT_IMAGE, exception.getErrorCode());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());
        assertEquals(existingTrackingId, trackingList.getFirst().getTrackingId());
        assertEquals("Test Title", trackingList.getFirst().getTitle());
        assertEquals("Test Content", trackingList.getFirst().getContent());
        assertEquals(LocalDate.now(), trackingList.getFirst().getDate());
        assertEquals(LocalDate.now().atStartOfDay(), trackingList.getFirst().getCreatedAt());
        assertNotNull(trackingList.getFirst().getUpdatedAt());
        assertNotNull(trackingList.getFirst().getTrackingImages());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(1, trackingImageList.size());
    }

    @Test
    @DisplayName("TS_updateTracking_10")
    void updateTracking_shouldThrowException_whenAdminLoggedInAndNewImageInvalidSize() {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);
        BigInteger existingTrackingId = tracking.getTrackingId();

        TrackingImage trackingImage = TrackingImage.builder()
                .tracking(tracking)
                .image("https://example.com/image.png")
                .build();
        trackingImage = trackingImageRepository.save(trackingImage);
        tracking.setTrackingImages(new ArrayList<>(List.of(trackingImage)));
        tracking = trackingRepository.save(tracking);

        TrackingDTO trackingDTO = TrackingDTO.builder()
                .trackingId(existingTrackingId)
                .title("Updated Title")
                .content("Updated Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = new MultipartFile[1];
        newImages[0] = new MockMultipartFile("image", "image.png", "image/png", new byte[3 * 1024 * 1024]);

        // Kiểm tra kết quả đúng
        AppException exception = assertThrows(AppException.class, () -> {
            trackingService.updateTracking(trackingDTO, existingTrackingId, newImages);
        });
        assertEquals(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT, exception.getErrorCode());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());
        assertEquals(existingTrackingId, trackingList.getFirst().getTrackingId());
        assertEquals("Test Title", trackingList.getFirst().getTitle());
        assertEquals("Test Content", trackingList.getFirst().getContent());
        assertEquals(LocalDate.now(), trackingList.getFirst().getDate());
        assertEquals(LocalDate.now().atStartOfDay(), trackingList.getFirst().getCreatedAt());
        assertNotNull(trackingList.getFirst().getUpdatedAt());
        assertNotNull(trackingList.getFirst().getTrackingImages());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(1, trackingImageList.size());
    }

    @Test
    @DisplayName("TS_updateTracking_11")
    void updateTracking_shouldThrowException_whenAdminLoggedInAndNewImageValidAndUploadFail() throws IOException {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);
        BigInteger existingTrackingId = tracking.getTrackingId();

        TrackingImage trackingImage = TrackingImage.builder()
                .tracking(tracking)
                .image("https://example.com/image.png")
                .build();
        trackingImage = trackingImageRepository.save(trackingImage);
        tracking.setTrackingImages(new ArrayList<>(List.of(trackingImage)));
        tracking = trackingRepository.save(tracking);

        TrackingDTO trackingDTO = TrackingDTO.builder()
                .trackingId(existingTrackingId)
                .title("Updated Title")
                .content("Updated Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = new MultipartFile[1];
        newImages[0] = new MockMultipartFile("image", "image.png", "image/png", "image".getBytes());

        when(firebaseService.uploadMultipleFile(any(), any(), any())).thenThrow(IOException.class);

        // Kiểm tra kết quả đúng
        AppException exception = assertThrows(AppException.class, () -> {
            trackingService.updateTracking(trackingDTO, existingTrackingId, newImages);
        });
        assertEquals(ErrorCode.UPLOAD_FAILED, exception.getErrorCode());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());
        assertEquals(existingTrackingId, trackingList.getFirst().getTrackingId());
        assertEquals("Test Title", trackingList.getFirst().getTitle());
        assertEquals("Test Content", trackingList.getFirst().getContent());
        assertEquals(LocalDate.now(), trackingList.getFirst().getDate());
        assertEquals(LocalDate.now().atStartOfDay(), trackingList.getFirst().getCreatedAt());
        assertNotNull(trackingList.getFirst().getUpdatedAt());
        assertNotNull(trackingList.getFirst().getTrackingImages());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(1, trackingImageList.size());
    }

    @Test
    @DisplayName("TS_updateTracking_12")
    void updateTracking_shouldUpdateAndReturnUpdatedDTO_whenAdminLoggedInAndNewImageValidAndUploadSuccess() throws IOException {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);
        BigInteger existingTrackingId = tracking.getTrackingId();

        TrackingImage trackingImage = TrackingImage.builder()
                .tracking(tracking)
                .image("https://example.com/image.png")
                .build();
        trackingImage = trackingImageRepository.save(trackingImage);
        tracking.setTrackingImages(new ArrayList<>(List.of(trackingImage)));
        tracking = trackingRepository.save(tracking);

        TrackingDTO trackingDTO = TrackingDTO.builder()
                .trackingId(existingTrackingId)
                .title("Updated Title")
                .content("Updated Content")
                .date(LocalDate.now())
                .build();

        MultipartFile[] newImages = new MultipartFile[1];
        newImages[0] = new MockMultipartFile("image", "image.png", "image/png", "image".getBytes());

        List<String> uploadedImageUrls = List.of("https://example.com/image1.png");
        when(firebaseService.uploadMultipleFile(any(), any(), any())).thenReturn(uploadedImageUrls);

        TrackingDTO updatedTrackingDTO = trackingService.updateTracking(trackingDTO, existingTrackingId, newImages);

        // Kiểm tra kết quả đúng
        assertNotNull(updatedTrackingDTO);
        assertEquals(existingTrackingId, updatedTrackingDTO.getTrackingId());
        assertEquals("Updated Title", updatedTrackingDTO.getTitle());
        assertEquals("Updated Content", updatedTrackingDTO.getContent());
        assertEquals(LocalDate.now(), updatedTrackingDTO.getDate());
        assertNotNull(updatedTrackingDTO.getUpdatedAt());
        assertNotNull(updatedTrackingDTO.getCreatedAt());
        assertEquals(1, updatedTrackingDTO.getTrackingImages().size());
        assertEquals(uploadedImageUrls.getFirst(), updatedTrackingDTO.getTrackingImages().getFirst().getImageUrl());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());
        assertEquals(existingTrackingId, trackingList.getFirst().getTrackingId());
        assertEquals("Updated Title", trackingList.getFirst().getTitle());
        assertEquals("Updated Content", trackingList.getFirst().getContent());
        assertEquals(LocalDate.now(), trackingList.getFirst().getDate());
        assertEquals(LocalDate.now().atStartOfDay(), trackingList.getFirst().getCreatedAt());
        assertNotNull(trackingList.getFirst().getUpdatedAt());
        assertNotNull(trackingList.getFirst().getTrackingImages());
        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(1, trackingImageList.size());
        assertEquals("https://example.com/image1.png", trackingImageList.getFirst().getImage());
    }

    @Test
    @DisplayName("TS_viewByFilter_01")
    void viewByFilter_shouldThrowException_whenProjectNotFound() {
        Integer page = 0;
        Integer size = 10;
        String title = null;
        BigInteger projectId = BigInteger.valueOf(0);

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            trackingService.viewByFilter(page, size, title, projectId);
        });
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, thrown.getErrorCode());
    }

    @Test
    @DisplayName("TS_viewByFilter_02")
    void viewByFilter_shouldReturnEmptyList_whenNoTracking() {
        Integer page = 0;
        Integer size = 10;
        String title = null;
        BigInteger projectId = existingProjectId;

        PageResponse<TrackingDTO> result = trackingService.viewByFilter(page, size, title, projectId);

        // Kiểm tra kết quả đúng
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(10, result.getLimit());
        assertEquals(0, result.getOffset());
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("TS_viewByFilter_03")
    void viewByFilter_shouldReturnList_whenHasTracking() {
        Integer page = 0;
        Integer size = 10;
        String title = "Test Title";
        BigInteger projectId = existingProjectId;

        Optional<Project> projectOptional = projectRepository.findById(projectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();

        tracking = trackingRepository.save(tracking);
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());

        PageResponse<TrackingDTO> result = trackingService.viewByFilter(page, size, title, projectId);

        // Kiểm tra kết quả đúng
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(10, result.getLimit());
        assertEquals(0, result.getOffset());
        assertEquals(1, result.getTotal());

        // Kiểm tra thông tin của Tracking trong kết quả
        TrackingDTO trackingDTO = result.getContent().getFirst();
        assertNotNull(trackingDTO);
        assertEquals(tracking.getTrackingId(), trackingDTO.getTrackingId());
        assertEquals(tracking.getTitle(), trackingDTO.getTitle());
        assertEquals(tracking.getContent(), trackingDTO.getContent());
        assertEquals(tracking.getDate(), trackingDTO.getDate());
        assertEquals(tracking.getCreatedAt(), trackingDTO.getCreatedAt());
        assertEquals(tracking.getUpdatedAt(), trackingDTO.getUpdatedAt());
        assertEquals(tracking.getProject().getProjectId(), trackingDTO.getProject().getProjectId());
        assertEquals(tracking.getProject().getTitle(), trackingDTO.getProject().getTitle());
    }

    @Test
    @DisplayName("TS_deleteTracking_01")
    void deleteTracking_shouldThrowException_whenTrackingNotFound() {
        BigInteger trackingId = BigInteger.valueOf(0);

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            trackingService.deleteTracking(trackingId);
        });
        assertEquals(ErrorCode.HTTP_TRACKING_NOT_FOUND, thrown.getErrorCode());

        // Kiểm tra trong DB không có tracking nào
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(0, trackingList.size());
    }

    @Test
    @DisplayName("TS_deleteTracking_02")
    void deleteTracking_shouldThrowException_whenAccountNotFound() {
        setupInvalidCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);
        BigInteger existingTrackingId = tracking.getTrackingId();

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            trackingService.deleteTracking(existingTrackingId);
        });
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, thrown.getErrorCode());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());
    }

    @Test
    @DisplayName("TS_deleteTracking_03")
    void deleteTracking_shouldThrowException_whenLoggedAccountIsNotAdminAndProjectNotBelongToLoggedAccount() {
        setupValidNotAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);
        BigInteger existingTrackingId = tracking.getTrackingId();

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            trackingService.deleteTracking(existingTrackingId);
        });
        assertEquals(ErrorCode.ACCESS_DENIED, thrown.getErrorCode());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());
    }

    @Test
    @DisplayName("TS_deleteTracking_04")
    void deleteTracking_shouldDeleteTracking_whenNoImage() {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .trackingImages(new ArrayList<>())
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);
        BigInteger existingTrackingId = tracking.getTrackingId();

        trackingService.deleteTracking(existingTrackingId);

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(0, trackingList.size());
    }

    @Test
    @DisplayName("TS_deleteTracking_05")
    void deleteTracking_shouldThrowException_whenHasImageAndDeleteImageFail() throws IOException {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);
        BigInteger existingTrackingId = tracking.getTrackingId();

        TrackingImage trackingImage = TrackingImage.builder()
                .tracking(tracking)
                .image("https://example.com/image.png")
                .build();
        trackingImage = trackingImageRepository.save(trackingImage);
        tracking.setTrackingImages(new ArrayList<>(List.of(trackingImage)));
        tracking = trackingRepository.save(tracking);

        when(firebaseService.deleteFileByPath(any())).thenThrow(IOException.class);

        // Kiểm tra kết quả đúng
        AppException thrown = assertThrows(AppException.class, () -> {
            trackingService.deleteTracking(existingTrackingId);
        });
        assertEquals(ErrorCode.DELETE_FILE_FAILED, thrown.getErrorCode());

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(1, trackingList.size());
        assertEquals(existingTrackingId, trackingList.getFirst().getTrackingId());
        assertEquals(tracking.getTitle(), trackingList.getFirst().getTitle());
        assertEquals(tracking.getContent(), trackingList.getFirst().getContent());
        assertEquals(tracking.getDate(), trackingList.getFirst().getDate());
        assertEquals(tracking.getCreatedAt(), trackingList.getFirst().getCreatedAt());
        assertEquals(tracking.getUpdatedAt(), trackingList.getFirst().getUpdatedAt());
        assertNotNull(trackingList.getFirst().getTrackingImages());
        assertEquals(1, trackingList.getFirst().getTrackingImages().size());

        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(1, trackingImageList.size());
        assertEquals(trackingImage.getTrackingImageId(), trackingImageList.getFirst().getTrackingImageId());
    }

    @Test
    @DisplayName("TS_deleteTracking_06")
    void deleteTracking_shouldDeleteTracking_whenHasImageAndDeleteImageSuccess() throws IOException {
        setupValidAdminCustomAccountDetails();

        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Test Title")
                .content("Test Content")
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);
        BigInteger existingTrackingId = tracking.getTrackingId();

        TrackingImage trackingImage = TrackingImage.builder()
                .tracking(tracking)
                .image("https://example.com/image.png")
                .build();
        trackingImage = trackingImageRepository.save(trackingImage);
        tracking.setTrackingImages(new ArrayList<>(List.of(trackingImage)));
        tracking = trackingRepository.save(tracking);

        when(firebaseService.deleteFileByPath(any())).thenReturn(true);

        trackingService.deleteTracking(existingTrackingId);

        // Kiểm tra trong DB
        List<Tracking> trackingList = trackingRepository.findAll();
        assertNotNull(trackingList);
        assertEquals(0, trackingList.size());

        List<TrackingImage> trackingImageList = trackingImageRepository.findAll();
        assertNotNull(trackingImageList);
        assertEquals(0, trackingImageList.size());
    }

    @Test
    @DisplayName("TS_getImagesByProjectIdAndTitles_01")
    void getImagesByProjectIdAndTitles_shouldReturnEmptyList_whenTrackingNotFound() {
        List<GroupedTrackingImageDTO> result = trackingService.getImagesByProjectIdAndTitles(existingProjectId);

        // Kiểm tra kết quả đúng
        assertNotNull(result);
        assertEquals(0, result.size());
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("TS_getImagesByProjectIdAndTitles_02")
    void getImagesByProjectIdAndTitles_shouldReturnList_whenTrackingHasNoImage() {
        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Hiện trạng")
                .content("Test Content")
                .trackingImages(new ArrayList<>())
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);

        List<GroupedTrackingImageDTO> result = trackingService.getImagesByProjectIdAndTitles(existingProjectId);

        // Kiểm tra kết quả đúng
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Hiện trạng", result.getFirst().getTitle());
        assertEquals(0, result.getFirst().getImageUrls().size());
    }

    @Test
    @DisplayName("TS_getImagesByProjectIdAndTitles_03")
    void getImagesByProjectIdAndTitles_shouldReturnList_whenTrackingHasImage() {
        Optional<Project> projectOptional = projectRepository.findById(existingProjectId);
        assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();

        Tracking tracking = Tracking.builder()
                .project(project)
                .title("Hiện trạng")
                .content("Test Content")
                .trackingImages(new ArrayList<>())
                .date(LocalDate.now())
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();
        tracking = trackingRepository.save(tracking);

        TrackingImage trackingImage = TrackingImage.builder()
                .tracking(tracking)
                .image("https://example.com/image.png")
                .build();
        trackingImage = trackingImageRepository.save(trackingImage);
        tracking.setTrackingImages(new ArrayList<>(List.of(trackingImage)));
        tracking = trackingRepository.save(tracking);

        List<GroupedTrackingImageDTO> result = trackingService.getImagesByProjectIdAndTitles(existingProjectId);

        // Kiểm tra kết quả đúng
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Hiện trạng", result.getFirst().getTitle());
        assertEquals(1, result.getFirst().getImageUrls().size());
        assertEquals("https://example.com/image.png", result.getFirst().getImageUrls().getFirst());
    }
}


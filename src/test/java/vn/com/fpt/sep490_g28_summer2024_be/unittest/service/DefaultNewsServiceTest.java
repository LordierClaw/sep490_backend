package vn.com.fpt.sep490_g28_summer2024_be.unittest.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.authentication.UserResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.category.CategoryResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsChangeStatusDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsUpdateDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Category;
import vn.com.fpt.sep490_g28_summer2024_be.entity.News;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Role;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseService;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseServiceImpl;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.CategoryRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.NewsRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.RoleRepository;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.service.news.DefaultNewsService;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@MockBean(FirebaseServiceImpl.class)
@Transactional
public class DefaultNewsServiceTest {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private FirebaseService firebaseService;
    @Autowired
    private DefaultNewsService defaultNewsService;

    private CategoryResponseDTO categoryResponseDTO;

    private News preSaveNews;

    @BeforeEach
    void setUp() {
        // Dữ liệu cho account
        Role userRole = roleRepository.save(Role.builder().roleName("user").roleDescription("").build());

        Account userAccount = new Account();
        userAccount.setEmail("user@example.com");
        userAccount.setFullname("USER");
        userAccount.setPassword("password");
        userAccount.setRole(userRole);
        accountRepository.save(userAccount);

        Role adminRole = roleRepository.save(Role.builder().roleName("admin").roleDescription("").build());
        Account adminAccount = new Account();
        adminAccount.setEmail("admin@example.com");
        adminAccount.setFullname("ADMIN");
        adminAccount.setPassword("password");
        adminAccount.setRole(adminRole);
        accountRepository.save(adminAccount);

        // Dữ liệu cho news
        Category category = Category.builder()
                .title("category1")
                .description("description1")
                .slug("slug1")
                .isActive(true)
                .build();

        category = categoryRepository.save(category);
        categoryResponseDTO = new CategoryResponseDTO();
        BeanUtils.copyProperties(category, categoryResponseDTO);

        preSaveNews = newsRepository.save(News.builder()
                .title("existedTitle1")
                .content("content1")
                .category(category)
                .status(2)
                .createdAt(LocalDateTime.now())
                .createdBy(adminAccount)
                .updatedAt(LocalDateTime.now())
                .updatedBy(adminAccount)
                .thumbnail("test thumbnail")
                .build());
    }

    void setUpAsNonExistUser() {
        // Mock CustomAccountDetails
        CustomAccountDetails mockDetails = mock(CustomAccountDetails.class);
        when(mockDetails.getUsername()).thenReturn("nonexistent@example.com");

        // Đưa vào SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken(mockDetails, null);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    void setUpAsAdmin() {
        // Mock CustomAccountDetails và UserResponse
        CustomAccountDetails mockDetails = mock(CustomAccountDetails.class);
        UserResponse mockUserResponse = mock(UserResponse.class);

        when(mockDetails.getUsername()).thenReturn("admin@example.com");
        when(mockDetails.getUserResponse()).thenReturn(mockUserResponse);
        when(mockUserResponse.getScope()).thenReturn("admin");

        // Mock SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken(mockDetails, null);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    void setUpAsNotAdmin () {
        // Mock CustomAccountDetails và UserResponse
        CustomAccountDetails mockDetails = mock(CustomAccountDetails.class);
        UserResponse mockUserResponse = mock(UserResponse.class);

        when(mockDetails.getUsername()).thenReturn("user@example.com");
        when(mockDetails.getUserResponse()).thenReturn(mockUserResponse);
        when(mockUserResponse.getScope()).thenReturn("user");

        // Mock SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken(mockDetails, null);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("NS_create_01")
    void create_shouldThrowDuplicateTitleException_whenTitleAlreadyExists() {
        // Chuẩn bị dữ liệu
        NewsDTO dto = new NewsDTO();
        dto.setTitle("existedTitle1");
        dto.setContent("content1");
        dto.setCategory(categoryResponseDTO);

        // Thực hiện chạy và kiểm tra
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.create(dto, null);
        });
        assertEquals(ErrorCode.DUPLICATE_TITLE, exception.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        Query query = entityManager.createQuery("select count(*) from News where title = :title");
        query.setParameter("title", "existedTitle1");
        Long count = (Long) query.getSingleResult();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("NS_create_02")
    void create_shouldThrowUnauthorizedException_whenUserNotFound() {
        // Chuẩn bị DTO
        NewsDTO dto = new NewsDTO();
        dto.setTitle("uniqueTitle");
        dto.setContent("some content");
        dto.setCategory(CategoryResponseDTO.builder()
                .categoryId(BigInteger.valueOf(1))
                .build());

        setUpAsNonExistUser();

        // Thực hiện và kiểm tra
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.create(dto, null);
        });

        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, exception.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        Query query = entityManager.createQuery("select count(*) from News where title = :title");
        query.setParameter("title", "uniqueTitle");
        Long count = (Long) query.getSingleResult();
        assertEquals(0, count);
    }

    @Test
    @DisplayName("NS_create_03")
    void create_shouldSetStatusAs2AndReturnNewsResponse_whenUserIsAdmin() throws IOException {
        // Chuẩn bị dữ liệu
        NewsDTO dto = new NewsDTO();
        dto.setTitle("new-admin-news");
        dto.setContent("Admin post content");
        dto.setCategory(categoryResponseDTO);

        setUpAsAdmin();

        // Thực thi
        NewsResponseDTO response = defaultNewsService.create(dto, null);

        // Kiểm tra dữ liệu trả về
        assertEquals("new-admin-news", response.getTitle());
        assertNull(response.getThumbnail());

        // Kiểm tra dữ liệu trong DB
        Optional<News> news = newsRepository.findById(response.getNewsId());
        assertTrue(news.isPresent());
        assertEquals("new-admin-news", news.get().getTitle());
        assertEquals("Admin post content", news.get().getContent());
        assertNull(news.get().getThumbnail());
        assertEquals("admin@example.com", news.get().getCreatedBy().getEmail());
        assertEquals(2, news.get().getStatus());
    }

    @Test
    @DisplayName("NS_create_04")
    void create_shouldSetStatusAs1AndReturnNewsResponse_whenUserIsNotAdmin() throws IOException {
        // Chuẩn bị dữ liệu
        NewsDTO dto = new NewsDTO();
        dto.setTitle("new-user-news");
        dto.setContent("User post content");
        dto.setCategory(categoryResponseDTO);

        setUpAsNotAdmin();

        // Thực thi
        NewsResponseDTO response = defaultNewsService.create(dto, null);

        // Kiểm tra dữ liệu trả về
        assertEquals("new-user-news", response.getTitle());
        assertNull(response.getThumbnail());

        // Kiểm tra dữ liệu trong DB
        Optional<News> news = newsRepository.findById(response.getNewsId());
        assertTrue(news.isPresent());
        assertEquals("new-user-news", news.get().getTitle());
        assertEquals("User post content", news.get().getContent());
        assertNull(news.get().getThumbnail());
        assertEquals("user@example.com", news.get().getCreatedBy().getEmail());
        assertEquals(1, news.get().getStatus());
    }

    @Test
    @DisplayName("NS_create_05")
    void create_shouldSetStatusAs2AndReturnNewsResponse_whenUserIsAdminAndImageIsNull() throws IOException {
        // Chuẩn bị dữ liệu
        NewsDTO dto = new NewsDTO();
        dto.setTitle("new-admin-news");
        dto.setContent("Admin post content");
        dto.setCategory(categoryResponseDTO);

        setUpAsAdmin();

        // Thực thi
        NewsResponseDTO response = defaultNewsService.create(dto, null);

        // Kiểm tra dữ liệu trả về
        assertEquals("new-admin-news", response.getTitle());
        assertNull(response.getThumbnail());

        // Kiểm tra dữ liệu trong DB
        Optional<News> news = newsRepository.findById(response.getNewsId());
        assertTrue(news.isPresent());
        assertEquals("new-admin-news", news.get().getTitle());
        assertEquals("Admin post content", news.get().getContent());
        assertNull(news.get().getThumbnail());
        assertEquals("admin@example.com", news.get().getCreatedBy().getEmail());
        assertEquals(2, news.get().getStatus());
    }

    @Test
    @DisplayName("NS_create_06")
    void create_shouldThrowFileIsNotImage_whenUserIsNotAdminAndImageIsNotValid() {
        // Chuẩn bị dữ liệu
        NewsDTO dto = new NewsDTO();
        dto.setTitle("new-user-news");
        dto.setContent("User post content");
        dto.setCategory(categoryResponseDTO);

        setUpAsNotAdmin();

        // Chuẩn bị ảnh
        MultipartFile imageFile = new MockMultipartFile("image",
                "not-image-name", "not-image-type", "image".getBytes());

        // Thực hiện và kiểm tra
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.create(dto, imageFile);
        });

        assertEquals(ErrorCode.HTTP_FILE_IS_NOT_IMAGE, exception.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        Query query = entityManager.createQuery("select count(*) from News where title = :title");
        query.setParameter("title", "new-user-news");
        Long count = (Long) query.getSingleResult();
        assertEquals(0, count);
    }

    @Test
    @DisplayName("NS_create_07")
    void create_shouldSetStatusAs2AndReturnNewsResponse_whenUserIsAdminAndImageIsValid() throws IOException {
        // Chuẩn bị dữ liệu
        NewsDTO dto = new NewsDTO();
        dto.setTitle("new-admin-news");
        dto.setContent("Admin post content");
        dto.setCategory(categoryResponseDTO);

        setUpAsAdmin();

        // Chuẩn bị ảnh
        String fakeThumbnailUrl = "https://firebase.com/news/thumbnail/test-image.jpg";
        when(firebaseService.uploadOneFile(any(), any(), eq("news/thumbnail"))).thenReturn(fakeThumbnailUrl);

        MultipartFile imageFile = new MockMultipartFile("image",
                "image.png", "image/png", "image".getBytes());

        // Thực thi
        NewsResponseDTO response = defaultNewsService.create(dto, imageFile);

        // Kiểm tra dữ liệu trả về
        assertEquals("new-admin-news", response.getTitle());
        assertEquals("https://firebase.com/news/thumbnail/test-image.jpg", response.getThumbnail());

        // Kiểm tra dữ liệu trong DB
        Optional<News> news = newsRepository.findById(response.getNewsId());
        assertTrue(news.isPresent());
        assertEquals("new-admin-news", news.get().getTitle());
        assertEquals("Admin post content", news.get().getContent());
        assertEquals("https://firebase.com/news/thumbnail/test-image.jpg", news.get().getThumbnail());
        assertEquals("admin@example.com", news.get().getCreatedBy().getEmail());
        assertEquals(2, news.get().getStatus());
    }

    @Test
    @DisplayName("NS_create_08")
    void create_shouldSetStatusAs1AndReturnNewsResponse_whenUserIsNotAdminAndImageIsValid() throws IOException {
        // Chuẩn bị dữ liệu
        NewsDTO dto = new NewsDTO();
        dto.setTitle("new-user-news");
        dto.setContent("User post content");
        dto.setCategory(categoryResponseDTO);

        setUpAsNotAdmin();

        // Chuẩn bị ảnh
        String fakeThumbnailUrl = "https://firebase.com/news/thumbnail/test-image.jpg";
        when(firebaseService.uploadOneFile(any(), any(), eq("news/thumbnail"))).thenReturn(fakeThumbnailUrl);

        MultipartFile imageFile = new MockMultipartFile("image",
                "image.png", "image/png", "image".getBytes());

        // Thực thi
        NewsResponseDTO response = defaultNewsService.create(dto, imageFile);

        // Kiểm tra dữ liệu trả về
        assertEquals("new-user-news", response.getTitle());
        assertEquals("https://firebase.com/news/thumbnail/test-image.jpg", response.getThumbnail());

        // Kiểm tra dữ liệu trong DB
        Optional<News> news = newsRepository.findById(response.getNewsId());
        assertTrue(news.isPresent());
        assertEquals("new-user-news", news.get().getTitle());
        assertEquals("User post content", news.get().getContent());
        assertEquals("https://firebase.com/news/thumbnail/test-image.jpg", news.get().getThumbnail());
        assertEquals("user@example.com", news.get().getCreatedBy().getEmail());
        assertEquals(1, news.get().getStatus());
    }

    @Test
    @DisplayName("NS_update_01")
    void update_shouldThrowUnauthorized_whenCurrentUserNotFound() {
        // Chuẩn bị dữ liệu
        NewsUpdateDTO dto = new NewsUpdateDTO();
        dto.setTitle("title2");
        dto.setContent("content2");
        dto.setCategoryDTO(categoryResponseDTO);

        setUpAsNonExistUser();

        // Thực hiện chạy và kiểm tra
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.update(dto, preSaveNews.getNewsId(), null);
        });
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, exception.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        Query query = entityManager.createQuery("select count(*) from News where title = :title");
        query.setParameter("title", "title2");
        Long count = (Long) query.getSingleResult();
        assertEquals(0, count);
    }

    @Test
    @DisplayName("NS_update_02")
    void update_shouldThrowDuplicateTitle_whenTitleAlreadyExistsAndDifferentFromCurrent() {
        // Chuẩn bị dữ liệu
        newsRepository.save(News.builder().title("title2").content("content2").build());
        NewsUpdateDTO dto = new NewsUpdateDTO();
        dto.setTitle("title2");
        dto.setContent("content2");
        dto.setCategoryDTO(categoryResponseDTO);

        setUpAsAdmin();

        // Thực hiện chạy và kiểm tra
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.update(dto, preSaveNews.getNewsId(), null);
        });
        assertEquals(ErrorCode.DUPLICATE_TITLE, exception.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        Optional<News> updatedNews = newsRepository.findById(preSaveNews.getNewsId());
        assertTrue(updatedNews.isPresent());
        assertNotEquals("title2", updatedNews.get().getTitle());
    }

    @Test
    @DisplayName("NS_update_03")
    void update_shouldThrowNewsNotFound_whenNewsDoesNotExist() {
        // Chuẩn bị dữ liệu
        NewsUpdateDTO dto = new NewsUpdateDTO();
        dto.setTitle("title2");
        dto.setContent("content2");
        dto.setCategoryDTO(categoryResponseDTO);

        setUpAsAdmin();

        // Thực hiện chạy và kiểm tra
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.update(dto, BigInteger.valueOf(-1), null);
        });
        assertEquals(ErrorCode.HTTP_NEWS_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    @DisplayName("NS_update_04")
    void update_shouldThrowAccessDenied_whenUserIsNotAdminOrOwnerOfNews() {
        NewsUpdateDTO dto = new NewsUpdateDTO();
        dto.setTitle("title2");
        dto.setContent("content2");
        dto.setCategoryDTO(categoryResponseDTO);

        setUpAsNotAdmin();

        // Thực hiện chạy và kiểm tra
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.update(dto, preSaveNews.getNewsId(), null);
        });
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        Optional<News> updatedNews = newsRepository.findById(preSaveNews.getNewsId());
        assertTrue(updatedNews.isPresent());
        assertNotEquals("title2", updatedNews.get().getTitle());
    }

    @Test
    @DisplayName("NS_update_05")
    void update_shouldUpdateNewsWithoutImage_whenImageIsNull() {
        NewsUpdateDTO dto = new NewsUpdateDTO();
        dto.setTitle("title2");
        dto.setContent("content2");
        dto.setCategoryDTO(categoryResponseDTO);

        setUpAsAdmin();

        // Thực hiện chạy
        NewsResponseDTO response = defaultNewsService.update(dto, preSaveNews.getNewsId(), null);
        assertEquals(preSaveNews.getNewsId(), response.getNewsId());
        assertEquals("title2", response.getTitle());

        // Kiểm tra dữ liệu trong DB
        Optional<News> updatedNews = newsRepository.findById(preSaveNews.getNewsId());
        assertTrue(updatedNews.isPresent());
        assertEquals("title2", updatedNews.get().getTitle());
        assertEquals("content2", updatedNews.get().getContent());
    }

    @Test
    @DisplayName("NS_update_06")
    void update_shouldThrowFileIsNotImage_whenOldImageNotExistsAndImageHasInvalidContentType() {
        NewsUpdateDTO dto = new NewsUpdateDTO();
        dto.setTitle("title2");
        dto.setContent("content2");
        dto.setCategoryDTO(categoryResponseDTO);

        setUpAsAdmin();

        // Chuẩn bị file
        MultipartFile file = new MockMultipartFile("image", "not-image-type", "not-image-type", "image".getBytes());

        // Thực hiện chạy
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.update(dto, preSaveNews.getNewsId(), file);
        });
        assertEquals(ErrorCode.HTTP_FILE_IS_NOT_IMAGE, exception.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        Optional<News> updatedNews = newsRepository.findById(preSaveNews.getNewsId());
        assertTrue(updatedNews.isPresent());
        assertNotEquals("title2", updatedNews.get().getTitle());
        assertNotEquals("content2", updatedNews.get().getContent());
    }

    @Test
    @DisplayName("NS_update_07")
    void update_shouldThrowUploadFailed_whenOldImageNotExistsAndUploadFailed() throws IOException {
        NewsUpdateDTO dto = new NewsUpdateDTO();
        dto.setTitle("title2");
        dto.setContent("content2");
        dto.setCategoryDTO(categoryResponseDTO);

        setUpAsAdmin();

        // Chuẩn bị file
        MultipartFile file = new MockMultipartFile("image", "image/png", "image/png", "image".getBytes());

        // Mock firebase
        when(firebaseService.uploadOneFile(any(), any(), any())).thenThrow(IOException.class);

        // Thực hiện chạy
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.update(dto, preSaveNews.getNewsId(), file);
        });
        assertEquals(ErrorCode.UPLOAD_FAILED, exception.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        Optional<News> updatedNews = newsRepository.findById(preSaveNews.getNewsId());
        assertTrue(updatedNews.isPresent());
        assertNotEquals("title2", updatedNews.get().getTitle());
        assertNotEquals("content2", updatedNews.get().getContent());
    }

    @Test
    @DisplayName("NS_update_08")
    void update_shouldNewsWithImage_whenOldImageNotExists() throws IOException {
        NewsUpdateDTO dto = new NewsUpdateDTO();
        dto.setTitle("title2");
        dto.setContent("content2");
        dto.setCategoryDTO(categoryResponseDTO);

        setUpAsAdmin();

        // Chuẩn bị file
        MultipartFile file = new MockMultipartFile("image", "image/png", "image/png", "image".getBytes());

        // Mock firebase
        when(firebaseService.uploadOneFile(any(), any(), any())).thenReturn("new thumbnail url");

        // Thực hiện chạy
        NewsResponseDTO response = defaultNewsService.update(dto, preSaveNews.getNewsId(), file);
        assertEquals(preSaveNews.getNewsId(), response.getNewsId());
        assertEquals("title2", response.getTitle());

        // Kiểm tra dữ liệu trong DB
        Optional<News> updatedNews = newsRepository.findById(preSaveNews.getNewsId());
        assertTrue(updatedNews.isPresent());
        assertEquals("title2", updatedNews.get().getTitle());
        assertEquals("content2", updatedNews.get().getContent());
        assertEquals("new thumbnail url", updatedNews.get().getThumbnail());
    }

    @Test
    @DisplayName("NS_update_09")
    void update_shouldThrowDeleteFileFailed_whenOldImageExistsAndDeleteFails() throws IOException {
        preSaveNews.setThumbnail("invalid thumbnail url");
        preSaveNews = newsRepository.save(preSaveNews);

        NewsUpdateDTO dto = new NewsUpdateDTO();
        dto.setTitle("title2");
        dto.setContent("content2");
        dto.setCategoryDTO(categoryResponseDTO);

        setUpAsAdmin();

        // Chuẩn bị file
        MultipartFile file = new MockMultipartFile("image", "image.png", "image/png", "image".getBytes());

        // Mock firebase
        when(firebaseService.deleteFileByPath(any())).thenThrow(IOException.class);

        // Thực hiện chạy
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.update(dto, preSaveNews.getNewsId(), file);
        });
        assertEquals(ErrorCode.DELETE_FILE_FAILED, exception.getErrorCode());

        // Kiểm tra dữ liệu trong DB
        Optional<News> updatedNews = newsRepository.findById(preSaveNews.getNewsId());
        assertTrue(updatedNews.isPresent());
        assertNotEquals("title2", updatedNews.get().getTitle());
        assertNotEquals("content2", updatedNews.get().getContent());
    }

    @Test
    @DisplayName("NS_update_10")
    void update_shouldUpdateNewsWithImage_whenValidImageProvided() throws IOException {
        preSaveNews.setThumbnail("thumbnail url");
        preSaveNews = newsRepository.save(preSaveNews);

        NewsUpdateDTO dto = new NewsUpdateDTO();
        dto.setTitle("title2");
        dto.setContent("content2");
        dto.setCategoryDTO(categoryResponseDTO);

        setUpAsAdmin();

        // Chuẩn bị file
        MultipartFile file = new MockMultipartFile("image", "image.png", "image/png", "image".getBytes());

        // Mock firebase
        when(firebaseService.deleteFileByPath(any())).thenReturn(true);
        when(firebaseService.uploadOneFile(any(), any(), any())).thenReturn("new thumbnail url");

        // Thực hiện chạy
        NewsResponseDTO response = defaultNewsService.update(dto, preSaveNews.getNewsId(), file);
        assertEquals(preSaveNews.getNewsId(), response.getNewsId());
        assertEquals("title2", response.getTitle());

        // Kiểm tra dữ liệu trong DB
        Optional<News> updatedNews = newsRepository.findById(preSaveNews.getNewsId());
        assertTrue(updatedNews.isPresent());
        assertEquals("title2", updatedNews.get().getTitle());
        assertEquals("content2", updatedNews.get().getContent());
        assertEquals("new thumbnail url", updatedNews.get().getThumbnail());
    }

    @Test
    @DisplayName("NS_updateStatus_01")
    void updateStatus_shouldThrowNewsNotFound_whenNewsDoesNotExist() {
        // Chuẩn bị dữ liệu test
        setUpAsAdmin();

        // Tạo request với newsId không tồn tại
        NewsChangeStatusDTO request = new NewsChangeStatusDTO();
        request.setNewsId(BigInteger.valueOf(999999));
        request.setStatus(2);

        // Thực hiện chạy
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.update(request);
        });

        // Kiểm tra kết quả
        assertEquals(ErrorCode.HTTP_NEWS_NOT_EXISTED, exception.getErrorCode());

        // Kiểm tra dữ liệu trong DB không bị thay đổi
        Optional<News> newsInDb = newsRepository.findById(BigInteger.valueOf(999999));
        assertFalse(newsInDb.isPresent());
    }

    @Test
    @DisplayName("NS_updateStatus_02")
    void updateStatus_shouldThrowCategoryInactive_whenCategoryIsInactive() {
        // Chuẩn bị dữ liệu test
        preSaveNews.getCategory().setIsActive(false);
        categoryRepository.save(preSaveNews.getCategory());
        preSaveNews = newsRepository.save(preSaveNews);
        int originalStatus = preSaveNews.getStatus();

        setUpAsAdmin();

        // Tạo request
        NewsChangeStatusDTO request = new NewsChangeStatusDTO();
        request.setNewsId(preSaveNews.getNewsId());
        request.setStatus(2);

        // Thực hiện chạy
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.update(request);
        });

        // Kiểm tra kết quả
        assertEquals(ErrorCode.CATEGORY_OF_NEWS_MUST_BE_ACTIVE, exception.getErrorCode());

        // Kiểm tra dữ liệu trong DB không bị thay đổi
        Optional<News> newsInDb = newsRepository.findById(preSaveNews.getNewsId());
        assertTrue(newsInDb.isPresent());
        assertEquals(originalStatus, newsInDb.get().getStatus());
    }

    @Test
    @DisplayName("NS_updateStatus_03")
    void updateStatus_shouldThrowUnauthorized_whenUserNotFoundInSecurityContext() {
        // Chuẩn bị dữ liệu test
        int originalStatus = preSaveNews.getStatus();

        // Mock SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // Tạo request
        NewsChangeStatusDTO request = new NewsChangeStatusDTO();
        request.setNewsId(preSaveNews.getNewsId());
        request.setStatus(2);

        // Thực hiện chạy
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.update(request);
        });

        // Kiểm tra kết quả
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, exception.getErrorCode());

        // Kiểm tra dữ liệu trong DB không bị thay đổi
        Optional<News> newsInDb = newsRepository.findById(preSaveNews.getNewsId());
        assertTrue(newsInDb.isPresent());
        assertEquals(originalStatus, newsInDb.get().getStatus());
    }

    @Test
    @DisplayName("NS_updateStatus_04")
    void updateStatus_shouldThrowUnauthorized_whenUserAccountDoesNotExist() {
        // Chuẩn bị dữ liệu test
        int originalStatus = preSaveNews.getStatus();
        setUpAsNonExistUser();

        // Tạo request
        NewsChangeStatusDTO request = new NewsChangeStatusDTO();
        request.setNewsId(preSaveNews.getNewsId());
        request.setStatus(2);

        // Thực hiện chạy
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.update(request);
        });

        // Kiểm tra kết quả
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, exception.getErrorCode());

        // Kiểm tra dữ liệu trong DB không bị thay đổi
        Optional<News> newsInDb = newsRepository.findById(preSaveNews.getNewsId());
        assertTrue(newsInDb.isPresent());
        assertEquals(originalStatus, newsInDb.get().getStatus());
    }

    @Test
    @DisplayName("NS_updateStatus_05")
    void updateStatus_shouldThrowAccessDenied_whenUserDoesNotHaveAdminRole() {
        // Chuẩn bị dữ liệu test
        int originalStatus = preSaveNews.getStatus();
        setUpAsNotAdmin();

        // Tạo request
        NewsChangeStatusDTO request = new NewsChangeStatusDTO();
        request.setNewsId(preSaveNews.getNewsId());
        request.setStatus(2);

        // Thực hiện chạy
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.update(request);
        });

        // Kiểm tra kết quả
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());

        // Kiểm tra dữ liệu trong DB không bị thay đổi
        Optional<News> newsInDb = newsRepository.findById(preSaveNews.getNewsId());
        assertTrue(newsInDb.isPresent());
        assertEquals(originalStatus, newsInDb.get().getStatus());
    }

    @Test
    @DisplayName("NS_updateStatus_06")
    void updateStatus_shouldUpdateNewsStatus_whenRequestIsValid() {
        // Chuẩn bị dữ liệu test
        setUpAsAdmin();

        // Tạo request
        NewsChangeStatusDTO request = new NewsChangeStatusDTO();
        request.setNewsId(preSaveNews.getNewsId());
        request.setStatus(2);

        // Thực hiện chạy
        NewsResponseDTO response = defaultNewsService.update(request);

        // Kiểm tra kết quả
        assertEquals(preSaveNews.getNewsId(), response.getNewsId());

        // Kiểm tra dữ liệu trong DB
        Optional<News> updatedNews = newsRepository.findById(preSaveNews.getNewsId());
        assertTrue(updatedNews.isPresent());
        assertEquals(2, updatedNews.get().getStatus());
    }

    @Test
    @DisplayName("NS_viewDetail_01")
    void viewDetail_shouldThrowNewsNotExisted_whenNewsDoesNotExist() {
        // Thực hiện chạy
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.viewDetail(BigInteger.valueOf(999999));
        });

        // Kiểm tra kết quả
        assertEquals(ErrorCode.HTTP_NEWS_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    @DisplayName("NS_viewDetail_02")
    void viewDetail_shouldReturnNewsWithEmptyThumbnail_whenNewsHasNoThumbnail() {
        // Chuẩn bị dữ liệu test
        preSaveNews.setThumbnail(null);
        preSaveNews = newsRepository.save(preSaveNews);

        // Thực hiện chạy
        NewsResponseDTO response = defaultNewsService.viewDetail(preSaveNews.getNewsId());

        // Kiểm tra kết quả
        assertNotNull(response);
        assertEquals(preSaveNews.getNewsId(), response.getNewsId());
        assertEquals(preSaveNews.getTitle(), response.getTitle());
        assertEquals(preSaveNews.getContent(), response.getContent());
        assertEquals(response.getThumbnail(), "");
        // Kiểm tra các trường khác vẫn có dữ liệu
        assertNotNull(response.getCreatedBy());
        assertNotNull(response.getUpdatedBy());
    }

    @Test
    @DisplayName("NS_viewDetail_03")
    void viewDetail_shouldReturnNewsWithEmptyCreatedBy_whenNewsHasNoCreatedBy() {
        // Chuẩn bị dữ liệu test
        preSaveNews.setCreatedBy(null);
        preSaveNews = newsRepository.save(preSaveNews);

        // Thực hiện chạy
        NewsResponseDTO response = defaultNewsService.viewDetail(preSaveNews.getNewsId());

        // Kiểm tra kết quả
        assertNotNull(response);
        assertEquals(preSaveNews.getNewsId(), response.getNewsId());
        assertEquals(preSaveNews.getTitle(), response.getTitle());
        assertEquals(preSaveNews.getContent(), response.getContent());
        assertNull(response.getCreatedBy());
        // Kiểm tra các trường khác vẫn có dữ liệu
        assertNotNull(response.getUpdatedBy());
        assertNotNull(response.getThumbnail());
    }

    @Test
    @DisplayName("NS_viewDetail_04")
    void viewDetail_shouldReturnNewsWithEmptyUpdatedBy_whenNewsHasNoUpdatedBy() {
        // Chuẩn bị dữ liệu test
        preSaveNews.setUpdatedBy(null);
        preSaveNews = newsRepository.save(preSaveNews);

        // Thực hiện chạy
        NewsResponseDTO response = defaultNewsService.viewDetail(preSaveNews.getNewsId());

        // Kiểm tra kết quả
        assertNotNull(response);
        assertEquals(preSaveNews.getNewsId(), response.getNewsId());
        assertEquals(preSaveNews.getTitle(), response.getTitle());
        assertEquals(preSaveNews.getContent(), response.getContent());
        assertNull(response.getUpdatedBy());
        // Kiểm tra các trường khác vẫn có dữ liệu
        assertNotNull(response.getCreatedBy());
        assertNotNull(response.getThumbnail());
    }

    @Test
    @DisplayName("NS_viewDetail_05")
    void viewDetail_shouldReturnFullNewsResponse_whenNewsHasAllInformation() {
        // Chuẩn bị dữ liệu test
        preSaveNews.setThumbnail("thumbnail-url");
        preSaveNews = newsRepository.save(preSaveNews);

        // Thực hiện chạy
        NewsResponseDTO response = defaultNewsService.viewDetail(preSaveNews.getNewsId());

        // Kiểm tra kết quả
        assertNotNull(response);
        assertEquals(preSaveNews.getNewsId(), response.getNewsId());
        assertEquals(preSaveNews.getTitle(), response.getTitle());
        assertEquals(preSaveNews.getContent(), response.getContent());
        assertEquals(preSaveNews.getThumbnail(), response.getThumbnail());
        assertEquals(preSaveNews.getStatus(), response.getStatus());
        assertNotNull(response.getCreatedBy());
        assertEquals(preSaveNews.getCreatedBy().getEmail(), response.getCreatedBy().getEmail());
        assertNotNull(response.getUpdatedBy());
        assertEquals(preSaveNews.getUpdatedBy().getEmail(), response.getUpdatedBy().getEmail());
    }

    @Test
    @DisplayName("NS_viewNewsByAccount_01")
    void viewNewsByAccount_shouldThrowUnauthorized_whenEmailNotExist() {
        // Chuẩn bị dữ liệu test
        String nonExistEmail = "nonexist@example.com";

        // Thực hiện chạy
        AppException exception = assertThrows(AppException.class, () -> {
            defaultNewsService.viewNewsByAccount(1, 5, nonExistEmail, null, null, null, null, null);
        });

        // Kiểm tra kết quả
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    @DisplayName("NS_viewNewsByAccount_02")
    void viewNewsByAccount_shouldReturnEmptyCreatedByNews_whenNewsHasNoCreatedBy() {
        // Chuẩn bị dữ liệu test
        newsRepository.deleteAll();
        for (int i = 0; i < 5; i++) {
            News newsWithoutCreatedBy = News.builder()
                    .title("News without createdBy")
                    .content("Content")
                    .category(preSaveNews.getCategory())
                    .status(2)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(accountRepository.findByEmail("admin@example.com").get())
                    .build();
            newsRepository.save(newsWithoutCreatedBy);
        }

        // Thực hiện chạy
        PageResponse<?> response = defaultNewsService.viewNewsByAccount(
                0, 5, "admin@example.com", null, null, null, null, null);

        // Kiểm tra kết quả
        assertNotNull(response);
        assertEquals(5, response.getLimit());
        assertEquals(0, response.getOffset());
        List<NewsResponseDTO> news = (List<NewsResponseDTO>) response.getContent();
        assertTrue(news.isEmpty());
    }

    @Test
    @DisplayName("NS_viewNewsByAccount_03")
    void viewNewsByAccount_shouldReturnEmptyUpdatedByNews_whenNewsHasNoUpdatedBy() {
        // Chuẩn bị dữ liệu test
        newsRepository.deleteAll();
        for (int i = 0; i < 5; i++) {
            News newsWithoutCreatedBy = News.builder()
                    .title("News without updatedBy")
                    .content("Content")
                    .category(preSaveNews.getCategory())
                    .status(2)
                    .createdAt(LocalDateTime.now())
                    .createdBy(accountRepository.findByEmail("admin@example.com").get())
                    .updatedAt(LocalDateTime.now())
                    .build();
            newsRepository.save(newsWithoutCreatedBy);
        }

        // Thực hiện chạy
        PageResponse<?> response = defaultNewsService.viewNewsByAccount(
                0, 5, "admin@example.com", null, null, null, null, null);

        // Kiểm tra kết quả
        assertNotNull(response);
        assertEquals(5, response.getLimit());
        assertEquals(0, response.getOffset());
        List<NewsResponseDTO> news = (List<NewsResponseDTO>) response.getContent();
        assertFalse(news.isEmpty());
        assertTrue(news.stream().anyMatch(n -> n.getTitle().equals("News without updatedBy")));
        assertTrue(news.stream()
                .filter(n -> n.getTitle().equals("News without updatedBy"))
                .allMatch(n -> n.getUpdatedBy() == null));
    }

    @Test
    @DisplayName("NS_viewNewsByAccount_04")
    void viewNewsByAccount_shouldReturnFullNewsResponse_whenNewsHasAllInformation() {
        newsRepository.deleteAll();
        for (int i = 0; i < 5; i++) {
            News newsWithoutCreatedBy = News.builder()
                    .title("News")
                    .content("Content")
                    .category(preSaveNews.getCategory())
                    .status(2)
                    .createdAt(LocalDateTime.now())
                    .createdBy(accountRepository.findByEmail("admin@example.com").get())
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(accountRepository.findByEmail("admin@example.com").get())
                    .build();
            newsRepository.save(newsWithoutCreatedBy);
        }

        // Thực hiện chạy
        PageResponse<?> response = defaultNewsService.viewNewsByAccount(
                0, 5, "admin@example.com", null, null, null, null, null);

        // Kiểm tra kết quả
        assertNotNull(response);
        assertEquals(5, response.getLimit());
        assertEquals(0, response.getOffset());
        List<NewsResponseDTO> news = (List<NewsResponseDTO>) response.getContent();
        assertFalse(news.isEmpty());
        assertTrue(news.stream().anyMatch(n -> n.getTitle().equals("News")));
        assertTrue(news.stream()
                .allMatch(n -> n.getTitle().equals("News")));
    }

    @Test
    @DisplayName("NS_viewByFilter_01")
    void viewByFilter_shouldReturnNewsResponseList_whenFilterWithNullConditions() {
        // Chuẩn bị dữ liệu test
        newsRepository.deleteAll();
        for (int i = 0; i < 5; i++) {
            News newsWithoutCreatedBy = News.builder()
                    .title("News")
                    .content("Content")
                    .category(preSaveNews.getCategory())
                    .status(2)
                    .createdAt(LocalDateTime.now())
                    .createdBy(accountRepository.findByEmail("admin@example.com").get())
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(accountRepository.findByEmail("admin@example.com").get())
                    .build();
            newsRepository.save(newsWithoutCreatedBy);
        }

        // Thực hiện chạy
        PageResponse<?> response = defaultNewsService.viewByFilter(
                0, 5, null, null, null, null, null, null);

        // Kiểm tra kết quả
        assertNotNull(response);
        assertEquals(5, response.getLimit());
        assertEquals(0, response.getOffset());
        List<NewsResponseDTO> news = (List<NewsResponseDTO>) response.getContent();
        assertFalse(news.isEmpty());
        assertEquals(5, news.size());
        assertTrue(news.stream().anyMatch(n -> n.getTitle().equals("News")));
        assertTrue(news.stream()
                .allMatch(n -> n.getTitle().equals("News")));
    }

    @Test
    @DisplayName("NS_viewNewsClientByFilter_01")
    void viewNewsClientByFilter_shouldReturnNewsWithoutThumbnail_whenNewsHasNoThumbnail() {
        // Chuẩn bị dữ liệu test
        newsRepository.deleteAll();
        for (int i = 0; i < 5; i++) {
            News newsWithoutCreatedBy = News.builder()
                    .title("News")
                    .content("Content")
                    .category(preSaveNews.getCategory())
                    .status(2)
                    .createdAt(LocalDateTime.now())
                    .createdBy(accountRepository.findByEmail("admin@example.com").get())
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(accountRepository.findByEmail("admin@example.com").get())
                    .build();
            newsRepository.save(newsWithoutCreatedBy);
        }

        // Thực hiện chạy
        PageResponse<?> response = defaultNewsService.viewNewsClientByFilter(
                0, 5, null, null);

        // Kiểm tra kết quả
        assertNotNull(response);
        assertEquals(5, response.getLimit());
        assertEquals(0, response.getOffset());
        List<NewsResponseDTO> news = (List<NewsResponseDTO>) response.getContent();
        assertFalse(news.isEmpty());
        assertTrue(news.stream().anyMatch(n -> n.getTitle().equals("News")));
        assertTrue(news.stream()
                .allMatch(n -> n.getTitle().equals("News") && "".equals(n.getThumbnail())));
    }

    @Test
    @DisplayName("NS_viewNewsClientByFilter_02")
    void viewNewsClientByFilter_shouldReturnFullNewsResponse_whenNewsHasAllInformation() {
        // Chuẩn bị dữ liệu test
        newsRepository.deleteAll();
        for (int i = 0; i < 5; i++) {
            News newsWithoutCreatedBy = News.builder()
                    .title("News")
                    .content("Content")
                    .category(preSaveNews.getCategory())
                    .thumbnail("Thumbnail")
                    .status(2)
                    .createdAt(LocalDateTime.now())
                    .createdBy(accountRepository.findByEmail("admin@example.com").get())
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(accountRepository.findByEmail("admin@example.com").get())
                    .build();
            newsRepository.save(newsWithoutCreatedBy);
        }

        // Thực hiện chạy
        PageResponse<?> response = defaultNewsService.viewNewsClientByFilter(
                0, 5, null, null);

        // Kiểm tra kết quả
        assertNotNull(response);
        assertEquals(5, response.getLimit());
        assertEquals(0, response.getOffset());
        List<NewsResponseDTO> news = (List<NewsResponseDTO>) response.getContent();
        assertFalse(news.isEmpty());
        assertTrue(news.stream().anyMatch(n -> n.getTitle().equals("News")));
        assertTrue(news.stream()
                .allMatch(n -> n.getTitle().equals("News") && "Thumbnail".equals(n.getThumbnail())));
    }
}

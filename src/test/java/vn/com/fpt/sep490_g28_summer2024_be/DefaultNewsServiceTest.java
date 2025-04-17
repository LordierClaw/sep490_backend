package vn.com.fpt.sep490_g28_summer2024_be;

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
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Category;
import vn.com.fpt.sep490_g28_summer2024_be.entity.News;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseService;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseServiceImpl;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.CategoryRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.NewsRepository;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.service.campaign.CampaignService;
import vn.com.fpt.sep490_g28_summer2024_be.service.news.DefaultNewsService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//@ActiveProfiles("test")
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
    private AccountRepository accountRepository;
    @Autowired
    private FirebaseService firebaseService;
    @Autowired
    private DefaultNewsService defaultNewsService;

    private CategoryResponseDTO categoryResponseDTO;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .title("category1")
                .description("descp")
                .slug("slug1")
                .isActive(true)
                .build();

        category = categoryRepository.save(category);
        categoryResponseDTO = new CategoryResponseDTO();
        BeanUtils.copyProperties(category, categoryResponseDTO);

        newsRepository.save(News.builder()
                .title("existedTitle1")
                .content("content1")
                .category(category)
                .build());
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

        // Lưu user tương ứng vào database
        Account adminAccount = new Account();
        adminAccount.setEmail("admin@example.com");
        adminAccount.setPassword("password");
        accountRepository.save(adminAccount);
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

        // Lưu user tương ứng vào database
        Account userAccount = new Account();
        userAccount.setEmail("user@example.com");
        userAccount.setPassword("password");
        accountRepository.save(userAccount);
    }

    @Test
    @DisplayName("NS_create_01")
    void create_shouldThrowDuplicateTitleException_whenTitleAlreadyExists() throws Exception {
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

        // Mock CustomAccountDetails
        CustomAccountDetails mockDetails = mock(CustomAccountDetails.class);
        when(mockDetails.getUsername()).thenReturn("nonexistent@example.com");

        // Đưa vào SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken(mockDetails, null);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

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
    void create_shouldSetStatusAs2AndReturnNewsResponse_whenUserIsAdminAndImageIsNull() throws IOException {
        // Chuẩn bị dữ liệu
        NewsDTO dto = new NewsDTO();
        dto.setTitle("new-admin-news");
        dto.setContent("Admin post content");
        dto.setCategory(categoryResponseDTO);

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

        // Lưu user tương ứng vào database
        Account adminAccount = new Account();
        adminAccount.setEmail("admin@example.com");
        adminAccount.setPassword("password");
        accountRepository.save(adminAccount);

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
    void create_shouldSetStatusAs1AndReturnNewsResponse_whenUserIsNotAdminAndImageIsNull() throws IOException {
        // Chuẩn bị dữ liệu
        NewsDTO dto = new NewsDTO();
        dto.setTitle("new-user-news");
        dto.setContent("User post content");
        dto.setCategory(categoryResponseDTO);

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

        // Lưu user tương ứng vào database
        Account userAccount = new Account();
        userAccount.setEmail("user@example.com");
        userAccount.setPassword("password");
        accountRepository.save(userAccount);

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
    void create_shouldSetStatusAs2AndReturnNewsResponse_whenUserIsAdminAndImageIsNotValid() {
        // Chuẩn bị dữ liệu
        NewsDTO dto = new NewsDTO();
        dto.setTitle("new-admin-news");
        dto.setContent("Admin post content");
        dto.setCategory(categoryResponseDTO);

        setUpAsAdmin();

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
        query.setParameter("title", "new-admin-news");
        Long count = (Long) query.getSingleResult();
        assertEquals(0, count);
    }

    @Test
    @DisplayName("NS_create_06")
    void create_shouldSetStatusAs1AndReturnNewsResponse_whenUserIsNotAdminAndImageIsNotValid() throws IOException {
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
}

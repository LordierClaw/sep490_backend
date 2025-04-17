package vn.com.fpt.sep490_g28_summer2024_be;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fpt.sep490_g28_summer2024_be.dto.category.CategoryCountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.category.CategoryRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.category.CategoryResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Category;
import vn.com.fpt.sep490_g28_summer2024_be.entity.News;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.repository.CategoryRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.NewsRepository;
import vn.com.fpt.sep490_g28_summer2024_be.service.category.DefaultCategoryService;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class DefaultCategoryServiceTest {

    @Autowired
    private DefaultCategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NewsRepository newsRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .title("Sample Category")
                .description("This is a test")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .newsList(new ArrayList<>())
                .build();
        category = categoryRepository.save(category);
    }

    @Test
    @DisplayName("CS_create_01")
    void CS_create_01() {
        // Chuẩn bị dữ liệu test
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setTitle("Tech");
        
        // Tạo category với title "Tech" trước
        Category existingCategory = Category.builder()
                .title("Tech")
                .description("Existing Tech category")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .newsList(new ArrayList<>())
                .build();
        categoryRepository.save(existingCategory);

        // Kiểm tra ném exception khi tạo category trùng title
        assertThrows(AppException.class, () -> categoryService.create(request));
    }

    @Test
    @DisplayName("CS_create_02")
    void CS_create_02() {
        // Chuẩn bị dữ liệu test
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setTitle("Health");
        request.setDescription("Health category");

        // Thực thi và kiểm tra kết quả
        CategoryResponseDTO response = categoryService.create(request);
        
        assertNotNull(response);
        assertEquals("Health", response.getTitle());
        assertNotNull(response.getSlug());

        // Kiểm tra trong database
        Category savedCategory = categoryRepository.findById(response.getCategoryId()).orElse(null);
        assertNotNull(savedCategory);
        assertEquals("Health", savedCategory.getTitle());
    }

    @Test
    @DisplayName("CS_update_01")
    void CS_update_01() {
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setTitle("Updated Title");
        request.setDescription("Updated Description");

        CategoryResponseDTO response = categoryService.update(request, category.getCategoryId());

        assertEquals("Updated Title", response.getTitle());
        assertEquals("Updated Description", response.getDescription());

        Category updated = categoryRepository.findById(category.getCategoryId()).orElse(null);
        assertNotNull(updated);
        assertEquals("Updated Title", updated.getTitle());
    }

    @Test
    @DisplayName("CS_update_02")
    void CS_update_02() {
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setTitle("Test");
        request.setDescription("Desc");

        BigInteger fakeId = BigInteger.valueOf(999);
        assertThrows(AppException.class, () -> categoryService.update(request, fakeId));
    }

    @Test
    @DisplayName("CS_updateStatus_01")
    void CS_updateStatus_01() {
        // Tạo news gắn với category
        News news = News.builder()
                .title("News")
                .content("News content test")
                .status(1)
                .category(category)
                .build();
        newsRepository.save(news);

        CategoryResponseDTO response = categoryService.updateStatus(category.getCategoryId(), false);

        assertFalse(response.getIsActive());
        News updatedNews = newsRepository.findById(news.getNewsId()).orElse(null);
        assertEquals(3, updatedNews.getStatus());
    }

    @Test
    @DisplayName("CS_updateStatus_02")
    void CS_updateStatus_02() {
        // Đầu tiên set category thành inactive
        categoryService.updateStatus(category.getCategoryId(), false);
        
        // Kích hoạt lại category
        CategoryResponseDTO response = categoryService.updateStatus(category.getCategoryId(), true);

        assertTrue(response.getIsActive());
    }

    @Test
    @DisplayName("CS_updateStatus_03")
    void CS_updateStatus_03() {
        BigInteger fakeId = BigInteger.valueOf(999);
        assertThrows(AppException.class, () ->
                categoryService.updateStatus(fakeId, false));
    }

    @Test
    @DisplayName("CS_getCategory_01")
    void CS_getCategory_01() {
        CategoryResponseDTO response = categoryService.getCategory(category.getCategoryId());
        
        assertNotNull(response);
        assertEquals("Sample Category", response.getTitle());
        assertEquals("This is a test", response.getDescription());
        assertTrue(response.getIsActive());
    }

    @Test
    @DisplayName("CS_getCategory_02")
    void CS_getCategory_02() {
        BigInteger fakeId = BigInteger.valueOf(999);
        assertThrows(AppException.class, () ->
                categoryService.getCategory(fakeId));
    }

    @Test
    @DisplayName("CS_viewList_01")
    void CS_viewList_01() {
        // Tạo thêm một số category và news để test
        Category category2 = Category.builder()
                .title("Category 2")
                .description("Test Category 2")
                .isActive(true)
                .newsList(new ArrayList<>())
                .build();
        category2 = categoryRepository.save(category2);

        Category category3 = Category.builder()
                .title("Category 3")
                .description("Test Category 3")
                .isActive(true)
                .newsList(new ArrayList<>())
                .build();
        category3 = categoryRepository.save(category3);

        // Tạo news cho các category
        News news1 = News.builder()
                .title("News 1")
                .content("Content for News 1")
                .status(1)
                .category(category)
                .build();
        News news2 = News.builder()
                .title("News 2")
                .content("Content for News 2")
                .status(1)
                .category(category2)
                .build();
        News news3 = News.builder()
                .title("News 3")
                .content("Content for News 3")
                .status(1)
                .category(category3)
                .build();
        newsRepository.saveAll(List.of(news1, news2, news3));

        // Thực thi và kiểm tra kết quả
        CategoryCountDTO result = categoryService.viewList();

        assertNotNull(result);
        assertEquals(3, result.getData().size());
        assertTrue(result.getTotalNews() >= 3);

        // Kiểm tra số lượng news của từng category
        result.getData().forEach(dto -> {
            assertTrue(dto.getNumberNewsByCategories() >= 1);
        });
    }
}

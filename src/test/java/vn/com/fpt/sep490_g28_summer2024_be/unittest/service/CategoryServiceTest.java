package vn.com.fpt.sep490_g28_summer2024_be.unittest.service;

import jakarta.validation.ConstraintViolationException;
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
public class CategoryServiceTest {

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
    @DisplayName("CS_create_03")
    void CS_create_03() {
        // Test creating category with empty title
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setTitle("");
        request.setDescription("Empty title category");

        assertThrows(ConstraintViolationException.class, () -> categoryService.create(request));
    }

    @Test
    @DisplayName("CS_create_04")
    void CS_create_04() {
        // Test creating category with very long title
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setTitle("A".repeat(256)); // Exceed max length
        request.setDescription("Long title category");

        assertThrows(ConstraintViolationException.class, () -> categoryService.create(request));
    }

    @Test
    @DisplayName("CS_create_05")
    void CS_create_05() {
        // Test creating category with special characters in title
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setTitle("Category @#$% Special");
        request.setDescription("Category with special characters");

        CategoryResponseDTO response = categoryService.create(request);
        
        assertNotNull(response);
        assertEquals("Category @#$% Special", response.getTitle());
        assertNotNull(response.getSlug());
        assertTrue(response.getSlug().matches("^[a-z0-9-]+$")); // Verify slug format
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
    @DisplayName("CS_update_03")
    void CS_update_03() {
        // Test updating to an existing category title
        Category existingCategory = Category.builder()
                .title("Existing Category")
                .description("Existing Description")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        categoryRepository.save(existingCategory);

        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setTitle("Existing Category");
        request.setDescription("Updated Description");

        assertThrows(AppException.class, () -> categoryService.update(request, category.getCategoryId()));
    }

    @Test
    @DisplayName("CS_update_04")
    void CS_update_04() {
        // Test updating with same title (no change)
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setTitle(category.getTitle());
        request.setDescription("Updated description only");

        CategoryResponseDTO response = categoryService.update(request, category.getCategoryId());
        
        assertNotNull(response);
        assertEquals(category.getTitle(), response.getTitle());
        assertEquals("Updated description only", response.getDescription());
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
    @DisplayName("CS_updateStatus_04")
    void CS_updateStatus_04() {
        // Test updating status of a category with multiple news
        News news1 = News.builder()
                .title("News 1")
                .content("Content 1")
                .status(1)
                .category(category)
                .build();
        News news2 = News.builder()
                .title("News 2")
                .content("Content 2")
                .status(2)
                .category(category)
                .build();
        newsRepository.saveAll(List.of(news1, news2));

        CategoryResponseDTO response = categoryService.updateStatus(category.getCategoryId(), false);

        assertFalse(response.getIsActive());
        
        // Verify each news individually
        News updatedNews1 = newsRepository.findById(news1.getNewsId()).orElse(null);
        News updatedNews2 = newsRepository.findById(news2.getNewsId()).orElse(null);
        
        assertNotNull(updatedNews1);
        assertNotNull(updatedNews2);
        assertEquals(3, updatedNews1.getStatus());
        assertEquals(3, updatedNews2.getStatus());
    }

    @Test
    @DisplayName("CS_updateStatus_05")
    void CS_updateStatus_05() {
        // Test updating status to same value
        CategoryResponseDTO response = categoryService.updateStatus(category.getCategoryId(), true);
        
        assertNotNull(response);
        assertTrue(response.getIsActive());
        assertEquals(category.getTitle(), response.getTitle());
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

    @Test
    @DisplayName("CS_viewList_02")
    void CS_viewList_02() {
        // Test view list with inactive categories
        Category inactiveCategory = Category.builder()
                .title("Inactive Category")
                .description("Inactive Category Description")
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .build();
        categoryRepository.save(inactiveCategory);

        News news = News.builder()
                .title("News for Inactive")
                .content("Content")
                .status(3)
                .category(inactiveCategory)
                .build();
        newsRepository.save(news);

        CategoryCountDTO result = categoryService.viewList();
        
        assertNotNull(result);
        // Should include both active and inactive categories
        assertTrue(result.getData().stream()
                .anyMatch(dto -> dto.getTitle().equals("Inactive Category")));
        assertTrue(result.getData().stream()
                .anyMatch(dto -> !dto.getIsActive()));
    }

    @Test
    @DisplayName("CS_viewList_03")
    void CS_viewList_03() {
        // Test view list with empty categories (no news)
        categoryRepository.deleteAll();
        CategoryCountDTO result = categoryService.viewList();
        
        assertNotNull(result);
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getTotalNews());
    }

    @Test
    @DisplayName("CS_viewList_04")
    void CS_viewList_04() {
        // Test view list with categories having different news counts
        Category category1 = categoryRepository.save(Category.builder()
                .title("Category 1")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build());

        Category category2 = categoryRepository.save(Category.builder()
                .title("Category 2")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build());

        // Add 3 news to category1
        for (int i = 0; i < 3; i++) {
            newsRepository.save(News.builder()
                    .title("News " + i)
                    .content("Content " + i)
                    .status(1)
                    .category(category1)
                    .build());
        }

        // Add 1 news to category2
        newsRepository.save(News.builder()
                .title("Single News")
                .content("Content")
                .status(1)
                .category(category2)
                .build());

        CategoryCountDTO result = categoryService.viewList();
        
        assertNotNull(result);
        assertEquals(4, result.getTotalNews());
        
        // Verify news counts for each category
        result.getData().forEach(dto -> {
            if (dto.getTitle().equals("Category 1")) {
                assertEquals(3, dto.getNumberNewsByCategories());
            } else if (dto.getTitle().equals("Category 2")) {
                assertEquals(1, dto.getNumberNewsByCategories());
            }
        });
    }
}

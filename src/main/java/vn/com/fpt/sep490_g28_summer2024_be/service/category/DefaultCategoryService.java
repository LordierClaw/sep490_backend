package vn.com.fpt.sep490_g28_summer2024_be.service.category;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.category.CategoryCountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.category.CategoryRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.category.CategoryResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Category;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.mapper.Mapper;
import vn.com.fpt.sep490_g28_summer2024_be.repository.CategoryRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.NewsRepository;
import vn.com.fpt.sep490_g28_summer2024_be.utils.SlugUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultCategoryService implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final NewsRepository newsRepository;
    private final SlugUtils slugUtils;

    @Override
    public CategoryResponseDTO create(CategoryRequestDTO request) {
        if (categoryRepository.existsCategoriesByTitle(request.getTitle())) {
            throw new AppException(ErrorCode.DUPLICATE_TITLE);
        }

        Category category = Category.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .build();
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category.setIsActive(true);
        Category savedCategory = categoryRepository.save(category);

        String slug = slugUtils.genSlug(String.format("%s %s", savedCategory.getCategoryId(), savedCategory.getTitle()));
        savedCategory.setSlug(slug);
        categoryRepository.save(savedCategory);
        return Mapper.mapEntityToDto(savedCategory, CategoryResponseDTO.class);
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateStatus(BigInteger id, Boolean isActive) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.HTTP_NOT_FOUND));
        category.setIsActive(isActive);

        if (!isActive) {
            category.getNewsList().forEach(news -> {
                news.setStatus(3);
                newsRepository.save(news);
            });
        }

        categoryRepository.save(category);

        return CategoryResponseDTO.builder()
                .categoryId(category.getCategoryId())
                .title(category.getTitle())
                .isActive(category.getIsActive())
                .build();
    }

    @Override
    public CategoryResponseDTO getCategory(BigInteger id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.HTTP_NOT_FOUND));

        return CategoryResponseDTO.builder()
                .categoryId(category.getCategoryId())
                .title(category.getTitle())
                .description(category.getDescription())
                .slug(category.getSlug())
                .build();
    }

    @Override
    public CategoryResponseDTO update(CategoryRequestDTO request, BigInteger id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.HTTP_NOT_FOUND));

        category.setTitle(request.getTitle());
        category.setDescription(request.getDescription());
        category.setUpdatedAt(LocalDateTime.now());
        String slug = slugUtils.genSlug(String.format("%s %s", category.getCategoryId(), request.getTitle()));
        category.setSlug(slug);

        categoryRepository.save(category);
        return CategoryResponseDTO.builder()
                .categoryId(category.getCategoryId())
                .title(category.getTitle())
                .description(category.getDescription())
                .build();
    }


    @Override
    public CategoryCountDTO viewList() {
        List<Object[]> newsCounts = newsRepository.countNewsByCategoryAndStatus();

        Map<Long, Long> newsCountMap = newsCounts.stream()
                .collect(Collectors.toMap(
                        count -> ((Number) count[0]).longValue(), // Handle Long and BigInteger
                        count -> (Long) count[1]
                ));

        long totalNewsCount = newsCounts.stream()
                .mapToLong(count -> (Long) count[1])
                .sum();

        List<CategoryResponseDTO> categoryDTOList = new ArrayList<>();
        categoryRepository.findAll().forEach(category -> {
            categoryDTOList.add(CategoryResponseDTO.builder()
                    .categoryId(category.getCategoryId())
                    .title(category.getTitle())
                    .slug(category.getSlug())
                    .isActive(category.getIsActive())
                    .numberNewsByCategories(newsCountMap.getOrDefault(category.getCategoryId().longValue(), 0L))
                    .build());
        });

        return CategoryCountDTO.builder()
                .data(categoryDTOList)
                .totalNews(totalNewsCount)
                .build();
    }
}

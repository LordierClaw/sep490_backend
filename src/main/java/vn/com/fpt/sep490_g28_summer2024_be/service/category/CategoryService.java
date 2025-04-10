package vn.com.fpt.sep490_g28_summer2024_be.service.category;

import vn.com.fpt.sep490_g28_summer2024_be.dto.category.CategoryCountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.category.CategoryRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.category.CategoryResponseDTO;

import java.math.BigInteger;

public interface CategoryService {
    CategoryResponseDTO create(CategoryRequestDTO request);
    CategoryResponseDTO update(CategoryRequestDTO request, BigInteger id);
    CategoryResponseDTO updateStatus(BigInteger id, Boolean isActive);

    CategoryResponseDTO getCategory(BigInteger id);

    CategoryCountDTO viewList();
}

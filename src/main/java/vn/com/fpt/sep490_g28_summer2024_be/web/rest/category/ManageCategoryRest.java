package vn.com.fpt.sep490_g28_summer2024_be.web.rest.category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.category.CategoryRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.service.category.CategoryService;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class ManageCategoryRest {
        private final CategoryService categoryService;

        @GetMapping()
        @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SOCIAL_STAFF')")
        public ApiResponse<?> getAllCategories(){
            return ApiResponse.builder()
                    .code("200")
                    .message("Successfully!")
                    .data(categoryService.viewList())
                    .build();
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SOCIAL_STAFF')")
        public ApiResponse<?> getDetailCategory(@PathVariable("id") BigInteger id){
            return ApiResponse.builder()
                    .code("200")
                    .message("Successfully!")
                    .data(categoryService.getCategory(id))
                    .build();
        }

        @PostMapping("/add")
        @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
        public ApiResponse<?> createCategory(@RequestBody @Valid CategoryRequestDTO request){
            return ApiResponse.builder()
                    .code("200")
                    .message("Successfully!")
                    .data(categoryService.create(request))
                    .build();
        }

        @PutMapping("/update/{id}")
        @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
        public ApiResponse<?> updateCategory(@RequestBody @Valid CategoryRequestDTO request,
                                             @PathVariable("id") BigInteger id){
            return ApiResponse.builder()
                    .code("200")
                    .message("Successfully!")
                    .data(categoryService.update(request, id))
                    .build();
        }

        @PutMapping("/change-status/{id}/{status}")
        @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
        public ApiResponse<?> changeStatus(@PathVariable("id") BigInteger id,
                                           @PathVariable("status") Boolean isActive){
            return ApiResponse.builder()
                    .code("200")
                    .message("Successfully!")
                    .data(categoryService.updateStatus(id, isActive))
                    .build();
        }

}

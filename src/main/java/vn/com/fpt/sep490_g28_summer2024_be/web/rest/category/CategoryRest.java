package vn.com.fpt.sep490_g28_summer2024_be.web.rest.category;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.com.fpt.sep490_g28_summer2024_be.common.CommonMessage;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.service.category.CategoryService;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryRest {

    public final CategoryService categoryService;

    @GetMapping("")
    public ApiResponse<?> viewListCategory(){
        return ApiResponse.builder()
                .message(CommonMessage.GET_SUCCESFULLY)
                .data(categoryService.viewList())
                .build();
    }

}

package vn.com.fpt.sep490_g28_summer2024_be.web.rest.news;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.CommonMessage;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsChangeStatusDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsUpdateDTO;

import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.service.news.NewsService;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/news")
@RequiredArgsConstructor
public class NewsRest {

    public final NewsService newsService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> create(@RequestPart @Valid NewsDTO request, @RequestPart(required = false) MultipartFile image) throws IOException {
        System.out.println(request);
        System.out.println(request);
        return ApiResponse.builder()
                .code("200")
                .message("Thêm tin tức mới thành công")
                .data(newsService.create(request, image))
                .build();
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER','ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> view(@RequestParam(defaultValue = "0", required = false) Integer page,
                               @RequestParam(defaultValue = "10", required = false) Integer size,
                               @RequestParam(required = false) BigInteger category_id,
                               @RequestParam(required = false) String title,
                               @RequestParam(value = "author_id", required = false) BigInteger authorId,
                               @RequestParam(required = false) Integer status,
                               @RequestParam(required = false) LocalDate startDate,
                               @RequestParam(required = false) LocalDate endDate) {
        return ApiResponse.builder()
                .code("200")
                .message("Danh sách tin tức")
                .data(newsService.viewByFilter(page, size, category_id, title, authorId, status, startDate, endDate))
                .build();
    }

    @GetMapping("/is-created")
    @PreAuthorize("hasAnyRole('ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> viewNewsByAccount(@RequestParam(defaultValue = "0", required = false) Integer page,
                                            @RequestParam(defaultValue = "10", required = false) Integer size,
                                            @AuthenticationPrincipal CustomAccountDetails userDetails,
                                            @RequestParam(required = false) BigInteger category_id,
                                            @RequestParam(required = false) String title,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) LocalDate startDate,
                                            @RequestParam(required = false) LocalDate endDate) {
        return ApiResponse.builder()
                .code("200")
                .message("Danh sách tin tức")
                .data(newsService.viewNewsByAccount(
                        page, size, userDetails.getUsername(), category_id, title,status, startDate, endDate))
                .build();
    }


    @PutMapping("/change-status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> changeStatus(@RequestBody @Valid NewsChangeStatusDTO request) {
        return ApiResponse.builder()
                .message(CommonMessage.UPDATE_SUCCESSFULLY)
                .data(newsService.update(request))
                .build();
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> update(@PathVariable BigInteger id, @RequestPart @Valid NewsUpdateDTO request, @RequestPart(required = false) MultipartFile image) throws IOException {
        return ApiResponse.builder()
                .message(CommonMessage.UPDATE_SUCCESSFULLY)
                .data(newsService.update(request, id, image))
                .build();
    }
}

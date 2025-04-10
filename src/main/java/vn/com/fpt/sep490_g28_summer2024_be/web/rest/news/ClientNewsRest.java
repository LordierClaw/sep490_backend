package vn.com.fpt.sep490_g28_summer2024_be.web.rest.news;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.service.news.NewsService;
import java.math.BigInteger;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class ClientNewsRest {

    public final NewsService newsService;
    @GetMapping("/{id}")
    public ApiResponse<?> viewDetail(@PathVariable("id") BigInteger id){
        return ApiResponse.builder()
                .code("200")
                .message("CHI TIẾT TIN TỨC")
                .data(newsService.viewDetail(id))
                .build();
    }

    @GetMapping("")
    public ApiResponse<?> view(@RequestParam(defaultValue = "0", required = false) Integer page,
                               @RequestParam(defaultValue = "10", required = false) Integer size,
                               @RequestParam(required = false) BigInteger category_id,
                               @RequestParam(required = false) String title) {
        return ApiResponse.builder()
                .code("200")
                .message("Danh sách tin tức")
                .data(newsService.viewNewsClientByFilter(page, size, category_id, title))
                .build();
    }

}

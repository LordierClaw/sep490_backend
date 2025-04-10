package vn.com.fpt.sep490_g28_summer2024_be.service.news;


import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsChangeStatusDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsUpdateDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;

public interface NewsService {

    NewsResponseDTO create(NewsDTO request, MultipartFile thumbnail) throws IOException;

    NewsResponseDTO update(NewsUpdateDTO request, BigInteger id, MultipartFile image) throws IOException;

    NewsResponseDTO update(NewsChangeStatusDTO request);

    NewsResponseDTO viewDetail(BigInteger id);

    PageResponse<?> viewByFilter(Integer page, Integer size, BigInteger category_id,
                                 String title, BigInteger authorId, Integer status,
                                 LocalDate startDate, LocalDate endDate);

    PageResponse<?> viewNewsByAccount(Integer page, Integer size,String email, BigInteger category_id,
                                      String title,Integer status,
                                      LocalDate startDate, LocalDate endDate);

    PageResponse<?> viewNewsClientByFilter(Integer page, Integer size, BigInteger category_id,
                                           String title);

}

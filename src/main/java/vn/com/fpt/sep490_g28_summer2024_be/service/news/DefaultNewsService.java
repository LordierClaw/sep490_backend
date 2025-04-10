package vn.com.fpt.sep490_g28_summer2024_be.service.news;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.category.CategoryResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsChangeStatusDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.news.NewsUpdateDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Category;
import vn.com.fpt.sep490_g28_summer2024_be.entity.News;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseService;
import vn.com.fpt.sep490_g28_summer2024_be.mapper.Mapper;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.NewsRepository;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.utils.SlugUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultNewsService implements NewsService {
    private final NewsRepository newsRepository;
    private final AccountRepository accountRepository;
    private final FirebaseService firebaseService;
    private final SlugUtils slugUtils;

    @Override
    public NewsResponseDTO create(NewsDTO request, MultipartFile image) throws IOException {
        if (newsRepository.findByTitle(request.getTitle()).isPresent()) {
            throw new AppException(ErrorCode.DUPLICATE_TITLE);
        }

        CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername()).orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

        News refNews = Mapper.mapDtoToEntity(request, News.class);

        Category category = Mapper.mapDtoToEntity(request.getCategory(), Category.class);
        refNews.setCategory(category);

        refNews.setCategory(category);
        refNews.setStatus("admin".equalsIgnoreCase(customAccountDetails.getUserResponse().getScope()) ? 2 : 1);
        refNews.setCreatedAt(LocalDateTime.now());
        refNews.setCreatedBy(loggedAccount);
        refNews.setUpdatedAt(LocalDateTime.now());
        refNews.setUpdatedBy(loggedAccount);

        News savedDraffNews = newsRepository.save(refNews);
        savedDraffNews.setSlug(slugUtils.genSlug(String.format("%s %s", savedDraffNews.getNewsId(), savedDraffNews.getTitle())));

//        file
        if (image != null) {
            if (!image.getContentType().startsWith("image/")) throw new AppException(ErrorCode.HTTP_FILE_IS_NOT_IMAGE);
            String thumbnail = firebaseService.uploadOneFile(image, savedDraffNews.getNewsId(), "news/thumbnail");
            savedDraffNews.setThumbnail(thumbnail);
        }

        News savedNews = newsRepository.save(savedDraffNews);
        return NewsResponseDTO.builder()
                .newsId(savedNews.getNewsId())
                .title(savedNews.getTitle())
                .slug(savedNews.getSlug())
                .thumbnail(savedNews.getThumbnail())
                .build();
    }

    @Override
    public NewsResponseDTO update(NewsUpdateDTO request, BigInteger id, MultipartFile image) {
        CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername()).orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

        if (!newsRepository.findById(id).orElseThrow().getTitle().equals(request.getTitle())
                && newsRepository.findByTitle(request.getTitle()).isPresent()) {
            throw new AppException(ErrorCode.DUPLICATE_TITLE);
        }

        News refNews = newsRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.HTTP_NEWS_NOT_EXISTED));

        if (!loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin") &&
                !refNews.getCreatedBy().getEmail().equalsIgnoreCase(loggedAccount.getEmail())){
            throw new  AppException(ErrorCode.ACCESS_DENIED);
        }

        Category category = Mapper.mapDtoToEntity(request.getCategoryDTO(), Category.class);
        refNews.setCategory(category);
        refNews.setTitle(request.getTitle());
        refNews.setShort_description(request.getShort_description());
        refNews.setContent(request.getContent());
        refNews.setSlug(slugUtils.genSlug(String.format("%s %s",refNews.getNewsId(), request.getTitle())));



        //update thumbnail
        if (image != null) {

            //delete file if have thumbmnail
            if (refNews.getThumbnail() != null && !refNews.getThumbnail().isEmpty()) {
                try {
                    firebaseService.deleteFileByPath(refNews.getThumbnail());
                    refNews.setThumbnail(null);
                } catch (IOException e) {
                    throw new AppException(ErrorCode.DELETE_FILE_FAILED);
                }
            }

            try {
                if (!image.getContentType().startsWith("image/"))
                    throw new AppException(ErrorCode.HTTP_FILE_IS_NOT_IMAGE);
                String fileName = firebaseService.uploadOneFile(image, id, "news/thumbnail");
                refNews.setThumbnail(fileName);
            } catch (IOException e) {
                throw new AppException(ErrorCode.UPLOAD_FAILED);
            }
        }

        //update updatedAt and updatedBy
        refNews.setUpdatedAt(LocalDateTime.now());
        refNews.setUpdatedBy(loggedAccount);

        News savedNews = newsRepository.save(refNews);
        return NewsResponseDTO.builder()
                .newsId(savedNews.getNewsId())
                .title(savedNews.getTitle())
                .build();
    }

    @Override
    @Transactional
    public NewsResponseDTO update(NewsChangeStatusDTO request) {
        News refNews = newsRepository.findById(request.getNewsId()).orElseThrow(() -> new AppException(ErrorCode.HTTP_NEWS_NOT_EXISTED));

        if (!refNews.getCategory().getIsActive()){
            throw new AppException(ErrorCode.CATEGORY_OF_NEWS_MUST_BE_ACTIVE);
        }

        CustomAccountDetails customAccountDetails = (CustomAccountDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (customAccountDetails == null) {
            throw new AppException(ErrorCode.HTTP_UNAUTHORIZED);
        }
        Account loggedAccount = accountRepository.findByEmail(customAccountDetails.getUsername()).orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));

        if (!loggedAccount.getRole().getRoleName().equalsIgnoreCase("admin") &&
                !refNews.getCreatedBy().getEmail().equalsIgnoreCase(loggedAccount.getEmail())){
            throw new  AppException(ErrorCode.ACCESS_DENIED);
        }

        refNews.setStatus(request.getStatus());
        refNews.setUpdatedBy(loggedAccount);
        refNews.setUpdatedAt(LocalDateTime.now());
        System.out.println(refNews.getUpdatedAt());

        News savedNews = newsRepository.save(refNews);
        System.out.println(savedNews.getUpdatedAt());
        return NewsResponseDTO.builder()
                .newsId(savedNews.getNewsId())
                .title(savedNews.getTitle())
                .build();
    }


    @Override
    public NewsResponseDTO viewDetail(BigInteger id) {
        News refNews = newsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HTTP_NEWS_NOT_EXISTED));

        return NewsResponseDTO.builder()
                .newsId(refNews.getNewsId())
                .title(refNews.getTitle())
                .slug(refNews.getSlug())
                .thumbnail(refNews.getThumbnail() != null ? refNews.getThumbnail() : "")
                .short_description(refNews.getShort_description())
                .content(refNews.getContent())
                .status(refNews.getStatus())
                .createdBy(refNews.getCreatedBy() != null ? AccountDTO.builder()
                        .accountId(refNews.getCreatedBy().getAccountId())
                        .fullname(refNews.getCreatedBy().getFullname())
                        .email(refNews.getCreatedBy().getEmail())
                        .build() : null)
                .createdAt(refNews.getCreatedAt())
                .updatedBy(refNews.getUpdatedBy() != null ? AccountDTO.builder()
                        .accountId(refNews.getUpdatedBy().getAccountId())
                        .fullname(refNews.getCreatedBy().getFullname())
                        .email(refNews.getUpdatedBy().getEmail())
                        .build() : null)
                .updatedAt(refNews.getUpdatedAt())
                .category(CategoryResponseDTO.builder()
                        .categoryId(refNews.getCategory().getCategoryId())
                        .title(refNews.getCategory().getTitle())
                        .build())
                .build();
    }


    @Override
    public PageResponse<?> viewByFilter(Integer page, Integer size, BigInteger category_id,
                                        String title, BigInteger authorId, Integer status,
                                        LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(page, size);
        Page<News> listedNews = newsRepository.findNewsByFilters(category_id, title, authorId, status,
                startDate, endDate, pageable);
        List<NewsResponseDTO> newsResponseDTOList = listedNews.map(news -> NewsResponseDTO.builder()
                .newsId(news.getNewsId())
                .title(news.getTitle())
                .category(CategoryResponseDTO.builder()
                        .title(news.getCategory().getTitle())
                        .build())
                .status(news.getStatus())
                .createdAt(news.getCreatedAt())
                .createdBy(AccountDTO.builder()
                        .fullname(news.getCreatedBy().getFullname())
                        .email(news.getCreatedBy().getEmail())
                        .code(news.getCreatedBy().getCode())
                        .build())
                .updatedAt(news.getUpdatedAt())
                .updatedBy(AccountDTO.builder()
                        .fullname(news.getUpdatedBy().getFullname())
                        .build())
                .build()).toList();

        return PageResponse.<NewsResponseDTO>builder()
                .limit(size)
                .offset(page)
                .total((int) listedNews.getTotalElements())
                .content(newsResponseDTOList)
                .build();
    }

    @Override
    public PageResponse<?> viewNewsByAccount(Integer page, Integer size, String email, BigInteger category_id,
                                             String title, Integer status,
                                             LocalDate startDate, LocalDate endDate) {

        Pageable pageable = PageRequest.of(page, size);


        Account loggedAccount = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.HTTP_UNAUTHORIZED));


        Page<News> listedNews = newsRepository.findNewsByAccount(loggedAccount.getAccountId(), category_id, title, status,
                startDate, endDate, pageable);

        List<NewsResponseDTO> newsResponseDTOList = listedNews.stream().map(news -> NewsResponseDTO.builder()
                .newsId(news.getNewsId())
                .title(news.getTitle())
                .category(CategoryResponseDTO.builder()
                        .title(news.getCategory().getTitle())
                        .build())
                .status(news.getStatus())
                .createdAt(news.getCreatedAt())
                .createdBy(news.getCreatedBy() == null ? null : AccountDTO.builder()
                        .fullname(news.getCreatedBy().getFullname())
                        .email(news.getCreatedBy().getEmail())
                        .code(news.getCreatedBy().getCode())
                        .build())
                .updatedAt(news.getUpdatedAt())
                .updatedBy(news.getUpdatedBy() == null ? null : AccountDTO.builder()
                        .fullname(news.getUpdatedBy().getFullname())
                        .email(news.getUpdatedBy().getEmail())
                        .code(news.getUpdatedBy().getCode())
                        .build())
                .build()).toList();

        PageResponse<NewsResponseDTO> pageResponse = PageResponse.<NewsResponseDTO>builder()
                .limit(size)
                .offset(page)
                .total((int) listedNews.getTotalElements()).build();
        pageResponse.setContent(newsResponseDTOList);
        return pageResponse;
    }


    @Override
    public PageResponse<?> viewNewsClientByFilter(Integer page, Integer size, BigInteger category_id,
                                                  String title) {
        Pageable pageable = PageRequest.of(page, size);
        Page<News> listedNews = newsRepository.findNewsByTitleAndCategories(category_id, title, pageable);
        List<NewsResponseDTO> newsResponseDTOList = listedNews.stream().map(news -> NewsResponseDTO.builder()
                .newsId(news.getNewsId())
                .title(news.getTitle())
                .slug(news.getSlug())
                .short_description(news.getShort_description())
                .category(CategoryResponseDTO.builder()
                        .title(news.getCategory().getTitle())
                        .build())
                .thumbnail(news.getThumbnail() != null ? news.getThumbnail() : "")
                .createdAt(news.getCreatedAt())
                .build()
        ).toList();

        PageResponse<NewsResponseDTO> pageResponse = PageResponse.<NewsResponseDTO>builder()
                .limit(size)
                .offset(page)
                .total((int) listedNews.getTotalElements()).build();

        pageResponse.setContent(newsResponseDTOList);
        return pageResponse;
    }

}

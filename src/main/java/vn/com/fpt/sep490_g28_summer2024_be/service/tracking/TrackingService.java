package vn.com.fpt.sep490_g28_summer2024_be.service.tracking;

import org.springframework.web.multipart.MultipartFile;

import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.tracking.GroupedTrackingImageDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.tracking.TrackingDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.tracking.TrackingImageDTO;


import java.math.BigInteger;
import java.util.List;

public interface TrackingService {
    TrackingDTO addTracking(TrackingDTO trackingDTO, MultipartFile[] newImage);
    TrackingDTO getTrackingById(BigInteger id);
    TrackingDTO updateTracking(TrackingDTO trackingDTO, BigInteger id, MultipartFile[] newImages);
    PageResponse<TrackingDTO> viewByFilter(Integer page, Integer size, String title, BigInteger projectId);
    void deleteTracking(BigInteger id);
    List<GroupedTrackingImageDTO> getImagesByProjectIdAndTitles(BigInteger projectId);
}

package vn.com.fpt.sep490_g28_summer2024_be.service.sponsor;


import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.sponsor.SponsorRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.sponsor.SponsorResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.sponsor.SponsorUpdateRequestDTO;

import java.math.BigInteger;

public interface SponsorService {

    SponsorResponseDTO addSponsorToProject(SponsorRequestDTO sponsorRequestDTO, BigInteger projectId, MultipartFile contract, MultipartFile logo);

    SponsorResponseDTO viewDetail(BigInteger sponsorId);

    PageResponse<?> viewListSponsorInProject(Integer page, Integer size, String companyName, BigInteger projectId);

    SponsorResponseDTO update(BigInteger sponsorId, SponsorUpdateRequestDTO requestDTO, MultipartFile file, MultipartFile logo);

}

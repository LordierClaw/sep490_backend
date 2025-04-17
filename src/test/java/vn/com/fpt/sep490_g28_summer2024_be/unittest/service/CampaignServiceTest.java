package vn.com.fpt.sep490_g28_summer2024_be.unittest.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.campaign.CampaignStatisticsResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectByStatusAndCampaignResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Campaign;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseServiceImpl;
import vn.com.fpt.sep490_g28_summer2024_be.repository.CampaignRepository;
import vn.com.fpt.sep490_g28_summer2024_be.repository.ProjectRepository;
import vn.com.fpt.sep490_g28_summer2024_be.service.campaign.CampaignService;
import vn.com.fpt.sep490_g28_summer2024_be.utils.SlugUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@MockBean(FirebaseServiceImpl.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CampaignServiceTest {
    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private FirebaseServiceImpl firebaseService;

    @Autowired
    private SlugUtils slugUtils;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Clear the campaign table before each test
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE campaign").executeUpdate();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    @Test
    @DisplayName("CS2_viewByFilter_01")
    void viewByFilter_shouldReturnFilteredResults() {
        // Arrange
        // Create first campaign (will have smaller ID)
        Campaign campaign1 = Campaign.builder()
                .title("Save Forest Campaign")
                .description("Description 1")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(campaign1);

        // Create second campaign (will have larger ID)
        Campaign campaign2 = Campaign.builder()
                .title("Save Forest Initiative")
                .description("Description 2")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(campaign2);

        // Create an inactive campaign that shouldn't be returned
        Campaign campaign3 = Campaign.builder()
                .title("Save Forest Project")
                .description("Description 3")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(false)
                .build();
        campaignRepository.save(campaign3);

        // Act
        PageResponse<?> response = campaignService.viewByFilter(
                0,              // page
                5,              // size
                "Save Forest",  // title
                true           // isActive
        );

        // Assert
        assertNotNull(response, "Response should not be null");
        List<CampaignResponseDTO> campaigns = (List<CampaignResponseDTO>) response.getContent();
        
        // Verify pagination parameters
        assertEquals(0, response.getOffset(), "Page offset should be 0");
        assertEquals(5, response.getLimit(), "Page size should be 5");
        assertEquals(2, response.getTotal(), "Total number of campaigns should be 2");
        
        // Verify content
        assertEquals(2, campaigns.size(), "Should return exactly 2 campaigns");
        
        // Verify each campaign in the result
        assertTrue(campaigns.stream()
                .allMatch(c -> c.getTitle().contains("Save Forest")),
                "All campaigns should contain 'Save Forest' in title");
        assertTrue(campaigns.stream()
                .allMatch(CampaignResponseDTO::getIsActive),
                "All campaigns should be active");

        // Verify first campaign details (will be campaign2 due to DESC ordering)
        CampaignResponseDTO firstCampaign = campaigns.getFirst();
        assertNotNull(firstCampaign.getCampaignId(), "Campaign ID should not be null");
        assertEquals(campaign2.getTitle(), firstCampaign.getTitle(), "First campaign title should match campaign2");
        assertEquals(campaign2.getDescription(), firstCampaign.getDescription(), "First campaign description should match campaign2");
        assertEquals(campaign2.getIsActive(), firstCampaign.getIsActive(), "First campaign active status should match campaign2");
        
        // Verify second campaign details (will be campaign1 due to DESC ordering)
        CampaignResponseDTO secondCampaign = campaigns.get(1);
        assertNotNull(secondCampaign.getCampaignId(), "Campaign ID should not be null");
        assertEquals(campaign1.getTitle(), secondCampaign.getTitle(), "Second campaign title should match campaign1");
        assertEquals(campaign1.getDescription(), secondCampaign.getDescription(), "Second campaign description should match campaign1");
        assertEquals(campaign1.getIsActive(), secondCampaign.getIsActive(), "Second campaign active status should match campaign1");
    }

    @Test
    @DisplayName("CS2_getCampaignById_01")
    void getCampaignById_shouldReturnCampaign_whenCampaignExists() {
        // Arrange
        Campaign campaign = Campaign.builder()
                .title("Test Campaign")
                .description("Test Description")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        Campaign savedCampaign = campaignRepository.save(campaign);

        // Act
        CampaignResponseDTO response = campaignService.getCampaignById(savedCampaign.getCampaignId());

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(savedCampaign.getCampaignId(), response.getCampaignId(), "Campaign ID should match");
        assertEquals(savedCampaign.getTitle(), response.getTitle(), "Title should match");
        assertEquals(savedCampaign.getDescription(), response.getDescription(), "Description should match");
        assertEquals(savedCampaign.getCreatedAt(), response.getCreatedAt(), "Created date should match");
        assertEquals(savedCampaign.getUpdatedAt(), response.getUpdatedAt(), "Updated date should match");
        assertEquals(savedCampaign.getIsActive(), response.getIsActive(), "Active status should match");
    }

    @Test
    @DisplayName("CS2_getCampaignById_02")
    void getCampaignById_shouldThrowException_whenCampaignNotFound() {
        // Arrange
        BigInteger nonExistentId = BigInteger.valueOf(1);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                campaignService.getCampaignById(nonExistentId),
                "Should throw AppException when campaign not found"
        );
        
        assertEquals(ErrorCode.CAMPAIGN_NO_CONTENT, exception.getErrorCode(),
                "Should throw CAMPAIGN_NO_CONTENT error code");
    }

    @Test
    @DisplayName("CS2_getAllCampaignsIdAndName_01")
    void getAllCampaignsIdAndName_shouldReturnAllCampaigns() {
        // Arrange
        Campaign campaign1 = Campaign.builder()
                .title("Campaign 1")
                .description("Description 1")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(campaign1);
        campaign1.setSlug(slugUtils.genSlug(String.format("%s %s", campaign1.getCampaignId(), campaign1.getTitle())));
        campaignRepository.save(campaign1);

        Campaign campaign2 = Campaign.builder()
                .title("Campaign 2")
                .description("Description 2")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(campaign2);
        campaign2.setSlug(slugUtils.genSlug(String.format("%s %s", campaign2.getCampaignId(), campaign2.getTitle())));
        campaignRepository.save(campaign2);

        // Act
        List<CampaignResponseDTO> result = campaignService.getAllCampaignsIdAndName();

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 campaigns");

        // Verify first campaign
        CampaignResponseDTO firstCampaign = result.getFirst();
        assertEquals(campaign1.getCampaignId(), firstCampaign.getCampaignId(), "First campaign ID should match");
        assertEquals(campaign1.getTitle(), firstCampaign.getTitle(), "First campaign title should match");
        assertEquals(campaign1.getSlug(), firstCampaign.getSlug(), "First campaign slug should match");
        assertNull(firstCampaign.getDescription(), "Description should be null");
        assertNull(firstCampaign.getThumbnail(), "Thumbnail should be null");
        assertNull(firstCampaign.getCreatedAt(), "CreatedAt should be null");
        assertNull(firstCampaign.getUpdatedAt(), "UpdatedAt should be null");
        assertNull(firstCampaign.getIsActive(), "IsActive should be null");

        // Verify second campaign
        CampaignResponseDTO secondCampaign = result.get(1);
        assertEquals(campaign2.getCampaignId(), secondCampaign.getCampaignId(), "Second campaign ID should match");
        assertEquals(campaign2.getTitle(), secondCampaign.getTitle(), "Second campaign title should match");
        assertEquals(campaign2.getSlug(), secondCampaign.getSlug(), "Second campaign slug should match");
        assertNull(secondCampaign.getDescription(), "Description should be null");
        assertNull(secondCampaign.getThumbnail(), "Thumbnail should be null");
        assertNull(secondCampaign.getCreatedAt(), "CreatedAt should be null");
        assertNull(secondCampaign.getUpdatedAt(), "UpdatedAt should be null");
        assertNull(secondCampaign.getIsActive(), "IsActive should be null");
    }

    @Test
    @DisplayName("CS2_addCampaign_01")
    void addCampaign_shouldThrowException_whenTitleExists() {
        // Arrange
        // Create an existing campaign
        Campaign existingCampaign = Campaign.builder()
                .title("Health")
                .description("Existing campaign")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(existingCampaign);

        CampaignRequestDTO campaignDTO = new CampaignRequestDTO();
        campaignDTO.setTitle("Health");
        campaignDTO.setDescription("New campaign with same title");

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                campaignService.addCampaign(campaignDTO, null),
                "Should throw AppException when title already exists"
        );
        assertEquals(ErrorCode.DUPLICATE_TITLE, exception.getErrorCode(),
                "Should throw DUPLICATE_TITLE error code");

        // Verify no new campaign was added
        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM campaign").getSingleResult();
        assertEquals(1, count.intValue(), "Should still have only one campaign");
    }

    @Test
    @DisplayName("CS2_addCampaign_02")
    void addCampaign_shouldThrowException_whenImageTypeInvalid() {
        // Arrange
        CampaignRequestDTO campaignDTO = new CampaignRequestDTO();
        campaignDTO.setTitle("Health");
        campaignDTO.setDescription("Test campaign");

        MockMultipartFile invalidImage = new MockMultipartFile(
                "image",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                campaignService.addCampaign(campaignDTO, invalidImage),
                "Should throw AppException when image type is invalid"
        );
        assertEquals(ErrorCode.HTTP_FILE_IS_NOT_IMAGE, exception.getErrorCode(),
                "Should throw HTTP_FILE_IS_NOT_IMAGE error code");

        // Verify no campaign was added
        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM campaign").getSingleResult();
        assertEquals(0, count.intValue(), "Should have no campaigns");
    }

    @Test
    @DisplayName("CS2_addCampaign_03")
    void addCampaign_shouldThrowException_whenImageSizeExceedsLimit() {
        // Arrange
        CampaignRequestDTO campaignDTO = new CampaignRequestDTO();
        campaignDTO.setTitle("Health");
        campaignDTO.setDescription("Test campaign");

        // Create a 3MB image (exceeds 2MB limit)
        byte[] largeContent = new byte[3 * 1024 * 1024];
        MockMultipartFile largeImage = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                largeContent
        );

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                campaignService.addCampaign(campaignDTO, largeImage),
                "Should throw AppException when image size exceeds limit"
        );
        assertEquals(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT, exception.getErrorCode(),
                "Should throw FILE_SIZE_EXCEEDS_LIMIT error code");

        // Verify no campaign was added
        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM campaign").getSingleResult();
        assertEquals(0, count.intValue(), "Should have no campaigns");
    }

    @Test
    @DisplayName("CS2_addCampaign_04")
    void addCampaign_shouldThrowException_whenUploadFails() throws IOException {
        // Arrange
        CampaignRequestDTO campaignDTO = new CampaignRequestDTO();
        campaignDTO.setTitle("Health");
        campaignDTO.setDescription("Test campaign");

        MockMultipartFile validImage = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // Mock Firebase service to throw IOException
        when(firebaseService.uploadOneFile(any(), any(), any()))
                .thenThrow(new IOException("Upload failed"));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                campaignService.addCampaign(campaignDTO, validImage),
                "Should throw AppException when upload fails"
        );
        assertEquals(ErrorCode.UPLOAD_FAILED, exception.getErrorCode(),
                "Should throw UPLOAD_FAILED error code");

        // Verify campaign was not added
        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM campaign").getSingleResult();
        assertEquals(0, count.intValue(), "Should have no campaigns");
    }

    @Test
    @DisplayName("CS2_addCampaign_05")
    void addCampaign_shouldSucceed_whenValidData() throws IOException {
        // Arrange
        CampaignRequestDTO campaignDTO = new CampaignRequestDTO();
        campaignDTO.setTitle("Health");
        campaignDTO.setDescription("Test campaign");

        // Mock Firebase service to return a URL
        when(firebaseService.uploadOneFile(any(), any(), any()))
                .thenReturn("test-thumbnail-url");

        // Act
        CampaignResponseDTO response = campaignService.addCampaign(campaignDTO, null);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(campaignDTO.getTitle(), response.getTitle(), "Title should match");
        assertNotNull(response.getSlug(), "Slug should be generated");
        
        // Verify campaign was added to database
        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM campaign").getSingleResult();
        assertEquals(1, count.intValue(), "Should have one campaign");
        Campaign savedCampaign = campaignRepository.findById(response.getCampaignId()).orElse(null);
        assertNotNull(savedCampaign, "Campaign should exist in database");
        assertEquals(campaignDTO.getTitle(), savedCampaign.getTitle(), "Saved title should match");
        assertEquals(campaignDTO.getDescription(), savedCampaign.getDescription(), "Saved description should match");
        assertTrue(savedCampaign.getIsActive(), "Campaign should be active");
        assertNotNull(savedCampaign.getCreatedAt(), "Created date should be set");
        assertNotNull(savedCampaign.getUpdatedAt(), "Updated date should be set");
    }

    @Test
    @DisplayName("CS2_updateCampaign_01")
    void updateCampaign_shouldThrowException_whenCampaignNotExist() {
        // Arrange
        CampaignRequestDTO campaignDTO = new CampaignRequestDTO();
        campaignDTO.setTitle("Summer Donation");
        BigInteger nonExistentId = BigInteger.valueOf(1);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                campaignService.updateCampaign(campaignDTO, nonExistentId, null),
                "Should throw AppException when campaign does not exist"
        );
        assertEquals(ErrorCode.CAMPAIGN_NOT_EXISTED, exception.getErrorCode(),
                "Should throw CAMPAIGN_NOT_EXISTED error code");

        // Verify no campaign was added
        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM campaign").getSingleResult();
        assertEquals(0, count.intValue(), "Should have no campaigns");
    }

    @Test
    @DisplayName("CS2_updateCampaign_02")
    void updateCampaign_shouldThrowException_whenTitleExists() {
        // Arrange
        // Create first campaign
        Campaign existingCampaign = Campaign.builder()
                .title("Existing Campaign")
                .description("Description")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(existingCampaign);

        // Create second campaign to update
        Campaign campaignToUpdate = Campaign.builder()
                .title("Summer Donation")
                .description("Description")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(campaignToUpdate);

        // Try to update second campaign with first campaign's title
        CampaignRequestDTO updateDTO = new CampaignRequestDTO();
        updateDTO.setTitle("Existing Campaign");
        updateDTO.setDescription("New description");

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                campaignService.updateCampaign(updateDTO, campaignToUpdate.getCampaignId(), null),
                "Should throw AppException when title already exists"
        );
        assertEquals(ErrorCode.DUPLICATE_TITLE, exception.getErrorCode(),
                "Should throw DUPLICATE_TITLE error code");

        // Verify campaign was not updated
        Campaign notUpdatedCampaign = campaignRepository.findById(campaignToUpdate.getCampaignId()).orElse(null);
        assertNotNull(notUpdatedCampaign, "Campaign should still exist");
        assertEquals("Summer Donation", notUpdatedCampaign.getTitle(), "Title should not be updated");
    }

    @Test
    @DisplayName("CS2_updateCampaign_03")
    void updateCampaign_shouldDeleteThumbnail_whenThumbnailIsNull() throws IOException {
        // Arrange
        Campaign campaign = Campaign.builder()
                .title("Summer Donation")
                .description("Description")
                .thumbnail("existing-thumbnail.jpg")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(campaign);

        CampaignRequestDTO updateDTO = new CampaignRequestDTO();
        updateDTO.setTitle("Summer Donation");
        updateDTO.setDescription("Updated description");
        updateDTO.setThumbnail(null);

        // Mock Firebase service
        when(firebaseService.deleteFileByPath(anyString())).thenReturn(true);

        // Act
        CampaignResponseDTO response = campaignService.updateCampaign(updateDTO, campaign.getCampaignId(), null);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNull(response.getThumbnail(), "Thumbnail should be null");
        
        // Verify thumbnail was deleted
        verify(firebaseService).deleteFileByPath("existing-thumbnail.jpg");
        
        // Verify campaign was updated in database
        Campaign updatedCampaign = campaignRepository.findById(campaign.getCampaignId()).orElse(null);
        assertNotNull(updatedCampaign, "Campaign should exist");
        assertNull(updatedCampaign.getThumbnail(), "Thumbnail should be null in database");
    }

    @Test
    @DisplayName("CS2_updateCampaign_04")
    void updateCampaign_shouldThrowException_whenImageTypeInvalid() {
        // Arrange
        Campaign campaign = Campaign.builder()
                .title("Summer Donation")
                .description("Description")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(campaign);

        CampaignRequestDTO updateDTO = new CampaignRequestDTO();
        updateDTO.setTitle("Summer Donation");
        updateDTO.setDescription("Updated description");

        MockMultipartFile invalidImage = new MockMultipartFile(
                "image",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                campaignService.updateCampaign(updateDTO, campaign.getCampaignId(), invalidImage),
                "Should throw AppException when image type is invalid"
        );
        assertEquals(ErrorCode.HTTP_FILE_IS_NOT_IMAGE, exception.getErrorCode(),
                "Should throw HTTP_FILE_IS_NOT_IMAGE error code");

        // Verify campaign was not updated
        Campaign notUpdatedCampaign = campaignRepository.findById(campaign.getCampaignId()).orElse(null);
        assertNotNull(notUpdatedCampaign, "Campaign should still exist");
        assertEquals("Description", notUpdatedCampaign.getDescription(), "Description should not be updated");
    }

    @Test
    @DisplayName("CS2_updateCampaign_05")
    void updateCampaign_shouldThrowException_whenImageSizeExceedsLimit() {
        // Arrange
        Campaign campaign = Campaign.builder()
                .title("Summer Donation")
                .description("Description")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(campaign);

        CampaignRequestDTO updateDTO = new CampaignRequestDTO();
        updateDTO.setTitle("Summer Donation");
        updateDTO.setDescription("Updated description");

        byte[] largeContent = new byte[3 * 1024 * 1024];
        MockMultipartFile largeImage = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                largeContent
        );

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                campaignService.updateCampaign(updateDTO, campaign.getCampaignId(), largeImage),
                "Should throw AppException when image size exceeds limit"
        );
        assertEquals(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT, exception.getErrorCode(),
                "Should throw FILE_SIZE_EXCEEDS_LIMIT error code");

        // Verify campaign was not updated
        Campaign notUpdatedCampaign = campaignRepository.findById(campaign.getCampaignId()).orElse(null);
        assertNotNull(notUpdatedCampaign, "Campaign should still exist");
        assertEquals("Description", notUpdatedCampaign.getDescription(), "Description should not be updated");
    }

    @Test
    @DisplayName("CS2_updateCampaign_06")
    void updateCampaign_shouldThrowException_whenUploadFails() throws IOException {
        // Arrange
        Campaign campaign = Campaign.builder()
                .title("Summer Donation")
                .description("Description")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(campaign);

        CampaignRequestDTO updateDTO = new CampaignRequestDTO();
        updateDTO.setTitle("Summer Donation");
        updateDTO.setDescription("Updated description");

        MockMultipartFile validImage = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // Mock Firebase service to throw IOException
        when(firebaseService.uploadOneFile(any(), any(), any()))
                .thenThrow(new IOException("Upload failed"));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                campaignService.updateCampaign(updateDTO, campaign.getCampaignId(), validImage),
                "Should throw AppException when upload fails"
        );
        assertEquals(ErrorCode.UPLOAD_FAILED, exception.getErrorCode(),
                "Should throw UPLOAD_FAILED error code");

        // Verify campaign was not updated
        Campaign notUpdatedCampaign = campaignRepository.findById(campaign.getCampaignId()).orElse(null);
        assertNotNull(notUpdatedCampaign, "Campaign should still exist");
        assertEquals("Description", notUpdatedCampaign.getDescription(), "Description should not be updated");
    }

    @Test
    @DisplayName("CS2_updateCampaign_07")
    void updateCampaign_shouldSucceed_whenValidData() throws IOException {
        // Arrange
        Campaign campaign = Campaign.builder()
                .title("Summer Donation")
                .description("Original description")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(campaign);

        CampaignRequestDTO updateDTO = new CampaignRequestDTO();
        updateDTO.setTitle("Summer Donation Updated");
        updateDTO.setDescription("Updated description");

        MockMultipartFile newImage = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // Mock Firebase service
        when(firebaseService.uploadOneFile(any(), any(), any()))
                .thenReturn("new-thumbnail-url");

        // Act
        CampaignResponseDTO response = campaignService.updateCampaign(updateDTO, campaign.getCampaignId(), newImage);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(updateDTO.getTitle(), response.getTitle(), "Title should be updated");
        assertEquals(updateDTO.getDescription(), response.getDescription(), "Description should be updated");
        assertEquals("new-thumbnail-url", response.getThumbnail(), "Thumbnail should be updated");

        // Verify campaign was updated in database
        Campaign updatedCampaign = campaignRepository.findById(campaign.getCampaignId()).orElse(null);
        assertNotNull(updatedCampaign, "Campaign should exist");
        assertEquals(updateDTO.getTitle(), updatedCampaign.getTitle(), "Title should be updated in database");
        assertEquals(updateDTO.getDescription(), updatedCampaign.getDescription(), "Description should be updated in database");
        assertEquals("new-thumbnail-url", updatedCampaign.getThumbnail(), "Thumbnail should be updated in database");
        assertNotNull(updatedCampaign.getUpdatedAt(), "Updated date should be set");
    }


    @Test
    @DisplayName("CS2_getAllCampaigns_01")
    void getAllCampaigns_shouldReturnAllCampaigns_whenCampaignsExist() {
        // Arrange
        Campaign campaign1 = Campaign.builder()
                .title("Campaign 1")
                .description("Description 1")
                .thumbnail("thumbnail1.jpg")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(campaign1);
        campaign1.setSlug(slugUtils.genSlug(String.format("%s %s", campaign1.getCampaignId(), campaign1.getTitle())));
        campaignRepository.save(campaign1);

        Campaign campaign2 = Campaign.builder()
                .title("Campaign 2")
                .description("Description 2")
                .thumbnail("thumbnail2.jpg")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(campaign2);
        campaign2.setSlug(slugUtils.genSlug(String.format("%s %s", campaign2.getCampaignId(), campaign2.getTitle())));
        campaignRepository.save(campaign2);

        // Act
        List<CampaignResponseDTO> result = campaignService.getAllCampaigns();

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 campaigns");

        // Verify first campaign
        CampaignResponseDTO firstCampaign = result.getFirst();
        assertEquals(campaign1.getCampaignId(), firstCampaign.getCampaignId(), "First campaign ID should match");
        assertEquals(campaign1.getTitle(), firstCampaign.getTitle(), "First campaign title should match");
        assertEquals(campaign1.getSlug(), firstCampaign.getSlug(), "First campaign slug should match");
        assertEquals(campaign1.getDescription(), firstCampaign.getDescription(), "First campaign description should match");
        assertEquals(campaign1.getThumbnail(), firstCampaign.getThumbnail(), "First campaign thumbnail should match");

        // Verify second campaign
        CampaignResponseDTO secondCampaign = result.get(1);
        assertEquals(campaign2.getCampaignId(), secondCampaign.getCampaignId(), "Second campaign ID should match");
        assertEquals(campaign2.getTitle(), secondCampaign.getTitle(), "Second campaign title should match");
        assertEquals(campaign2.getSlug(), secondCampaign.getSlug(), "Second campaign slug should match");
        assertEquals(campaign2.getDescription(), secondCampaign.getDescription(), "Second campaign description should match");
        assertEquals(campaign2.getThumbnail(), secondCampaign.getThumbnail(), "Second campaign thumbnail should match");
    }

    @Test
    @DisplayName("CS2_getCampaignClientById_01")
    void getCampaignClientById_shouldThrowException_whenCampaignNotExist() {
        // Arrange
        BigInteger nonExistentId = BigInteger.valueOf(1);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                campaignService.getCampaignClientById(nonExistentId),
                "Should throw AppException when campaign does not exist"
        );
        assertEquals(ErrorCode.CAMPAIGN_NO_CONTENT, exception.getErrorCode(),
                "Should throw CAMPAIGN_NO_CONTENT error code");

        // Verify database is empty
        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM campaign").getSingleResult();
        assertEquals(0, count.intValue(), "Database should have no campaigns");
    }

    @Test
    @DisplayName("CS2_getCampaignClientById_02")
    void getCampaignClientById_shouldReturnCampaign_whenCampaignExists() {
        // Arrange
        Campaign campaign = Campaign.builder()
                .title("Test Campaign")
                .description("Test Description")
                .thumbnail("test-thumbnail.jpg")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(campaign);
        campaign.setSlug(slugUtils.genSlug(String.format("%s %s", campaign.getCampaignId(), campaign.getTitle())));
        campaignRepository.save(campaign);

        // Act
        CampaignResponseDTO response = campaignService.getCampaignClientById(campaign.getCampaignId());

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(campaign.getCampaignId(), response.getCampaignId(), "Campaign ID should match");
        assertEquals(campaign.getTitle(), response.getTitle(), "Title should match");
        assertEquals(campaign.getSlug(), response.getSlug(), "Slug should match");
        assertEquals(campaign.getDescription(), response.getDescription(), "Description should match");
        assertEquals(campaign.getThumbnail(), response.getThumbnail(), "Thumbnail should match");
        assertEquals(campaign.getCreatedAt(), response.getCreatedAt(), "Created date should match");
        assertEquals(campaign.getUpdatedAt(), response.getUpdatedAt(), "Updated date should match");
        assertEquals(campaign.getIsActive(), response.getIsActive(), "Active status should match");

        // Verify campaign exists in database
        Campaign savedCampaign = campaignRepository.findById(campaign.getCampaignId()).orElse(null);
        assertNotNull(savedCampaign, "Campaign should exist in database");
        assertEquals(campaign.getTitle(), savedCampaign.getTitle(), "Title should match in database");
        assertEquals(campaign.getDescription(), savedCampaign.getDescription(), "Description should match in database");
        assertEquals(campaign.getThumbnail(), savedCampaign.getThumbnail(), "Thumbnail should match in database");
        assertEquals(campaign.getCreatedAt(), savedCampaign.getCreatedAt(), "Created date should match in database");
        assertEquals(campaign.getUpdatedAt(), savedCampaign.getUpdatedAt(), "Updated date should match in database");
        assertEquals(campaign.getIsActive(), savedCampaign.getIsActive(), "Active status should match in database");
    }

    @Test
    @DisplayName("CS2_getCountProjectsGroupedByCampaignAndStatus_01")
    void getCountProjectsGroupedByCampaignAndStatus_shouldReturnCorrectStatistics() {
        // Arrange
        // Create test campaigns
        Campaign campaign1 = Campaign.builder()
                .title("Environment Campaign")
                .description("Description 1")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(campaign1);

        Campaign campaign2 = Campaign.builder()
                .title("Health Campaign")
                .description("Description 2")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();
        campaignRepository.save(campaign2);

        // Insert test projects with different statuses using native queries
        // For campaign1:
        // 2 projects with status 1
        // 1 project with status 2
        // 3 projects with status 3
        // 1 project with status 4
        entityManager.createNativeQuery("""
            INSERT INTO project (title, campaign_id, status)
            VALUES 
            ('Project 1', ?1, 1),
            ('Project 2', ?1, 1),
            ('Project 3', ?1, 2),
            ('Project 4', ?1, 3),
            ('Project 5', ?1, 3),
            ('Project 6', ?1, 3),
            ('Project 7', ?1, 4)
        """)
        .setParameter(1, campaign1.getCampaignId())
        .executeUpdate();

        // For campaign2:
        // 1 project with status 1
        // 2 projects with status 2
        // 1 project with status 3
        // 0 projects with status 4
        entityManager.createNativeQuery("""
            INSERT INTO project (title, campaign_id, status)
            VALUES 
            ('Project 8', ?1, 1),
            ('Project 9', ?1, 2),
            ('Project 10', ?1, 2),
            ('Project 11', ?1, 3)
        """)
        .setParameter(1, campaign2.getCampaignId())
        .executeUpdate();

        // Act
        CampaignStatisticsResponse response = campaignService.getCountProjectsGroupedByCampaignAndStatus();

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(11, response.getTotalProjects(), "Total projects should be 11");
        
        List<ProjectByStatusAndCampaignResponseDTO> statistics = response.getData();
        assertEquals(2, statistics.size(), "Should have statistics for 2 campaigns");

        // Verify Environment Campaign statistics
        ProjectByStatusAndCampaignResponseDTO envStats = statistics.stream()
                .filter(stat -> stat.getTitle().equals("Environment Campaign"))
                .findFirst()
                .orElseThrow();
        assertEquals(7, envStats.getCount(), "Environment Campaign should have 7 total projects");
        assertEquals(2, envStats.getStatus1(), "Environment Campaign should have 2 projects with status 1");
        assertEquals(1, envStats.getStatus2(), "Environment Campaign should have 1 project with status 2");
        assertEquals(3, envStats.getStatus3(), "Environment Campaign should have 3 projects with status 3");
        assertEquals(1, envStats.getStatus4(), "Environment Campaign should have 1 project with status 4");

        // Verify Health Campaign statistics
        ProjectByStatusAndCampaignResponseDTO healthStats = statistics.stream()
                .filter(stat -> stat.getTitle().equals("Health Campaign"))
                .findFirst()
                .orElseThrow();
        assertEquals(4, healthStats.getCount(), "Health Campaign should have 4 total projects");
        assertEquals(1, healthStats.getStatus1(), "Health Campaign should have 1 project with status 1");
        assertEquals(2, healthStats.getStatus2(), "Health Campaign should have 2 projects with status 2");
        assertEquals(1, healthStats.getStatus3(), "Health Campaign should have 1 project with status 3");
        assertEquals(0, healthStats.getStatus4(), "Health Campaign should have 0 projects with status 4");
    }
}

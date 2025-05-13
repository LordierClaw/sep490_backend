package vn.com.fpt.sep490_g28_summer2024_be;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.ChallengeRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.ChallengeResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Challenge;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Role;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Project;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Campaign;
import vn.com.fpt.sep490_g28_summer2024_be.entity.ChallengeProject;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseService;
import vn.com.fpt.sep490_g28_summer2024_be.firebase.FirebaseServiceImpl;
import vn.com.fpt.sep490_g28_summer2024_be.repository.*;
import vn.com.fpt.sep490_g28_summer2024_be.service.challenge.ChallengeService;
import vn.com.fpt.sep490_g28_summer2024_be.service.challenge.ChallengerServiceImpl;
import vn.com.fpt.sep490_g28_summer2024_be.utils.CodeUtils;
import vn.com.fpt.sep490_g28_summer2024_be.utils.SlugUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@MockBean(FirebaseServiceImpl.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@MockBean(ChallengeRepository.class)
public class ChallengeServiceTest {

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private FirebaseService firebaseService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private ChallengeProjectRepository challengeProjectRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CodeUtils codeUtils;

    @Autowired
    private SlugUtils slugUtils;

    private Account account;
    private Role adminRole;
    private Role userRole;
    private Campaign campaign;

    @BeforeEach
    void setup() {
        // Truncate tables
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE challenge_project").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE challenge").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE project").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE campaign").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE account").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE role").executeUpdate();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        
        // Insert admin role using native query
        entityManager.createNativeQuery(
            "INSERT INTO role (role_id, role_name, is_active) " +
            "SELECT 4, 'System Admin', true " +
            "WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_id = 4)"
        ).executeUpdate();
        
        // Refresh admin role from database
        adminRole = roleRepository.findById(BigInteger.valueOf(4))
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        // Create user role
        userRole = roleRepository.save(Role.builder()
                .roleName("User")
                .isActive(true)
                .build());

        // Create admin account
        account = accountRepository.save(Account.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullname("Test User")
                .phone("0123456789")
                .address("Test Address")
                .gender(1)
                .dob(LocalDate.of(2000, 1, 1))
                .isActive(true)
                .role(adminRole)
                .build());

        // Create test campaign
        campaign = campaignRepository.save(Campaign.builder()
                .title("Test Campaign")
                .isActive(true)
                .build());
    }

    private ChallengeRequestDTO createValidRequest(String title) {
        return ChallengeRequestDTO.builder()
                .title(title)
                .content("Challenge Content")
                .goal(BigDecimal.valueOf(100))
                .finishedAt(LocalDate.now().plusDays(10))
                .projects(null)
                .build();
    }

    private Project createProject(int status) {
        return projectRepository.save(Project.builder()
                .title("Test Project")
                .status(status)
                .campaign(campaign)
                .ward("Test Ward")
                .district("Test District")
                .province("Test Province")
                .build());
    }

    @Test
    @DisplayName("CS_addChallenge_01")
    void addChallenge_shouldFail_whenTitleAlreadyExists() {
        // Arrange: Tạo một challenge đã tồn tại với tiêu đề "Challenge A"
        String duplicatedTitle = "Challenge A";
        Challenge existingChallenge = Challenge.builder()
                .title(duplicatedTitle)
                .slug("challenge-a")
                .content("Some content")
                .goal(BigDecimal.valueOf(100))
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .build();
        challengeRepository.save(existingChallenge);

        // Act & Assert
        ChallengeRequestDTO request = createValidRequest(duplicatedTitle);
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.addChallenge(request, account.getEmail(), null)
        );
        assertEquals(ErrorCode.DUPLICATE_TITLE, exception.getErrorCode());

        // Verify only one challenge exists
        Long challengeCount = entityManager
                .createQuery("SELECT COUNT(c) FROM Challenge c WHERE c.title = :title", Long.class)
                .setParameter("title", duplicatedTitle)
                .getSingleResult();
        assertEquals(1L, challengeCount);
    }

    @Test
    @DisplayName("CS_addChallenge_02")
    void addChallenge_shouldFail_whenUserNotFound() {
        // Arrange
        ChallengeRequestDTO request = createValidRequest("New Challenge");
        String nonExistentEmail = "nonexistent@example.com";

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.addChallenge(request, nonExistentEmail, null)
        );
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, exception.getErrorCode());

        // Verify no challenge was created
        Long challengeCount = entityManager
                .createQuery("SELECT COUNT(c) FROM Challenge c WHERE c.title = :title", Long.class)
                .setParameter("title", "New Challenge")
                .getSingleResult();
        assertEquals(0L, challengeCount);
    }

    @Test
    @DisplayName("CS_addChallenge_03")
    void addChallenge_shouldFail_whenUserIsNotAdmin() {
        // Arrange
        Account nonAdminAccount = accountRepository.save(Account.builder()
                .email("user@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullname("Regular User")
                .phone("0123456789")
                .address("Test Address")
                .gender(1)
                .dob(LocalDate.of(2000, 1, 1))
                .isActive(true)
                .role(userRole)
                .build());

        ChallengeRequestDTO request = createValidRequest("New Challenge");

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.addChallenge(request, nonAdminAccount.getEmail(), null)
        );
        assertEquals(ErrorCode.ADMIN_ACCESS_DENIED, exception.getErrorCode());

        // Verify no challenge was created
        Long challengeCount = entityManager
                .createQuery("SELECT COUNT(c) FROM Challenge c WHERE c.title = :title", Long.class)
                .setParameter("title", "New Challenge")
                .getSingleResult();
        assertEquals(0L, challengeCount);
    }

    @Test
    @DisplayName("CS_addChallenge_04")
    void addChallenge_shouldFail_whenFinishDateIsInvalid() {
        // Arrange
        ChallengeRequestDTO request = createValidRequest("New Challenge");
        request.setFinishedAt(LocalDate.now().minusDays(1));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.addChallenge(request, account.getEmail(), null)
        );
        assertEquals(ErrorCode.INVALID_FINISH_DATE, exception.getErrorCode());

        // Verify no challenge was created
        Long challengeCount = entityManager
                .createQuery("SELECT COUNT(c) FROM Challenge c WHERE c.title = :title", Long.class)
                .setParameter("title", "New Challenge")
                .getSingleResult();
        assertEquals(0L, challengeCount);
    }

    @Test
    @DisplayName("CS_addChallenge_05")
    void addChallenge_shouldFail_whenImageContentTypeIsInvalid() {
        // Arrange
        ChallengeRequestDTO request = createValidRequest("New Challenge");
        MockMultipartFile invalidImage = new MockMultipartFile(
                "image",
                "test.txt",
                "text/plain",
                "This is not an image".getBytes()
        );

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.addChallenge(request, account.getEmail(), invalidImage)
        );
        assertEquals(ErrorCode.HTTP_FILE_IS_NOT_IMAGE, exception.getErrorCode());

        // Verify no challenge was created
        Long challengeCount = entityManager
                .createQuery("SELECT COUNT(c) FROM Challenge c WHERE c.title = :title", Long.class)
                .setParameter("title", "New Challenge")
                .getSingleResult();
        assertEquals(0L, challengeCount);
    }

    @Test
    @DisplayName("CS_addChallenge_06")
    void addChallenge_shouldFail_whenImageSizeExceedsLimit() {
        // Arrange
        ChallengeRequestDTO request = createValidRequest("New Challenge");
        byte[] largeImage = new byte[3 * 1024 * 1024];
        MockMultipartFile largeImageFile = new MockMultipartFile(
                "image",
                "large.jpg",
                "image/jpeg",
                largeImage
        );

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.addChallenge(request, account.getEmail(), largeImageFile)
        );
        assertEquals(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT, exception.getErrorCode());

        // Verify no challenge was created
        Long challengeCount = entityManager
                .createQuery("SELECT COUNT(c) FROM Challenge c WHERE c.title = :title", Long.class)
                .setParameter("title", "New Challenge")
                .getSingleResult();
        assertEquals(0L, challengeCount);
    }

    @Test
    @DisplayName("CS_addChallenge_07")
    void addChallenge_shouldFail_whenImageUploadFails() throws IOException {
        // Arrange
        ChallengeRequestDTO request = createValidRequest("New Challenge");
        MockMultipartFile validImage = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "valid image content".getBytes()
        );

        // Mock FirebaseService to throw IOException
        when(firebaseService.uploadOneFile(any(), any(), any()))
                .thenThrow(new IOException("Upload failed"));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.addChallenge(request, account.getEmail(), validImage)
        );
        assertEquals(ErrorCode.UPLOAD_FAILED, exception.getErrorCode());

        // Verify no challenge was created
        Long challengeCount = entityManager
                .createQuery("SELECT COUNT(c) FROM Challenge c WHERE c.title = :title", Long.class)
                .setParameter("title", "New Challenge")
                .getSingleResult();
        assertEquals(0L, challengeCount);
    }

    @Test
    @DisplayName("CS_addChallenge_08")
    void addChallenge_shouldFail_whenProjectNotExisted() {
        // Arrange
        ChallengeRequestDTO request = createValidRequest("New Challenge");
        ProjectRequestDTO projectRequest = ProjectRequestDTO.builder()
                .projectId(BigInteger.valueOf(999))
                .build();
        request.setProjects(List.of(projectRequest));

        // Verify project doesn't exist
        Optional<Project> project = projectRepository.findById(projectRequest.getProjectId());
        assertFalse(project.isPresent(), "Project should not exist before test");

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.addChallenge(request, account.getEmail(), null)
        );
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, exception.getErrorCode());

        // Verify no challenge was created
        Long challengeCount = entityManager
                .createQuery("SELECT COUNT(c) FROM Challenge c WHERE c.title = :title", Long.class)
                .setParameter("title", "New Challenge")
                .getSingleResult();
        assertEquals(0L, challengeCount);
    }

    @Test
    @DisplayName("CS_addChallenge_09")
    void addChallenge_shouldSucceed_whenProjectExistsInRequest() throws IOException {
        // Arrange
        Project existingProject = createProject(2);

        ChallengeRequestDTO request = createValidRequest("New Challenge");
        ProjectRequestDTO projectRequest = ProjectRequestDTO.builder()
                .projectId(existingProject.getProjectId())
                .build();
        request.setProjects(List.of(projectRequest));

        MockMultipartFile validImage = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "valid image content".getBytes()
        );

        // Mock Firebase upload
        String expectedUrl = "https://firebase.com/test.jpg";
        when(firebaseService.uploadOneFile(any(), any(), any()))
                .thenReturn(expectedUrl);

        // Act
        ChallengeResponseDTO response = challengeService.addChallenge(request, account.getEmail(), validImage);

        // Assert
        assertNotNull(response.getChallengeId(), "Challenge ID should not be null");
        assertNotNull(response.getProjectResponseDTOS(), "Projects list should not be null");
        assertFalse(response.getProjectResponseDTOS().isEmpty(), "Projects list should not be empty");
        assertEquals(existingProject.getProjectId(), response.getProjectResponseDTOS().get(0).getProjectId(),
                "Project ID should match");

        // Verify challenge and challenge_project were created in database
        Optional<Challenge> savedChallenge = challengeRepository.findById(response.getChallengeId());
        assertTrue(savedChallenge.isPresent(), "Challenge should exist in database");
        assertFalse(savedChallenge.get().getChallengeProjects().isEmpty(),
                "Challenge should have associated projects");
        assertEquals(existingProject.getProjectId(),
                savedChallenge.get().getChallengeProjects().get(0).getProject().getProjectId(),
                "Associated project should match request");
    }

    @Test
    @DisplayName("CS_addChallenge_10")
    void addChallenge_shouldSucceed_withDefaultProjects() throws IOException {
        // Arrange
        // Create some projects with status = 2
        Project project1 = createProject(2);
        Project project2 = createProject(2);
        Project project3 = createProject(1); // Different status, shouldn't be included

        String challengeTitle = "New Challenge";
        ChallengeRequestDTO request = createValidRequest(challengeTitle);
        // Don't set projects list - should use default behavior
        request.setProjects(null);

        MockMultipartFile validImage = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "valid image content".getBytes()
        );

        // Mock Firebase upload
        String expectedUrl = "https://firebase.com/test.jpg";
        when(firebaseService.uploadOneFile(any(), any(), any()))
                .thenReturn(expectedUrl);

        // Act
        ChallengeResponseDTO response = challengeService.addChallenge(request, account.getEmail(), validImage);

        // Assert response
        assertNotNull(response.getChallengeId(), "Challenge ID should not be null");
        assertEquals(challengeTitle, response.getTitle(), "Challenge title should match");
        assertEquals("Challenge Content", response.getContent(), "Challenge content should match");
        assertEquals(BigDecimal.valueOf(100), response.getGoal(), "Challenge goal should match");
        assertEquals(expectedUrl, response.getThumbnail(), "Challenge thumbnail URL should match");
        assertNotNull(response.getCreatedBy(), "Created by should not be null");

        // Verify projects were automatically associated
        assertNotNull(response.getProjectResponseDTOS(), "Projects list should not be null");
        assertEquals(2, response.getProjectResponseDTOS().size(), "Should have 2 projects with status = 2");
        assertTrue(response.getProjectResponseDTOS().stream()
                .anyMatch(p -> p.getProjectId().equals(project1.getProjectId())),
                "Should include first project with status 2");
        assertTrue(response.getProjectResponseDTOS().stream()
                .anyMatch(p -> p.getProjectId().equals(project2.getProjectId())),
                "Should include second project with status 2");
        assertFalse(response.getProjectResponseDTOS().stream()
                .anyMatch(p -> p.getProjectId().equals(project3.getProjectId())),
                "Should not include project with status != 2");

        // Verify challenge was created in database
        Optional<Challenge> savedChallenge = challengeRepository.findById(response.getChallengeId());
        assertTrue(savedChallenge.isPresent(), "Challenge should exist in database");
        Challenge challenge = savedChallenge.get();
        assertEquals(challengeTitle, challenge.getTitle(), "Saved title should match");
        assertEquals("Challenge Content", challenge.getContent(), "Saved content should match");
        assertEquals(BigDecimal.valueOf(100), challenge.getGoal(), "Saved goal should match");
        assertEquals(expectedUrl, challenge.getThumbnail(), "Saved thumbnail should match");
        assertEquals(account.getAccountId(), challenge.getCreatedBy().getAccountId(), "Saved creator should match");
        assertNotNull(challenge.getCreatedAt(), "Created date should not be null");
        assertEquals(LocalDate.now().plusDays(10), challenge.getFinishedAt(), "Finish date should match");

        // Verify challenge_project associations
        assertFalse(challenge.getChallengeProjects().isEmpty(), "Challenge should have associated projects");
        assertEquals(2, challenge.getChallengeProjects().size(), "Should have 2 challenge-project associations");
    }



    // Update Challenge Test Cases
    @Test
    @DisplayName("CS_updateChallenge_01")
    void updateChallenge_shouldFail_whenChallengeNotFound() {
        // Arrange
        BigInteger nonExistentId = BigInteger.valueOf(999);
        ChallengeRequestDTO request = createValidRequest("New Challenge");

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.updateChallenge(request, nonExistentId, null, account.getEmail())
        );
        assertEquals(ErrorCode.CHALLENGE_NOT_FOUND, exception.getErrorCode());

        // Verify no challenge was created with this ID
        Optional<Challenge> challenge = challengeRepository.findById(nonExistentId);
        assertFalse(challenge.isPresent(), "Challenge should not exist");
    }

    @Test
    @DisplayName("CS_updateChallenge_02")
    void updateChallenge_shouldFail_whenUserNotFound() {
        // Arrange
        Challenge challenge = challengeRepository.save(Challenge.builder()
                .title("Original Challenge")
                .content("Original Content")
                .goal(BigDecimal.valueOf(100))
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .build());

        ChallengeRequestDTO request = createValidRequest("Updated Challenge");
        String nonExistentEmail = "nonexistent@example.com";

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.updateChallenge(request, challenge.getChallengeId(), null, nonExistentEmail)
        );
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, exception.getErrorCode());

        // Verify challenge was not updated
        Challenge unchangedChallenge = challengeRepository.findById(challenge.getChallengeId())
                .orElseThrow(() -> new RuntimeException("Challenge should exist"));
        assertEquals("Original Challenge", unchangedChallenge.getTitle(), "Title should not be updated");
        assertEquals("Original Content", unchangedChallenge.getContent(), "Content should not be updated");
        assertEquals(BigDecimal.valueOf(100), unchangedChallenge.getGoal(), "Goal should not be updated");
    }

    @Test
    @DisplayName("CS_updateChallenge_12")
    void updateChallenge_shouldFail_whenUserIsNotCreator() {
        // Arrange
        Challenge challenge = challengeRepository.save(Challenge.builder()
                .title("Original Challenge")
                .content("Original Content")
                .goal(BigDecimal.valueOf(100))
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .build());

        // Create another user account
        Account otherUser = accountRepository.save(Account.builder()
                .email("other@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullname("Other User")
                .phone("0123456789")
                .address("Test Address")
                .gender(1)
                .dob(LocalDate.of(2000, 1, 1))
                .isActive(true)
                .role(adminRole)
                .build());

        ChallengeRequestDTO request = createValidRequest("Updated Challenge");

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.updateChallenge(request, challenge.getChallengeId(), null, otherUser.getEmail())
        );
        assertEquals(ErrorCode.HTTP_UNAUTHORIZED, exception.getErrorCode());

        // Verify challenge was not updated
        Challenge unchangedChallenge = challengeRepository.findById(challenge.getChallengeId())
                .orElseThrow(() -> new RuntimeException("Challenge should exist"));
        assertEquals("Original Challenge", unchangedChallenge.getTitle(), "Title should not be updated");
        assertEquals("Original Content", unchangedChallenge.getContent(), "Content should not be updated");
        assertEquals(BigDecimal.valueOf(100), unchangedChallenge.getGoal(), "Goal should not be updated");
    }

    @Test
    @DisplayName("CS_updateChallenge_03")
    void updateChallenge_shouldFail_whenTitleExists() {
        // Arrange
        String existingTitle = "Existing Challenge";
        Challenge existingChallenge = challengeRepository.save(Challenge.builder()
                .title(existingTitle)
                .content("Existing Content")
                .goal(BigDecimal.valueOf(100))
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .build());

        Challenge challengeToUpdate = challengeRepository.save(Challenge.builder()
                .title("Original Challenge")
                .content("Original Content")
                .goal(BigDecimal.valueOf(100))
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .build());

        ChallengeRequestDTO request = createValidRequest(existingTitle);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.updateChallenge(request, challengeToUpdate.getChallengeId(), null, account.getEmail())
        );
        assertEquals(ErrorCode.DUPLICATE_TITLE, exception.getErrorCode());

        // Verify challenge was not updated
        Challenge unchangedChallenge = challengeRepository.findById(challengeToUpdate.getChallengeId())
                .orElseThrow(() -> new RuntimeException("Challenge should exist"));
        assertEquals("Original Challenge", unchangedChallenge.getTitle(), "Title should not be updated");
        assertEquals("Original Content", unchangedChallenge.getContent(), "Content should not be updated");
        assertEquals(BigDecimal.valueOf(100), unchangedChallenge.getGoal(), "Goal should not be updated");
    }

    @Test
    @DisplayName("CS_updateChallenge_04")
    void updateChallenge_shouldFail_whenExistingChallengeFinishDateIsBeforeToday() {
        // Arrange
        Challenge challenge = challengeRepository.save(Challenge.builder()
                .title("Original Challenge")
                .content("Original Content")
                .goal(BigDecimal.valueOf(100))
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().minusDays(1))
                .build());

        ChallengeRequestDTO request = createValidRequest("Updated Challenge");

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.updateChallenge(request, challenge.getChallengeId(), null, account.getEmail())
        );
        assertEquals(ErrorCode.CHALLENGE_ALREADY_FINISHED, exception.getErrorCode());

        // Verify challenge was not updated
        Challenge unchangedChallenge = challengeRepository.findById(challenge.getChallengeId())
                .orElseThrow(() -> new RuntimeException("Challenge should exist"));
        assertEquals("Original Challenge", unchangedChallenge.getTitle(), "Title should not be updated");
        assertEquals("Original Content", unchangedChallenge.getContent(), "Content should not be updated");
        assertEquals(BigDecimal.valueOf(100), unchangedChallenge.getGoal(), "Goal should not be updated");
    }

    @Test
    @DisplayName("CS_updateChallenge_05")
    void updateChallenge_shouldFail_whenRequestFinishDateIsBeforeToday() {
        // Arrange
        Challenge challenge = challengeRepository.save(Challenge.builder()
                .title("Original Challenge")
                .content("Original Content")
                .goal(BigDecimal.valueOf(100))
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .build());

        ChallengeRequestDTO request = createValidRequest("Updated Challenge");
        request.setFinishedAt(LocalDate.now().minusDays(1));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.updateChallenge(request, challenge.getChallengeId(), null, account.getEmail())
        );
        assertEquals(ErrorCode.INVALID_FINISH_DATE, exception.getErrorCode());

        // Verify challenge was not updated
        Challenge unchangedChallenge = challengeRepository.findById(challenge.getChallengeId())
                .orElseThrow(() -> new RuntimeException("Challenge should exist"));
        assertEquals("Original Challenge", unchangedChallenge.getTitle(), "Title should not be updated");
        assertEquals("Original Content", unchangedChallenge.getContent(), "Content should not be updated");
        assertEquals(BigDecimal.valueOf(100), unchangedChallenge.getGoal(), "Goal should not be updated");
    }

    @Test
    @DisplayName("CS_updateChallenge_06")
    void updateChallenge_shouldDeleteThumbnail_whenRequestThumbnailIsNullAndExistingThumbnailExists() throws IOException {
        // Arrange
        Challenge challenge = challengeRepository.save(Challenge.builder()
                .title("Original Challenge")
                .content("Original Content")
                .goal(BigDecimal.valueOf(100))
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .thumbnail("existing-thumbnail.jpg")
                .build());

        ChallengeRequestDTO request = createValidRequest("Updated Challenge");
        request.setThumbnail(null);

        // Act
        ChallengeResponseDTO response = challengeService.updateChallenge(request, challenge.getChallengeId(), null, account.getEmail());

        // Assert
        assertNotNull(response);
        assertNull(response.getThumbnail());

        // Verify challenge was updated in database
        Challenge updatedChallenge = challengeRepository.findById(challenge.getChallengeId())
                .orElseThrow(() -> new RuntimeException("Challenge should exist"));
        assertNull(updatedChallenge.getThumbnail(), "Thumbnail should be null");
        assertEquals("Updated Challenge", updatedChallenge.getTitle(), "Title should be updated");
        assertEquals("Challenge Content", updatedChallenge.getContent(), "Content should be updated");
        assertEquals(BigDecimal.valueOf(100), updatedChallenge.getGoal(), "Goal should be updated");
    }

    @Test
    @DisplayName("CS_updateChallenge_07")
    void updateChallenge_shouldSetNullThumbnail_whenBothThumbnailsAreNull() throws IOException {
        // Arrange
        Challenge challenge = challengeRepository.save(Challenge.builder()
                .title("Original Challenge")
                .content("Original Content")
                .goal(BigDecimal.valueOf(100))
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .thumbnail(null)
                .build());

        ChallengeRequestDTO request = createValidRequest("Updated Challenge");
        request.setThumbnail(null);

        // Act
        ChallengeResponseDTO response = challengeService.updateChallenge(request, challenge.getChallengeId(), null, account.getEmail());

        // Assert
        assertNotNull(response);
        assertNull(response.getThumbnail());

        // Verify challenge was updated in database
        Challenge updatedChallenge = challengeRepository.findById(challenge.getChallengeId())
                .orElseThrow(() -> new RuntimeException("Challenge should exist"));
        assertNull(updatedChallenge.getThumbnail(), "Thumbnail should be null");
        assertEquals("Updated Challenge", updatedChallenge.getTitle(), "Title should be updated");
        assertEquals("Challenge Content", updatedChallenge.getContent(), "Content should be updated");
        assertEquals(BigDecimal.valueOf(100), updatedChallenge.getGoal(), "Goal should be updated");
    }

    @Test
    @DisplayName("CS_updateChallenge_08")
    void updateChallenge_shouldFail_whenThumbnailHasInvalidContentType() {
        // Arrange
        Challenge challenge = challengeRepository.save(Challenge.builder()
                .title("Original Challenge")
                .content("Original Content")
                .goal(BigDecimal.valueOf(100))
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .build());

        ChallengeRequestDTO request = createValidRequest("Updated Challenge");
        MockMultipartFile invalidImage = new MockMultipartFile(
                "image",
                "test.txt",
                "text/plain",
                "invalid content".getBytes()
        );

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.updateChallenge(request, challenge.getChallengeId(), invalidImage, account.getEmail())
        );
        assertEquals(ErrorCode.HTTP_FILE_IS_NOT_IMAGE, exception.getErrorCode());

        // Verify challenge was not updated
        Challenge unchangedChallenge = challengeRepository.findById(challenge.getChallengeId())
                .orElseThrow(() -> new RuntimeException("Challenge should exist"));
        assertEquals("Original Challenge", unchangedChallenge.getTitle(), "Title should not be updated");
        assertEquals("Original Content", unchangedChallenge.getContent(), "Content should not be updated");
        assertEquals(BigDecimal.valueOf(100), unchangedChallenge.getGoal(), "Goal should not be updated");
    }

    @Test
    @DisplayName("CS_updateChallenge_09")
    void updateChallenge_shouldFail_whenThumbnailSizeIsInvalid() {
        // Arrange
        Challenge challenge = challengeRepository.save(Challenge.builder()
                .title("Original Challenge")
                .content("Original Content")
                .goal(BigDecimal.valueOf(100))
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .build());

        ChallengeRequestDTO request = createValidRequest("Updated Challenge");
        byte[] largeImage = new byte[3 * 1024 * 1024]; // 3MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "image",
                "large.jpg",
                "image/jpeg",
                largeImage
        );

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.updateChallenge(request, challenge.getChallengeId(), largeFile, account.getEmail())
        );
        assertEquals(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT, exception.getErrorCode());

        // Verify challenge was not updated
        Challenge unchangedChallenge = challengeRepository.findById(challenge.getChallengeId())
                .orElseThrow(() -> new RuntimeException("Challenge should exist"));
        assertEquals("Original Challenge", unchangedChallenge.getTitle(), "Title should not be updated");
        assertEquals("Original Content", unchangedChallenge.getContent(), "Content should not be updated");
        assertEquals(BigDecimal.valueOf(100), unchangedChallenge.getGoal(), "Goal should not be updated");
    }

    @Test
    @DisplayName("CS_updateChallenge_10")
    void updateChallenge_shouldSucceed_whenUploadingValidThumbnail() throws IOException {
        // Arrange
        Challenge challenge = challengeRepository.save(Challenge.builder()
                .title("Original Challenge")
                .content("Original Content")
                .goal(BigDecimal.valueOf(100))
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .build());

        ChallengeRequestDTO request = createValidRequest("Updated Challenge");
        MockMultipartFile validImage = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "valid content".getBytes()
        );

        String expectedUrl = "https://firebase.com/test.jpg";
        when(firebaseService.uploadOneFile(any(), any(), any()))
                .thenReturn(expectedUrl);

        // Act
        ChallengeResponseDTO response = challengeService.updateChallenge(request, challenge.getChallengeId(), validImage, account.getEmail());

        // Assert
        assertNotNull(response);
        assertEquals(expectedUrl, response.getThumbnail());

        // Verify challenge was updated in database
        Challenge updatedChallenge = challengeRepository.findById(challenge.getChallengeId())
                .orElseThrow(() -> new RuntimeException("Challenge should exist"));
        assertEquals(expectedUrl, updatedChallenge.getThumbnail(), "Thumbnail should be updated");
        assertEquals("Updated Challenge", updatedChallenge.getTitle(), "Title should be updated");
        assertEquals("Challenge Content", updatedChallenge.getContent(), "Content should be updated");
        assertEquals(BigDecimal.valueOf(100), updatedChallenge.getGoal(), "Goal should be updated");
    }

    @Test
    @DisplayName("CS_updateChallenge_11")
    void updateChallenge_shouldFail_whenUploadFails() throws IOException {
        // Arrange
        Challenge challenge = challengeRepository.save(Challenge.builder()
                .title("Original Challenge")
                .content("Original Content")
                .goal(BigDecimal.valueOf(100))
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .build());

        ChallengeRequestDTO request = createValidRequest("Updated Challenge");
        MockMultipartFile validImage = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "valid content".getBytes()
        );

        when(firebaseService.uploadOneFile(any(), any(), any()))
                .thenThrow(new IOException("Upload failed"));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.updateChallenge(request, challenge.getChallengeId(), validImage, account.getEmail())
        );
        assertEquals(ErrorCode.UPLOAD_FAILED, exception.getErrorCode());

        // Verify challenge was not updated
        Challenge unchangedChallenge = challengeRepository.findById(challenge.getChallengeId())
                .orElseThrow(() -> new RuntimeException("Challenge should exist"));
        assertEquals("Original Challenge", unchangedChallenge.getTitle(), "Title should not be updated");
        assertEquals("Original Content", unchangedChallenge.getContent(), "Content should not be updated");
        assertEquals(BigDecimal.valueOf(100), unchangedChallenge.getGoal(), "Goal should not be updated");
    }

    @Test
    @DisplayName("CS_viewActiveChallengesByFilter_01")
    void viewActiveChallengesByFilter_shouldReturnCorrectTotalDonation_whenAccountCodeExists() {
        // Arrange
        Account testAccount = accountRepository.save(Account.builder()
                .email("test2@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullname("Test User Two")
                .code("ABC123")
                .phone("0123456789")
                .address("Test Address")
                .gender(1)
                .dob(LocalDate.of(2000, 1, 1))
                .isActive(true)
                .role(adminRole)
                .build());

        // Create an active challenge
        Challenge activeChallenge = challengeRepository.save(Challenge.builder()
                .title("Active Challenge")
                .content("Test Content")
                .goal(BigDecimal.valueOf(1000))
                .createdBy(testAccount)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5)) // Future date = active
                .build());

        // Act
        PageResponse<ChallengeResponseDTO> response = challengeService.viewActiveChallengesByFilter(0, 10, "ABC123");

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getContent(), "Content should not be null");
        assertFalse(response.getContent().isEmpty(), "Should have at least one challenge");
        
        ChallengeResponseDTO challengeResponse = response.getContent().getFirst();
        assertNotNull(challengeResponse.getTotalDonation(), "Total donation should not be null");
        assertEquals(new BigDecimal("0.00"), challengeResponse.getTotalDonation(), "Total donation should be zero for new challenge");
        
        // Verify pagination
        assertEquals(0, response.getOffset(), "Page offset should be 0");
        assertEquals(10, response.getLimit(), "Page size should be 10");
        assertTrue(response.getTotal() > 0, "Total elements should be greater than 0");
        
        // Verify challenge details
        assertEquals("Active Challenge", challengeResponse.getTitle());
        assertEquals("Test Content", challengeResponse.getContent());
        assertEquals(new BigDecimal("1000.00"), challengeResponse.getGoal());
        assertEquals(testAccount.getAccountId(), challengeResponse.getCreatedBy().getAccountId());
    }

    @Test
    @DisplayName("CS_viewActiveChallengesByFilter_02")
    void viewActiveChallengesByFilter_shouldReturnCorrectTotalDonation_whenDifferentAccountCode() {
        // Arrange
        Account testAccount = accountRepository.save(Account.builder()
                .email("test3@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullname("Test User three")
                .code("XYZ789")
                .phone("0123456789")
                .address("Test Address")
                .gender(1)
                .dob(LocalDate.of(2000, 1, 1))
                .isActive(true)
                .role(adminRole)
                .build());

        // Create an active challenge
        Challenge activeChallenge = challengeRepository.save(Challenge.builder()
                .title("Another Active Challenge")
                .content("Test Content")
                .goal(BigDecimal.valueOf(2000))
                .createdBy(testAccount)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(10)) // Future date = active
                .build());

        // Act
        PageResponse<ChallengeResponseDTO> response = challengeService.viewActiveChallengesByFilter(0, 10, "XYZ789");

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getContent(), "Content should not be null");
        assertFalse(response.getContent().isEmpty(), "Should have at least one challenge");
        
        ChallengeResponseDTO challengeResponse = response.getContent().get(0);
        assertNotNull(challengeResponse.getTotalDonation(), "Total donation should not be null");
        assertEquals(new BigDecimal("0.00"), challengeResponse.getTotalDonation(), "Total donation should be zero for new challenge");
        
        // Verify pagination
        assertEquals(0, response.getOffset(), "Page offset should be 0");
        assertEquals(10, response.getLimit(), "Page size should be 10");
        assertTrue(response.getTotal() > 0, "Total elements should be greater than 0");
        
        // Verify challenge details
        assertEquals("Another Active Challenge", challengeResponse.getTitle());
        assertEquals("Test Content", challengeResponse.getContent());
        assertEquals(new BigDecimal("2000.00"), challengeResponse.getGoal());
        assertEquals(testAccount.getAccountId(), challengeResponse.getCreatedBy().getAccountId());
    }

    @Test
    @DisplayName("CS_viewExpiredChallengesByFilter_01")
    void viewExpiredChallengesByFilter_shouldReturnCorrectTotalDonation_whenFirstAccountCode() {
        // Arrange
        Account testAccount = accountRepository.save(Account.builder()
                .email("test4@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullname("Test User Four")
                .code("user01")
                .phone("0123456789")
                .address("Test Address")
                .gender(1)
                .dob(LocalDate.of(2000, 1, 1))
                .isActive(true)
                .role(adminRole)
                .build());

        // Create expired challenges
        Challenge expiredChallenge1 = challengeRepository.save(Challenge.builder()
                .title("Expired Challenge One")
                .content("Test Content")
                .goal(BigDecimal.valueOf(1000))
                .createdBy(testAccount)
                .createdAt(LocalDateTime.now().minusDays(30))
                .finishedAt(LocalDate.now().minusDays(5)) // Past date = expired
                .build());

        Challenge expiredChallenge2 = challengeRepository.save(Challenge.builder()
                .title("Expired Challenge Two")
                .content("Test Content")
                .goal(BigDecimal.valueOf(2000))
                .createdBy(testAccount)
                .createdAt(LocalDateTime.now().minusDays(20))
                .finishedAt(LocalDate.now().minusDays(2)) // Past date = expired
                .build());

        // Act
        PageResponse<ChallengeResponseDTO> response = challengeService.viewExpiredChallengesByFilter(0, 10, "user01");

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getContent(), "Content should not be null");
        assertEquals(2, response.getContent().size(), "Should have exactly 2 challenges");
        
        // Verify pagination
        assertEquals(0, response.getOffset(), "Page offset should be 0");
        assertEquals(10, response.getLimit(), "Page size should be 10");
        assertEquals(2, response.getTotal(), "Total elements should be 2");
        
        // Verify summary
        assertNotNull(response.getSummary(), "Summary should not be null");
        assertEquals(new BigDecimal("300.00"), response.getSummary().get("total_donation"), 
            "Total donation should be 300.00");
    }

    @Test
    @DisplayName("CS_viewExpiredChallengesByFilter_02")
    void viewExpiredChallengesByFilter_shouldReturnCorrectTotalDonation_whenSecondAccountCode() {
        // Arrange
        Account testAccount = accountRepository.save(Account.builder()
                .email("test5@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullname("Test User Five")
                .code("user02")
                .phone("0123456789")
                .address("Test Address")
                .gender(1)
                .dob(LocalDate.of(2000, 1, 1))
                .isActive(true)
                .role(adminRole)
                .build());

        // Create one expired challenge
        Challenge expiredChallenge = challengeRepository.save(Challenge.builder()
                .title("Expired Challenge Three")
                .content("Test Content")
                .goal(BigDecimal.valueOf(1500))
                .createdBy(testAccount)
                .createdAt(LocalDateTime.now().minusDays(15))
                .finishedAt(LocalDate.now().minusDays(1)) // Past date = expired
                .build());

        // Act
        PageResponse<ChallengeResponseDTO> response = challengeService.viewExpiredChallengesByFilter(0, 10, "user02");

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getContent(), "Content should not be null");
        assertEquals(1, response.getContent().size(), "Should have exactly 1 challenge");
        
        // Verify pagination
        assertEquals(0, response.getOffset(), "Page offset should be 0");
        assertEquals(10, response.getLimit(), "Page size should be 10");
        assertEquals(1, response.getTotal(), "Total elements should be 1");
        
        // Verify summary
        assertNotNull(response.getSummary(), "Summary should not be null");
        assertEquals(new BigDecimal("0.00"), response.getSummary().get("total_donation"), 
            "Total donation should be 0.00");
    }

    @Test
    @DisplayName("CS_viewChallengesAdminByFilter_01")
    void viewChallengesAdminByFilter_shouldReturnFilteredResults() {
        // Arrange
        Account testAccount = accountRepository.save(Account.builder()
                .email("test6@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullname("Test User Six")
                .code("admin01")
                .phone("0123456789")
                .address("Test Address")
                .gender(1)
                .dob(LocalDate.of(2000, 1, 1))
                .isActive(true)
                .role(adminRole)
                .build());

        // Create challenges that match the filter criteria
        Challenge challenge1 = challengeRepository.save(Challenge.builder()
                .title("Environment Protection Challenge")
                .content("Test Content 1")
                .goal(BigDecimal.valueOf(1000))
                .createdBy(testAccount)
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .finishedAt(LocalDate.of(2024, 12, 31))
                .build());

        Challenge challenge2 = challengeRepository.save(Challenge.builder()
                .title("Environment Awareness Campaign")
                .content("Test Content 2")
                .goal(BigDecimal.valueOf(2000))
                .createdBy(testAccount)
                .createdAt(LocalDateTime.of(2024, 2, 1, 0, 0))
                .finishedAt(LocalDate.of(2024, 12, 31))
                .build());

        // Create a challenge that shouldn't match the filter
        Challenge challenge3 = challengeRepository.save(Challenge.builder()
                .title("Health Challenge")
                .content("Test Content 3")
                .goal(BigDecimal.valueOf(3000))
                .createdBy(testAccount)
                .createdAt(LocalDateTime.of(2023, 1, 1, 0, 0))
                .finishedAt(LocalDate.of(2023, 12, 31))
                .build());

        // Act
        PageResponse<?> response = challengeService.viewChallengesAdminByFilter(
                0,                              // page
                10,                             // size
                "Environment",                  // title
                2024,                          // year
                BigDecimal.valueOf(100),       // minDonation
                BigDecimal.valueOf(500)        // maxDonation
        );

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getContent(), "Content should not be null");
        
        // Verify result size
        assertEquals(2, response.getContent().size(), "Should have exactly 2 challenges");
        assertEquals(2, response.getTotal(), "Total count should be 2");
        
        // Verify pagination
        assertEquals(0, response.getOffset(), "Page offset should be 0");
        assertEquals(10, response.getLimit(), "Page size should be 10");

        // Verify content contains expected challenges
        List<ChallengeResponseDTO> challenges = (List<ChallengeResponseDTO>) response.getContent();
        assertTrue(challenges.stream()
                .anyMatch(c -> c.getTitle().equals("Environment Protection Challenge")),
                "Should contain first environment challenge");
        assertTrue(challenges.stream()
                .anyMatch(c -> c.getTitle().equals("Environment Awareness Campaign")),
                "Should contain second environment challenge");
        
        // Verify each challenge has correct data mapping
        for (ChallengeResponseDTO challenge : challenges) {
            assertNotNull(challenge.getChallengeId(), "Challenge ID should not be null");
            assertNotNull(challenge.getTitle(), "Title should not be null");
            assertNotNull(challenge.getContent(), "Content should not be null");
            assertNotNull(challenge.getGoal(), "Goal should not be null");
            assertNotNull(challenge.getCreatedBy(), "CreatedBy should not be null");
            assertNotNull(challenge.getCreatedAt(), "CreatedAt should not be null");
            assertNotNull(challenge.getFinishedAt(), "FinishedAt should not be null");
            assertTrue(challenge.getTitle().contains("Environment"), 
                    "Challenge title should contain 'Environment'");
            assertEquals(2024, challenge.getCreatedAt().getYear(), 
                    "Challenge should be from 2024");
        }
    }

    @Test
    @DisplayName("CS_getChallengeById_01")
    void getChallengeById_shouldFail_whenChallengeNotFound() {
        // Arrange
        BigInteger nonExistentId = BigInteger.valueOf(1);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.getChallengeById(nonExistentId)
        );
        assertEquals(ErrorCode.CHALLENGE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("CS_getChallengeById_02")
    void getChallengeById_shouldSucceed_whenChallengeExists() {
        // Arrange
        // Create projects
        Project project1 = createProject(2);
        project1.setCode("PRJ001");
        project1.setTitle("Project One");
        project1.setAmountNeededToRaise(BigDecimal.valueOf(5000));
        projectRepository.save(project1);

        Project project2 = createProject(2);
        project2.setCode("PRJ002");
        project2.setTitle("Project Two");
        project2.setAmountNeededToRaise(BigDecimal.valueOf(3000));
        projectRepository.save(project2);

        // Create a challenge
        Challenge challenge = challengeRepository.save(Challenge.builder()
                .title("Test Challenge")
                .challengeCode("CHL001")
                .slug("test-challenge")
                .content("Test Content")
                .goal(BigDecimal.valueOf(1000))
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .build());

        // Create challenge-project associations
        entityManager.createNativeQuery(
            "INSERT INTO challenge_project (challenge_id, project_id) VALUES (?, ?)"
        )
        .setParameter(1, challenge.getChallengeId())
        .setParameter(2, project1.getProjectId())
        .executeUpdate();

        entityManager.createNativeQuery(
            "INSERT INTO challenge_project (challenge_id, project_id) VALUES (?, ?)"
        )
        .setParameter(1, challenge.getChallengeId())
        .setParameter(2, project2.getProjectId())
        .executeUpdate();

        // Refresh the challenge entity to load the challenge-project associations
        entityManager.flush();
        entityManager.refresh(challenge);

        // Act
        ChallengeResponseDTO response = challengeService.getChallengeById(challenge.getChallengeId());

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(challenge.getChallengeId(), response.getChallengeId(), "Challenge ID should match");
        assertEquals("CHL001", response.getChallengeCode(), "Challenge code should match");
        assertEquals("Test Challenge", response.getTitle(), "Title should match");
        assertEquals("test-challenge", response.getSlug(), "Slug should match");
        assertEquals("Test Content", response.getContent(), "Content should match");
        assertEquals(new BigDecimal("1000.00"), response.getGoal(), "Goal should match");
        assertNotNull(response.getCreatedBy(), "CreatedBy should not be null");
        assertEquals(account.getAccountId(), response.getCreatedBy().getAccountId(), "Creator ID should match");
        assertEquals(account.getFullname(), response.getCreatedBy().getFullname(), "Creator name should match");
        assertNotNull(response.getCreatedAt(), "CreatedAt should not be null");
        assertEquals(challenge.getFinishedAt(), response.getFinishedAt(), "FinishedAt should match");
        assertNotNull(response.getTotalDonation(), "TotalDonation should not be null");

        // Verify projects
        assertNotNull(response.getProjectResponseDTOS(), "Project list should not be null");
        assertEquals(2, response.getProjectResponseDTOS().size(), "Should have 2 projects");

        // Verify first project
        assertTrue(response.getProjectResponseDTOS().stream()
                .anyMatch(p -> p.getProjectId().equals(project1.getProjectId()) &&
                             p.getCode().equals("PRJ001") &&
                             p.getTitle().equals("Project One") &&
                             p.getAmountNeededToRaise().equals(BigDecimal.valueOf(5000))),
                "First project should be correctly mapped");

        // Verify second project
        assertTrue(response.getProjectResponseDTOS().stream()
                .anyMatch(p -> p.getProjectId().equals(project2.getProjectId()) &&
                             p.getCode().equals("PRJ002") &&
                             p.getTitle().equals("Project Two") &&
                             p.getAmountNeededToRaise().equals(BigDecimal.valueOf(3000))),
                "Second project should be correctly mapped");
    }

    @Test
    @DisplayName("CS_deleteChallengeById_01")
    void deleteChallenge_shouldFail_whenChallengeNotFound() {
        // Arrange
        BigInteger nonExistentId = BigInteger.valueOf(1);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.deleteChallenge(nonExistentId)
        );
        assertEquals(ErrorCode.CHALLENGE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("CS_deleteChallengeById_02")
    void deleteChallenge_shouldSucceed_whenChallengeHasThumbnail() throws IOException {
        // Arrange
        Challenge challenge = challengeRepository.save(Challenge.builder()
                .title("Challenge to Delete")
                .content("Test Content")
                .goal(BigDecimal.valueOf(100))
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .thumbnail("test-thumbnail.jpg")
                .build());

        // Create some challenge projects
        Project project = createProject(2);
        ChallengeProject challengeProject = ChallengeProject.builder()
                .challenge(challenge)
                .project(project)
                .build();
        challengeProjectRepository.save(challengeProject);

        // Mock Firebase delete
        when(firebaseService.deleteFileByPath(anyString())).thenReturn(true);

        // Act
        challengeService.deleteChallenge(challenge.getChallengeId());

        // Assert
        // Verify challenge was deleted
        Optional<Challenge> deletedChallenge = challengeRepository.findById(challenge.getChallengeId());
        assertFalse(deletedChallenge.isPresent(), "Challenge should be deleted");

        // Verify challenge projects were deleted
        List<ChallengeProject> remainingChallengeProjects = challengeProjectRepository.findByChallenge(challenge);
        assertTrue(remainingChallengeProjects.isEmpty(), "Challenge projects should be deleted");

        // Verify Firebase delete was called
        verify(firebaseService, times(1)).deleteFileByPath(challenge.getThumbnail());
    }

    @Test
    @DisplayName("CS_deleteChallengeById_03 ")
    void deleteChallenge_shouldFail_whenDeleteFileFails() throws IOException {
        // Arrange
        Challenge challenge = challengeRepository.save(Challenge.builder()
                .title("Challenge with Thumbnail")
                .content("Test Content")
                .goal(BigDecimal.valueOf(100))
                .createdBy(account)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .thumbnail("test-thumbnail.jpg")  // Has thumbnail
                .build());

        // Create some challenge projects
        Project project = createProject(2);
        ChallengeProject challengeProject = ChallengeProject.builder()
                .challenge(challenge)
                .project(project)
                .build();
        challengeProjectRepository.save(challengeProject);

        // Mock Firebase delete to throw IOException
        when(firebaseService.deleteFileByPath(anyString())).thenThrow(new IOException("Delete failed"));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                challengeService.deleteChallenge(challenge.getChallengeId())
        );
        assertEquals(ErrorCode.DELETE_FILE_FAILED, exception.getErrorCode());

        // Verify challenge was not deleted
        Optional<Challenge> existingChallenge = challengeRepository.findById(challenge.getChallengeId());
        assertTrue(existingChallenge.isPresent(), "Challenge should not be deleted");
        assertEquals("test-thumbnail.jpg", existingChallenge.get().getThumbnail(), "Thumbnail should not be deleted");

//        // Verify challenge projects were not deleted
//        List<ChallengeProject> remainingChallengeProjects = challengeProjectRepository.findByChallenge(challenge);
//        assertFalse(remainingChallengeProjects.isEmpty(), "Challenge projects should not be deleted");

        // Verify Firebase delete was attempted
        verify(firebaseService, times(1)).deleteFileByPath(challenge.getThumbnail());
    }

    @Test
    @DisplayName("CS_getTopChallenges_01 ")
    void getTopChallenges_shouldReturnValidList_whenRepositoryReturnsData() {
        // Arrange
        Account testAccount = accountRepository.save(Account.builder()
                .email("test7@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullname("Test User Seven")
                .code("TOP001")
                .phone("0123456789")
                .address("Test Address")
                .gender(1)
                .dob(LocalDate.of(2000, 1, 1))
                .isActive(true)
                .role(adminRole)
                .avatar("avatar.jpg")
                .build());

        // Create test challenges
        Challenge challenge1 = challengeRepository.save(Challenge.builder()
                .title("Top Challenge 1")
                .challengeCode("TCH001")
                .content("Test Content 1")
                .goal(BigDecimal.valueOf(1000))
                .createdBy(testAccount)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .build());

        Challenge challenge2 = challengeRepository.save(Challenge.builder()
                .title("Top Challenge 2")
                .challengeCode("TCH002")
                .content("Test Content 2")
                .goal(BigDecimal.valueOf(2000))
                .createdBy(testAccount)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(5))
                .build());

        // Act
        List<ChallengeResponseDTO> result = challengeService.getTopChallenges(3);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isEmpty(), "Result should not be empty");
        assertTrue(result.size() <= 3, "Result size should not exceed requested number");

        // Verify challenge details
        ChallengeResponseDTO firstChallenge = result.get(0);
        assertNotNull(firstChallenge.getChallengeId(), "Challenge ID should not be null");
        assertNotNull(firstChallenge.getChallengeCode(), "Challenge code should not be null");
        assertNotNull(firstChallenge.getTitle(), "Title should not be null");
        assertNotNull(firstChallenge.getContent(), "Content should not be null");
        assertNotNull(firstChallenge.getGoal(), "Goal should not be null");
        assertNotNull(firstChallenge.getCreatedAt(), "Created at should not be null");
        assertNotNull(firstChallenge.getFinishedAt(), "Finished at should not be null");
        assertNotNull(firstChallenge.getTotalDonation(), "Total donation should not be null");

        // Verify creator details
        assertNotNull(firstChallenge.getCreatedBy(), "Created by should not be null");
        assertEquals(testAccount.getAccountId(), firstChallenge.getCreatedBy().getAccountId());
        assertEquals(testAccount.getCode(), firstChallenge.getCreatedBy().getCode());
        assertEquals(testAccount.getFullname(), firstChallenge.getCreatedBy().getFullname());
        assertEquals(testAccount.getAvatar(), firstChallenge.getCreatedBy().getAvatar());
    }

    @Test
    @DisplayName("CS_getTopChallenges_02 ")
    void getTopChallenges_shouldReturnEmptyList_whenRepositoryReturnsNull() {
        // Arrange
        ChallengeRepository mockChallengeRepository = mock(ChallengeRepository.class);
        when(mockChallengeRepository.getTopChallenge(3)).thenReturn(null);

        ChallengeService challengeServiceWithMock = new ChallengerServiceImpl(
                projectRepository,
                mockChallengeRepository,
                accountRepository,
                firebaseService,
                codeUtils,
                slugUtils,
                challengeProjectRepository,
                donationRepository
        );

        // Act
        List<ChallengeResponseDTO> result = challengeServiceWithMock.getTopChallenges(3);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty when repository returns null");
        
        // Verify repository was called
        verify(mockChallengeRepository).getTopChallenge(3);
    }

    @Test
    @DisplayName("CS_getChallenges_01")
    void getChallenges_shouldReturnFilteredResults_whenDataExists() {
        // Test with all fields populated
        testGetChallengesWithAllFields();


    }

    private void testGetChallengesWithAllFields() {
        // Arrange
        Account testAccount = accountRepository.save(Account.builder()
                .email("test1@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullname("John Doe")
                .code("JD001")
                .phone("0123456789")
                .address("Test Address")
                .gender(1)
                .dob(LocalDate.of(2000, 1, 1))
                .isActive(true)
                .role(adminRole)
                .avatar("test-avatar.jpg")
                .build());

        // Create challenge with all fields populated
        Challenge challenge = challengeRepository.save(Challenge.builder()
                .title("Complete Challenge")
                .challengeCode("CHL001")
                .slug("complete-challenge")
                .content("Complete content")
                .thumbnail("thumbnail.jpg")
                .goal(BigDecimal.valueOf(100000))
                .createdBy(testAccount)
                .createdAt(LocalDateTime.now())
                .finishedAt(LocalDate.now().plusDays(30))
                .build());

        // Act
        PageResponse<?> response = challengeService.getChallenges(
                0, 5, null, null, null, null);

        // Assert
        assertResponseWithCompleteFields(response, challenge, testAccount);
    }

    private void assertResponseWithCompleteFields(PageResponse<?> response, Challenge expectedChallenge, Account expectedAccount) {
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getContent(), "Content should not be null");
        assertFalse(response.getContent().isEmpty(), "Should have at least one challenge");

        ChallengeResponseDTO dto = ((List<ChallengeResponseDTO>) response.getContent()).get(0);

        // Verify challenge fields
        assertEquals(expectedChallenge.getChallengeId(), dto.getChallengeId(), "Challenge ID should match");
        assertEquals(expectedChallenge.getChallengeCode(), dto.getChallengeCode(), "Challenge code should match");
        assertEquals(expectedChallenge.getTitle(), dto.getTitle(), "Title should match");
        assertEquals(expectedChallenge.getSlug(), dto.getSlug(), "Slug should match");
        assertEquals(expectedChallenge.getThumbnail(), dto.getThumbnail(), "Thumbnail should match");
        assertEquals(expectedChallenge.getContent(), dto.getContent(), "Content should match");
        // Use compareTo for BigDecimal comparison to ignore scale differences
        assertEquals(0, expectedChallenge.getGoal().compareTo(dto.getGoal()), "Goal should match");
        // Allow for small differences in timestamp precision (up to 1 second)
        assertTrue(Math.abs(Duration.between(expectedChallenge.getCreatedAt(), dto.getCreatedAt()).toMillis()) <= 1000,
            "Created at times should be within 1 second of each other");
        assertEquals(expectedChallenge.getFinishedAt(), dto.getFinishedAt(), "Finished at should match");

        // Verify account fields
        assertNotNull(dto.getCreatedBy(), "Created by should not be null");
        assertEquals(expectedAccount.getAccountId(), dto.getCreatedBy().getAccountId(), "Account ID should match");
        assertEquals(expectedAccount.getCode(), dto.getCreatedBy().getCode(), "Account code should match");
        assertEquals(expectedAccount.getFullname(), dto.getCreatedBy().getFullname(), "Fullname should match");
        assertEquals(expectedAccount.getAvatar(), dto.getCreatedBy().getAvatar(), "Avatar should match");
    }

}

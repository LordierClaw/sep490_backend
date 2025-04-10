package vn.com.fpt.sep490_g28_summer2024_be.web.rest.project;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.budget.BudgetRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.budget.BudgetResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.expense.ExpenseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectUpdateRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.sponsor.SponsorRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.sponsor.SponsorUpdateRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.tracking.TrackingDTO;

import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;

import vn.com.fpt.sep490_g28_summer2024_be.service.assign.AssignService;
import vn.com.fpt.sep490_g28_summer2024_be.service.budget.BudgetService;
import vn.com.fpt.sep490_g28_summer2024_be.service.donation.DonationService;
import vn.com.fpt.sep490_g28_summer2024_be.service.expense.ExpenseService;
import vn.com.fpt.sep490_g28_summer2024_be.service.project.ProjectService;
import vn.com.fpt.sep490_g28_summer2024_be.service.sponsor.SponsorService;
import vn.com.fpt.sep490_g28_summer2024_be.service.tracking.TrackingService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/admin/projects")
@RequiredArgsConstructor
public class ProjectRest {

    private final ProjectService projectService;
    private final TrackingService trackingService;
    private final BudgetService budgetService;
    private final DonationService donationService;
    private final ExpenseService expenseService;
    private final AssignService assignService;
    private final SponsorService sponsorService;

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER','ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> viewListProjects(@RequestParam(defaultValue = "0", required = false) Integer page,
                                           @RequestParam(defaultValue = "10", required = false) Integer size,
                                           @RequestParam(required = false) String title,
                                           @RequestParam(value = "campaign_id", required = false) BigInteger campaignId,
                                           @RequestParam(required = false) String year,
                                           @RequestParam(required = false) String province,
                                           @RequestParam(required = false) Integer status) {
        return ApiResponse.builder()
                .code("200")
                .message("Danh sách dự án!")
                .data(projectService.viewByFilter(page, size, title, campaignId, status, province, year))
                .build();
    }
    @GetMapping("/is-assigned")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_MANAGER')")
    public ApiResponse<?> viewProjectByAccountId(
                                                 @RequestParam(defaultValue = "0", required = false) Integer page,
                                                 @RequestParam(defaultValue = "10", required = false) Integer size,
                                                 @AuthenticationPrincipal CustomAccountDetails userDetails,
                                                 @RequestParam(required = false) String title,
                                                 @RequestParam(value = "campaign_id", required = false) BigInteger campaignId,
                                                 @RequestParam(required = false) String year,
                                                 @RequestParam(required = false) String province,
                                                 @RequestParam(required = false) Integer status) {
        return ApiResponse.builder()
                .code("200")
                .message("Danh sách dự án tham gia")
                .data(projectService.viewProjectsByAccountId(page,size,userDetails.getUsername(),title,campaignId,status,province,year))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> getProjectById(@PathVariable BigInteger id) {
        return ApiResponse.builder()
                .code("200")
                .message("Chi tiết dự án")
                .data(projectService.getProjectById(id))
                .build();
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ApiResponse<?> updateProject(@RequestPart(value = "request") @Valid ProjectUpdateRequestDTO request,
                                        @RequestPart(value = "images", required = false) MultipartFile[] images,
                                        @RequestPart(value = "files", required = false) MultipartFile[] files,
                                        @PathVariable BigInteger id) {
        return ApiResponse.builder()
                .code(ErrorCode.HTTP_OK.getCode())
                .message(ErrorCode.HTTP_OK.getMessage())
                .data(projectService.updateProject(request, id, images, files))
                .build();
    }

    @PutMapping("/update/{id}/{status}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ApiResponse<?> updateProjectStatus(@PathVariable(value = "id") BigInteger id,
                                              @PathVariable(value = "status") Integer status) {
        return ApiResponse.builder()
                .code(ErrorCode.HTTP_OK.getCode())
                .message(ErrorCode.HTTP_OK.getMessage())
                .data(projectService.updateProjectStatus(id, status))
                .build();
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ApiResponse<?> createProject(@RequestPart @Valid ProjectRequestDTO request,
                                        @RequestPart(value = "project_images", required = false) MultipartFile[] images,
                                        @RequestPart(value = "project_files", required = false) MultipartFile[] files) {
        return ApiResponse.builder()
                .code("200")
                .message("Create project successfully!")
                .data(projectService.addProject(request, images, files))
                .build();
    }

    // Tracking Manage

    @PostMapping(value = "{id}/tracking/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ApiResponse<TrackingDTO> createTracking(
            @PathVariable("id") BigInteger projectId,
            @RequestPart("tracking") @Valid TrackingDTO trackingDTO,
            @RequestPart(value = "image", required = false) MultipartFile[] images) {

        ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                .projectId(projectId)
                .build();
        trackingDTO.setProject(projectResponseDTO);

        TrackingDTO trackingResponseDTO = trackingService.addTracking(trackingDTO, images);
        return ApiResponse.<TrackingDTO>builder()
                .code("200")
                .message("Tracking added successfully")
                .data(trackingResponseDTO)
                .build();
    }

    @GetMapping("/tracking/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<TrackingDTO> getTrackingById(@PathVariable BigInteger id) {
        TrackingDTO trackingResponseDTO = trackingService.getTrackingById(id);
        return ApiResponse.<TrackingDTO>builder()
                .code("200")
                .message("OK")
                .data(trackingResponseDTO)
                .build();
    }

    @GetMapping("{id}/trackings")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> viewTrackingByFilter(
            @PathVariable("id") BigInteger projectId,
            @RequestParam(defaultValue = "0", required = false) Integer page,
            @RequestParam(defaultValue = "10", required = false) Integer size,
            @RequestParam(required = false) String title) {

        PageResponse<TrackingDTO> pageResponse = trackingService.viewByFilter(page, size, title, projectId);
        return ApiResponse.builder()
                .code("200")
                .message("List Tracking successfully")
                .data(pageResponse)
                .build();
    }

    @PutMapping(value = "/tracking/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ApiResponse<TrackingDTO> updateTracking(@RequestPart("tracking") TrackingDTO trackingDTO,
                                                   @RequestPart(value = "image", required = false) MultipartFile[] images,
                                                   @PathVariable BigInteger id) {
        TrackingDTO trackingResponseDTO = trackingService.updateTracking(trackingDTO, id, images);
        return ApiResponse.<TrackingDTO>builder()
                .code("200")
                .message("Tracking updated successfully")
                .data(trackingResponseDTO)
                .build();
    }


    @DeleteMapping("/tracking/delete/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ApiResponse<?> deleteTracking(@PathVariable BigInteger id) {
        trackingService.deleteTracking(id);
        return ApiResponse.builder()
                .code("200")
                .message("Xóa tiến độ theo dõi thành công")
                .build();
    }
    //Expense Manage
    @PostMapping(value = "/{id}/expense/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ApiResponse<ExpenseDTO> createExpense(
            @PathVariable("id") BigInteger projectId,
            @RequestPart("expense") @Valid ExpenseDTO expenseDTO,
            @RequestPart(value = "file", required = false) MultipartFile[] files) {

        ProjectResponseDTO projectResponseDTO = ProjectResponseDTO.builder()
                .projectId(projectId)
                .build();

        expenseDTO.setProject(projectResponseDTO);
        ExpenseDTO expenseResponseDTO = expenseService.addExpense(expenseDTO, files);
        return ApiResponse.<ExpenseDTO>builder()
                .code("200")
                .message("Expense added successfully")
                .data(expenseResponseDTO)
                .build();
    }

    @GetMapping("/expense/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<ExpenseDTO> getExpenseById(@PathVariable BigInteger id) {
        ExpenseDTO expenseResponseDTO = expenseService.getExpenseById(id);
        return ApiResponse.<ExpenseDTO>builder()
                .code("200")
                .message("OK")
                .data(expenseResponseDTO)
                .build();
    }

    @GetMapping("/{id}/expenses")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> viewExpenseByFilter(@PathVariable("id") BigInteger projectId,
                                              @RequestParam(defaultValue = "0", required = false) Integer page,
                                              @RequestParam(defaultValue = "10", required = false) Integer size,
                                              @RequestParam(required = false) String title) {
        return ApiResponse.builder()
                .code("200")
                .message("List Expenses successfully")
                .data(expenseService.viewByFilter(page, size, title, projectId))
                .build();
    }


    @PutMapping(value = "expense/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ApiResponse<ExpenseDTO> updateExpense(@RequestPart("expense") ExpenseDTO expenseDTO,
                                                 @RequestPart(value = "file", required = false) MultipartFile[] files,
                                                 @PathVariable BigInteger id) {
        ExpenseDTO expenseResponseDTO = expenseService.updateExpense(expenseDTO, id, files);
        return ApiResponse.<ExpenseDTO>builder()
                .code("200")
                .message("Expense updated successfully")
                .data(expenseResponseDTO)
                .build();
    }

    @DeleteMapping("/expense/delete/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ApiResponse<?> deleteExpense(@PathVariable BigInteger id) {
        expenseService.deleteExpense(id);
        return ApiResponse.builder()
                .code("200")
                .message("Expense deleted successfully")
                .build();
    }

    // Assgin Manage
    @GetMapping(value = "/{id}/members")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> viewMembersByFilter(@PathVariable(name = "id") BigInteger projectId,
                                              @RequestParam(defaultValue = "0", required = false) Integer page,
                                              @RequestParam(defaultValue = "10", required = false) Integer size,
                                              @RequestParam(required = false) String email,
                                              @RequestParam(required = false) String fullname,
                                              @RequestParam(required = false) BigInteger roleId) {
        return ApiResponse.builder()
                .code(ErrorCode.HTTP_OK.getCode())
                .message("Danh sách thành viên tham gia dự án")
                .data(assignService.viewMemberInProjectByFilter(page, size, projectId, roleId, email, fullname))
                .build();
    }


    @PostMapping(value = "/{id}/members/add")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ApiResponse<?> addMembersToProject(@PathVariable("id") BigInteger projectId,
                                              @RequestBody List<BigInteger> accountIds) {
        assignService.addMembersToProject(accountIds, projectId);
        return ApiResponse.<List<AccountDTO>>builder()
                .code("200")
                .message("Thêm thành viên vào dự án thành công")
                .build();
    }

    @DeleteMapping(value = "/members/remove/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ApiResponse<?> removeMemberFromProject(@PathVariable("id") BigInteger assignId) {
        assignService.removeMember(assignId);
        return ApiResponse.<String>builder()
                .code("200")
                .message("Xóa thành viên khỏi dự án thành công")
                .build();

    }

    @GetMapping(value = "/{id}/members/not-assigned")
    public ApiResponse<List<AccountDTO>> getMembersNotAssignedToProject(@PathVariable(name = "id") BigInteger projectId) {
        List<AccountDTO> members = assignService.viewMembersNotAssignedToProject(projectId);
        return ApiResponse.<List<AccountDTO>>builder()
                .code("200")
                .message("Danh sách thành viên chưa được gán vào dự án")
                .data(members)
                .build();
    }


    // Budget Manage
    @GetMapping(value = "/{id}/budgets")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> viewBudgetByFilter(@PathVariable(name = "id") BigInteger id,
                                             @RequestParam(defaultValue = "0", required = false) Integer page,
                                             @RequestParam(defaultValue = "10", required = false) Integer size,
                                             @RequestParam(name = "title", required = false) String title) {
        return ApiResponse.builder()
                .code(ErrorCode.HTTP_OK.getCode())
                .message(ErrorCode.HTTP_OK.getMessage())
                .data(budgetService.viewBudgetByFilter(page, size, title, id))
                .build();
    }

    @PostMapping(value = "/{id}/budget/add")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ApiResponse<?> addBudgets(@RequestBody @Valid List<BudgetRequestDTO> budgetRequestDTOs, @PathVariable(name = "id") BigInteger projectId) {
        return ApiResponse.builder()
                .code(ErrorCode.HTTP_OK.getCode())
                .message(ErrorCode.HTTP_OK.getMessage())
                .data(budgetService.addBudgetsToProject(budgetRequestDTOs, projectId))
                .build();
    }

    @DeleteMapping(value = "/budget/delete/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ApiResponse<?> deleteBudget(@PathVariable BigInteger id) {
        budgetService.deleteBudget(id);
        return ApiResponse.builder()
                .code(ErrorCode.HTTP_OK.getCode())
                .message("Xóa ngân sách thành công")
                .build();
    }

    @PutMapping(value = "/budget/update/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ApiResponse<?> updateBudget(@PathVariable BigInteger id, @RequestBody BudgetRequestDTO budgetRequestDTO) {
        BudgetResponseDTO updatedBudget = budgetService.updateBudget(id, budgetRequestDTO);
        return ApiResponse.builder()
                .code(ErrorCode.HTTP_OK.getCode())
                .message("Ngân sách cập nhật thành công")
                .data(updatedBudget)
                .build();
    }
    @GetMapping("/budget/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<BudgetResponseDTO> getBudgetById(@PathVariable BigInteger id) {
        BudgetResponseDTO budgetResponseDTO = budgetService.getBudgetById(id);
        return ApiResponse.<BudgetResponseDTO>builder()
                .code("200")
                .message("OK")
                .data(budgetResponseDTO)
                .build();
    }


    // Sponsor manage
    @PostMapping(value = "/{id}/sponsors/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ApiResponse<?> add(@RequestPart(value = "request") @Valid SponsorRequestDTO request,
                              @RequestPart(required = true) MultipartFile contract,
                              @RequestPart(required = false) MultipartFile logo,
                              @PathVariable(name = "id") BigInteger id) {
        return ApiResponse.builder()
                .code(ErrorCode.HTTP_OK.getCode())
                .message(ErrorCode.HTTP_OK.getMessage())
                .data(sponsorService.addSponsorToProject(request, id, contract, logo))
                .build();
    }

    @PutMapping(value = "sponsors/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER')")
    public ApiResponse<?> updateSponsor(@RequestPart("request") SponsorUpdateRequestDTO requestDTO,
                                        @RequestPart(value = "contract", required = false) MultipartFile file,
                                        @RequestPart(value = "logo", required = false) MultipartFile logo,
                                        @PathVariable BigInteger id) {
        return ApiResponse.builder()
                .code(ErrorCode.HTTP_OK.getCode())
                .message(ErrorCode.HTTP_OK.getMessage())
                .data(sponsorService.update(id, requestDTO, file, logo))
                .build();
    }


    @GetMapping(value = "/{id}/sponsors")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> viewByFilter(@PathVariable(name = "id") BigInteger id,
                                       @RequestParam(defaultValue = "0", required = false) Integer page,
                                       @RequestParam(defaultValue = "10", required = false) Integer size,
                                       @RequestParam(name = "company_name", required = false) String companyName) {
        return ApiResponse.builder()
                .code(ErrorCode.HTTP_OK.getCode())
                .message(ErrorCode.HTTP_OK.getMessage())
                .data(sponsorService.viewListSponsorInProject(page, size, companyName, id))
                .build();
    }

    @GetMapping("/sponsors/{sponsor}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> viewDetail(@PathVariable(name = "sponsor") BigInteger sponsorId) {
        return ApiResponse.builder()
                .code(ErrorCode.HTTP_OK.getCode())
                .message(ErrorCode.HTTP_OK.getMessage())
                .data(sponsorService.viewDetail(sponsorId))
                .build();
    }
    // donation
    @GetMapping("/{id}/donations")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROJECT_MANAGER', 'ROLE_SOCIAL_STAFF')")
    public ApiResponse<?> viewListDonationByProjectId(@RequestParam(defaultValue = "0", required = false) Integer page,
                                                      @RequestParam(defaultValue = "10", required = false) Integer size,
                                                      @RequestParam(required = false) String description,
                                                      @PathVariable BigInteger id
    ) {
        return ApiResponse.builder()
                .code("200")
                .message("Danh sách donate")
                .data(donationService.viewListDonationsAdmin(page,size,id,description))
                .build();
    }
}

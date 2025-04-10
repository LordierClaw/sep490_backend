package vn.com.fpt.sep490_g28_summer2024_be.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    //Invalid Status Code(1000-2000)
    INVALID_EMAIL("1001", "Email không tồn tại!"),
    EXIST_EMAIL("1002", "Email đã đăng ký tài khoản!"),
    INVALID_IMAGE_FORMAT("1016","File tải lên không phải ảnh"),
    INVALID_KEY("1003", "Uncategorized error"),
    UPLOAD_FAILED("1016","Failed to upload file"),
    DELETE_FILE_FAILED("1017","Failed to delete file"),
    USER_NOT_EXISTED("1004", "Tài khoản không tồn tại"),
    CAMPAIGN_NOT_EXISTED("1010", "Chiến dịch không tồn tại"),
    CAMPAIGN_NO_CONTENT("1012", "Không tìm thấy chiến dịch nào"),
    PROJECT_NOT_EXISTED("1015", "Dự án không tồn tại"),
    PROJECT_NO_CONTENT("1014", "Không tìm thấy dự án nào"),
    DUPLICATE_TITLE("1013", "Tiêu đề đã tồn tại"),
    USER_EXISTED("1005", "Tài khoản tồn tại"),
    ACCOUNT_NO_CONTENT("1011", "Không tìm thấy tài khoản nào"),
    INVALID_PASSWORD("1006", "Mật khẩu không đúng"),
    HTTP_OTP_INVALID("1007","OTP INVALID"),
    HTTP_USER_NOT_ACTIVE("1008","TÀI KHOẢN ĐÃ BỊ VÔ HIỆU HÓA"),
    HTTP_NEWS_NOT_EXISTED("1009","TIN TỨC KHÔNG TỒN TẠI"),
    HTTP_FILE_IS_NOT_IMAGE("1010","FILE UPLOAD KHÔNG PHẢI LÀ ĐỊNH DẠNG ẢNH"),
    HTTP_TRACKING_NOT_FOUND("1020","Tracking not found"),
    HTTP_IMAGE_NOT_FOUND("1021","Image not founđ"),
    HTTP_FILE_NOT_FOUND("1022","File not found"),
    FILE_TOO_LARGE("1023","FILE_TOO_LARGE"),
    INVALID_FILE_TYPE("1024","INVALID_FILE_TYPE"),
    ROLE_NOT_EXISTED("1021", "Role không tồn tại"),
    OLD_PASSWORD_INCORRECT("1025","Mật khẩu cũ không đúng"),
    SPONSOR_NOT_EXIST("1026","Nhà tài trợ không tồn tại"),
    INVALID_FILE_SIZE("1026","File size exceeds the maximum limit of 10MB."),
    INVALID_IMAGE_SIZE("1027","Image size exceeds the maximum limit of 2MB."),
    EXPORT_PDF_FAILED("1028","Xuất file pdf failed"),
    EXPORT_EXCEL_FAILED("1029","Xuất file excel failed "),
    PROJECT_CONSTRUCTION_CONFLICT("1030","Ít nhất phải có một giá trị"),
    MEMBER_ALREADY_ASSIGNED("1031","Thành viên đã có trong dự án"),
    ROLE_MEMBER_NOT_VALID("1032","Thành viên không được tham gia dự án"),
    MEMBER_NOT_FOUND_IN_PROJECT("1033","Không có thành viên này trong dự án"),
    BUDGET_NOT_FOUND("1034","Không tìm thấy Budget "),
    CHALLENGE_NOT_FOUND("1036","Không tìm thấy challenge"),
    EXPENSE_NOT_FOUND("1035","Không tìm thấy chi phí "),
    CHALLENGE_ALREADY_FINISHED("1036","Thử thách đã hết hạn"),
    INVALID_FINISH_DATE("1037","Ngày kết thúc phải là ngày hợp lệ"),
    NEW_PASSWORD_MUST_BE_DIFFERENT("1038","Mật khẩu mới phải khác mật khẩu cũ !"),
    CATEGORY_OF_NEWS_MUST_BE_ACTIVE("1039","Danh mục tin tức đang bị ẩn nên không thể thay đổi trạng thái"),
    CONTRACT_NOT_NULL("1040","Không được để trống hợp đồng"),



    //Http Status Code
    HTTP_OK("200", "Success"),
    HTTP_CREATED("201", "Created"),
    HTTP_ACCEPTED("202", "Accepted"),
    HTTP_NO_CONTENT("204", "No Content"),

    // Redirection messages (300–399)
    HTTP_MOVED_PERMANENTLY("301", "Moved Permanently"),
    HTTP_FOUND("302", "Found"),
    HTTP_NOT_MODIFIED("304", "Not Modified"),
    HTTP_TEMPORARY_REDIRECT("307", "Temporary Redirect"),

    // Client error responses (400–499)
    HTTP_BAD_REQUEST("400", "Bad Request"),
    HTTP_UNAUTHORIZED("401", "Unauthorized"),
    HTTP_FORBIDDEN("403", "Forbidden"),
    HTTP_NOT_FOUND("404", "Not Found"),
    HTTP_METHOD_NOT_ALLOWED("405", "Method Not Allowed"),
    HTTP_REQUEST_TIMEOUT("408", "Request Timeout"),
    ADMIN_ACCESS_DENIED("430", "Người dùng có vai trò admin không được phép truy cập tài nguyên này"),
    ACCESS_DENIED("430", "Không được phép truy cập tài nguyên này"),
    HTTP_CONFLICT("409", "Conflict"),
    HTTP_PAYLOAD_TOO_LARGE("413", "Payload Too Large"),
    HTTP_SEND_EMAIL_FAILED("414", "Send email failed!"),
    FILE_SIZE_EXCEEDS_LIMIT("400","File aảnh không quá 2MB"),
    HTTP_UNSUPPORTED_MEDIA_TYPE("415", "Unsupported Media Type"),
    HTTP_TOO_MANY_REQUESTS("429", "Too Many Requests"),
    UNCATEGORIZED_EXCEPTION("499","Unclassified error"),
    HTTP_FETCH_FAILED("430","Fetch api failed!"),
    HTTP_MAPPING_FAILED("431","Mapping failed!"),
    HTTP_SIGNING_FAILED("498","Signing failed");


    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}


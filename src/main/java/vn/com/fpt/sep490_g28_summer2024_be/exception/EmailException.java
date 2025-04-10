package vn.com.fpt.sep490_g28_summer2024_be.exception;

import jakarta.mail.MessagingException;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;

public class EmailException extends MessagingException {
    public EmailException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    private ErrorCode errorCode;

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}

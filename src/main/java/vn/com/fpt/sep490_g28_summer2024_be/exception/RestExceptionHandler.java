package vn.com.fpt.sep490_g28_summer2024_be.exception;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ErrorApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.exception.ErrorDTO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler{
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ErrorApiResponse> handlingRuntimeException(Exception exception){
        log.error("Exception: ", exception);

        return ResponseEntity.badRequest().body(ErrorApiResponse.<ErrorDTO>builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .message(ErrorCode.HTTP_BAD_REQUEST.getMessage())
                .error(ErrorDTO.builder()
                        .message(exception.getMessage())
                        .build())
                .build());
    }

    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ErrorApiResponse> handlingRuntimeException(RuntimeException exception){
        log.error("Exception: ", exception);

        return ResponseEntity.badRequest().body(ErrorApiResponse.<ErrorDTO>builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .message(ErrorCode.HTTP_BAD_REQUEST.getMessage())
                .error(ErrorDTO.builder()
                        .message(exception.getMessage())
                        .build())
                .build());
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ErrorApiResponse> handlingAppException(AppException exception){
        log.error("Exception: ", exception);

        return ResponseEntity.badRequest().body(ErrorApiResponse.<ErrorDTO>builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .message(ErrorCode.HTTP_BAD_REQUEST.getMessage())
                .error(ErrorDTO.builder()
                        .code(exception.getErrorCode().getCode())
                        .message(exception.getErrorCode().getMessage())
                        .build())
                .build());
    }


    @ExceptionHandler(value = MessagingException.class)
    ResponseEntity<ErrorApiResponse> handlingEmailException(MessagingException exception){
        log.error("Exception: ", exception);

        return ResponseEntity.badRequest().body(ErrorApiResponse.<ErrorDTO>builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .message(ErrorCode.HTTP_SIGNING_FAILED.getMessage())
                .error(ErrorDTO.builder()
                        .code("407")
                        .message(exception.getMessage())
                        .build())
                .build());
    }
    @ExceptionHandler(value = IOException.class)
    ResponseEntity<ErrorApiResponse> handlingEmailException(IOException exception){
        log.error("Exception: ", exception);
        return ResponseEntity.badRequest().body(ErrorApiResponse.<ErrorDTO>builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .message(ErrorCode.HTTP_SIGNING_FAILED.getMessage())
                .error(ErrorDTO.builder()
                        .code("407")
                        .message(exception.getMessage())
                        .build())
                .build());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ErrorApiResponse<Map<String, String>>> handlingValidation(MethodArgumentNotValidException exception){

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult().getAllErrors().forEach(objectError -> {
            String fieldName = ((FieldError) objectError).getField();
            String errorMessage = objectError.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(ErrorApiResponse.<Map<String, String>>builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .message("fail!")
                .error(errors)
                .build());
    }

}


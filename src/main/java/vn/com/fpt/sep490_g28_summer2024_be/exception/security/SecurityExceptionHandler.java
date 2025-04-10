package vn.com.fpt.sep490_g28_summer2024_be.exception.security;

import com.nimbusds.jose.JOSEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ErrorApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.exception.ErrorDTO;


@RestControllerAdvice
@Slf4j
public class SecurityExceptionHandler {

    @ExceptionHandler(value = JOSEException.class)
    ResponseEntity<ErrorApiResponse> handlingJOSEException(JOSEException exception){
        log.error("Exception: ", exception);

        return ResponseEntity.badRequest().body(ErrorApiResponse.<ErrorDTO>builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .message(ErrorCode.HTTP_SIGNING_FAILED.getMessage())
                .error(ErrorDTO.builder()
                        .code("405")
                        .message(exception.getMessage())
                        .build())
                .build());
    }

    @ExceptionHandler(value = TokenException.class)
    ResponseEntity<ErrorApiResponse> handlingParseException(TokenException exception){
        log.error("Exception: ", exception);

        return ResponseEntity.badRequest().body(ErrorApiResponse.<ErrorDTO>builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .message(ErrorCode.HTTP_SIGNING_FAILED.getMessage())
                .error(ErrorDTO.builder()
                        .code("405")
                        .message(exception.getMessage())
                        .build())
                .build());
    }

    @ExceptionHandler(value = ParsingException.class)
    ResponseEntity<ErrorApiResponse> handlingParseException(ParsingException exception){
        log.error("Exception: ", exception);

        return ResponseEntity.badRequest().body(ErrorApiResponse.<ErrorDTO>builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .message(ErrorCode.HTTP_SIGNING_FAILED.getMessage())
                .error(ErrorDTO.builder()
                        .code("405")
                        .message(exception.getMessage())
                        .build())
                .build());
    }

}

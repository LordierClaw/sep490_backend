package vn.com.fpt.sep490_g28_summer2024_be.web.rest.authentication;

import com.nimbusds.jose.JOSEException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.fpt.sep490_g28_summer2024_be.dto.ApiResponse;
import vn.com.fpt.sep490_g28_summer2024_be.dto.authentication.*;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountForgotPasswordDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountRegisterDTO;
import vn.com.fpt.sep490_g28_summer2024_be.service.account.AccountService;
import vn.com.fpt.sep490_g28_summer2024_be.service.authentication.AuthenticationService;
import vn.com.fpt.sep490_g28_summer2024_be.utils.Email;

import java.text.ParseException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationRest {

    private final AuthenticationService authenticationService;
    private final AccountService accountService;

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody @Valid AuthenticationRequest request) throws Exception {
        return ApiResponse.<AuthenticationResponse>builder()
                .data(authenticationService.authenticate(request))
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<?> introspect(@RequestBody IntrospectRequest request) throws Exception {
        return ApiResponse.<IntrospectResponse>builder()
                .data(authenticationService.introspect(request))
                .build();
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody AccountRegisterDTO request){
        return ApiResponse.builder()
                .data(accountService.register(request))
                .build();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<?> forgot(@RequestBody AccountForgotPasswordDTO request) throws MessagingException {
        return ApiResponse.builder()
                .data(accountService.forgot(request))
                .build();
    }

    @PostMapping("/send-otp")
    public ApiResponse<?> sendOtp(@RequestBody Email request){
        System.out.println(request);
        return ApiResponse.builder()
                .data(accountService.sendOtp(request))
                .build();
    }

    @PostMapping("/refresh-token")
    public ApiResponse<?> refreshToken(@RequestBody RefreshTokenRequest request, HttpServletRequest httpServletRequest) throws ParseException, JOSEException {
        return ApiResponse.builder()
                .data(authenticationService.refreshToken(request, httpServletRequest))
                .build();
    }

}

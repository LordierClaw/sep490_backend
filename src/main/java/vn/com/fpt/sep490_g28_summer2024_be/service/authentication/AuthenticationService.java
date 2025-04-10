package vn.com.fpt.sep490_g28_summer2024_be.service.authentication;

import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import vn.com.fpt.sep490_g28_summer2024_be.dto.authentication.*;

import java.text.ParseException;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request) throws Exception;
    IntrospectResponse introspect(IntrospectRequest request) throws Exception;
    AuthenticationResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpServletRequest) throws ParseException, JOSEException;
}

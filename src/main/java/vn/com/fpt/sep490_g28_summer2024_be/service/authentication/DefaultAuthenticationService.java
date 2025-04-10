package vn.com.fpt.sep490_g28_summer2024_be.service.authentication;

import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import vn.com.fpt.sep490_g28_summer2024_be.common.AppConfig;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.authentication.*;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.CustomAccountDetails;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.JwtTokenGenerator;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.JwtTokenUtils;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultAuthenticationService implements AuthenticationService{
    private final AccountRepository accountRepository;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) throws Exception {
        var account = accountRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(AppConfig.BCRYPT_PASSWORD_ENCODER);
        Boolean authenticated = passwordEncoder.matches(request.getPassword(),
                account.getPassword());
        if(!authenticated){
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        if(!account.getIsActive()){
            throw new AppException(ErrorCode.HTTP_USER_NOT_ACTIVE);
        }

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomAccountDetails customUserDetails = (CustomAccountDetails) authentication.getPrincipal();
        System.out.println(new Date(System.currentTimeMillis() + jwtTokenUtils.getJWT_EXPIRATION()));
        return AuthenticationResponse.builder()
                .authenticated(authenticated)
                .code(account.getCode())
                .fullname(account.getFullname())
                .phoneNumber(account.getPhone())
                .token(jwtTokenGenerator.generateAccessToken(customUserDetails))
                .refreshToken(jwtTokenGenerator.generateRefreshToken(customUserDetails))
                .email(account.getEmail())
                .avatar(account.getAvatar())
                .role(customUserDetails.getAuthorities().stream().toList().get(0).toString())
                .expiryTime(new Date(System.currentTimeMillis() + jwtTokenUtils.getJWT_EXPIRATION()))
                .refreshExpiryTime(new Date(System.currentTimeMillis() + jwtTokenUtils.getJWT_REFRESH_EXPIRATION()))
                .build();
    }
    @Override
    public IntrospectResponse introspect(IntrospectRequest request) throws Exception {
        return IntrospectResponse.builder()
                .valid(jwtTokenUtils.validateToken(request.getToken()))
                .build();
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpServletRequest) throws ParseException, JOSEException {
        if(!jwtTokenUtils.validateRefreshToken(request.getToken())){
            throw new AppException(ErrorCode.HTTP_UNAUTHORIZED);
        }

        //build jwt decoder
        SecretKeySpec secretKeySpec = new SecretKeySpec(jwtTokenUtils.getJWT_REFRESH_SECRET().getBytes(), "HS512");
        var jwtDecoder =  NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();

        //decode refresh token
        final Jwt jwtToken = jwtDecoder.decode(request.getToken());

        //get userdetail from refresh token
        final String userName = jwtTokenUtils.getUserName(jwtToken);
        UserDetails userDetails = jwtTokenUtils.userDetails(userName);


        //add userdetail to SecurityContextHolder
        UsernamePasswordAuthenticationToken createdToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        createdToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
        SecurityContextHolder.getContext().setAuthentication(createdToken);

        CustomAccountDetails customAccountDetails = (CustomAccountDetails) userDetails;

        return AuthenticationResponse.builder()
                .email(customAccountDetails.getUsername())
                .role(customAccountDetails.getAuthorities().stream().toList().get(0).toString())
                .authenticated(true)
                .token(jwtTokenGenerator.generateAccessToken(customAccountDetails))
                .refreshToken(jwtTokenGenerator.generateRefreshToken(customAccountDetails))
                .expiryTime(new Date(System.currentTimeMillis() + jwtTokenUtils.getJWT_EXPIRATION()))
                .refreshExpiryTime(new Date(System.currentTimeMillis() + jwtTokenUtils.getJWT_REFRESH_EXPIRATION()))
                .build();
    }

}

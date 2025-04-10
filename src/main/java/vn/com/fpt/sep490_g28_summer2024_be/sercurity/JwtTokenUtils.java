package vn.com.fpt.sep490_g28_summer2024_be.sercurity;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.Data;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;

import java.text.ParseException;
import java.util.Date;

@Component
@Slf4j
@Data
public class JwtTokenUtils {
    @Value("${jwt.secret}")
    @NonFinal
    private String JWT_SECRET;

    @NonFinal
    @Value("${jwt.expiration}")
    private Long JWT_EXPIRATION;

    @NonFinal
    @Value("${jwt.refresh-secret}")
    private String JWT_REFRESH_SECRET;

    @NonFinal
    @Value("${jwt.refresh-expiration}")
    private Long JWT_REFRESH_EXPIRATION;

    private final AccountRepository accountRepository;
    private final CustomAccountDetailsService customAccountDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtils.class);

    public String getUserName(Jwt jwtToken){
        return jwtToken.getSubject();
    }
    public boolean validateToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(JWT_SECRET.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expiryTime == null) {
            logger.error("JWT claims string is empty: Unauthorized");
            throw new AppException(ErrorCode.HTTP_UNAUTHORIZED);
        }
        var verified = signedJWT.verify(verifier);
        if (!(verified && expiryTime.after(new Date())))
            throw new AppException(ErrorCode.HTTP_UNAUTHORIZED);

        return verified;
    }

    public boolean validateRefreshToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(JWT_REFRESH_SECRET.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expiryTime == null) {
            logger.error("JWT claims string is empty: Unauthorized");
            throw new AppException(ErrorCode.HTTP_UNAUTHORIZED);
        }
        var verified = signedJWT.verify(verifier);
        if (!(verified && expiryTime.after(new Date())))
            throw new AppException(ErrorCode.HTTP_UNAUTHORIZED);

        return verified;
    }

    public UserDetails userDetails(String emailId){
        return customAccountDetailsService.loadUserByUsername(emailId);
    }
}

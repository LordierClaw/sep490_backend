package vn.com.fpt.sep490_g28_summer2024_be.sercurity;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenGenerator {
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

    @NonFinal
    @Value("${jwt.issuer}")
    String ISSUER;

    private final PasswordEncoder passwordEncoder;
    public String generateAccessToken(CustomAccountDetails customAccountDetails) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        log.info(new Date(JWT_EXPIRATION).toString());

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(customAccountDetails.getUsername())
                .issuer(ISSUER)
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .jwtID(UUID.randomUUID().toString())
                .claim("user", customAccountDetails)
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        jwsObject.sign(new MACSigner(JWT_SECRET.getBytes()));

        return jwsObject.serialize();
    }



    public String generateRefreshToken(CustomAccountDetails customAccountDetails) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        log.info(new Date(JWT_REFRESH_EXPIRATION).toString());

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(customAccountDetails.getUsername())
                .issuer(ISSUER)
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + JWT_REFRESH_EXPIRATION))
                .jwtID(UUID.randomUUID().toString())
                .claim("user", customAccountDetails)
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        jwsObject.sign(new MACSigner(JWT_REFRESH_SECRET.getBytes()));

        return jwsObject.serialize();
    }

    private static String getRolesOfUser(CustomAccountDetails customAccountDetails) {
        return customAccountDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
    }

}

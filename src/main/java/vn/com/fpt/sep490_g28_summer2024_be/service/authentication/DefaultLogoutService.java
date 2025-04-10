package vn.com.fpt.sep490_g28_summer2024_be.service.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import vn.com.fpt.sep490_g28_summer2024_be.sercurity.JwtTokenUtils;

import javax.crypto.spec.SecretKeySpec;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultLogoutService implements LogoutHandler {
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String jwt = bearerToken.substring(7);

            SecretKeySpec secretKeySpec = new SecretKeySpec(jwtTokenUtils.getJWT_SECRET().getBytes(), "HS512");
            var jwtDecoder =  NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
            Jwt jwtToken = jwtDecoder.decode(jwt);

            String username = jwtTokenUtils.getUserName(jwtToken);

            if (username != null) {
                SecurityContextHolder.clearContext();
            }
        }
    }
}

package vn.com.fpt.sep490_g28_summer2024_be.sercurity;

import com.nimbusds.jose.JOSEException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.com.fpt.sep490_g28_summer2024_be.exception.AppException;
import vn.com.fpt.sep490_g28_summer2024_be.exception.security.ParsingException;
import vn.com.fpt.sep490_g28_summer2024_be.exception.security.TokenException;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.text.ParseException;

@RequiredArgsConstructor
@Slf4j
public class JwtAccessTokenFilter extends OncePerRequestFilter {
    private final JwtTokenUtils jwtTokenUtils;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, JwtValidationException {
        try{
            log.info("[JwtAccessTokenFilter:doFilterInternal] :: Started ");

            log.info("[JwtAccessTokenFilter:doFilterInternal]Filtering the Http Request:{}",request.getRequestURI());

            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            log.info("[JwtAccessTokenFilter:doFilterInternal] :: Started "+authHeader);
            SecretKeySpec secretKeySpec = new SecretKeySpec(jwtTokenUtils.getJWT_SECRET().getBytes(), "HS512");
            var jwtDecoder =  NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();

            if(authHeader == null || !authHeader.startsWith("Bearer")){
                filterChain.doFilter(request,response);
                return;
            }

            String token = authHeader.substring(7);
            System.out.println(token);
            Jwt jwtToken = jwtDecoder.decode(token);


            String userName = jwtTokenUtils.getUserName(jwtToken);

            //If not authentication
            if(!userName.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = jwtTokenUtils.userDetails(userName);
                if(jwtTokenUtils.validateToken(token)){
                    UsernamePasswordAuthenticationToken createdToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    createdToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(createdToken);
                }
            }
            log.info("[JwtAccessTokenFilter:doFilterInternal] Completed");
            filterChain.doFilter(request,response);
        }catch (AppException appException){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }catch (JwtValidationException jwtValidationException){
            log.error("[JwtAccessTokenFilter:doFilterInternal] Exception due to :{}",jwtValidationException.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (ParseException e) {
            throw new ParsingException(e.getMessage());
        } catch (JOSEException e) {
            throw new TokenException(e.getMessage());
        }
    }
}

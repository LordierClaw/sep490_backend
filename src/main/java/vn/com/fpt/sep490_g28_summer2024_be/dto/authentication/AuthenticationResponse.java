package vn.com.fpt.sep490_g28_summer2024_be.dto.authentication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AuthenticationResponse {
    private String token;
    private String refreshToken;
    private Boolean authenticated;
    private String email;
    private String fullname;
    private String phoneNumber;
    private String avatar;
    private String code;
    private String role;
    private Date expiryTime;
    private Date refreshExpiryTime;
}

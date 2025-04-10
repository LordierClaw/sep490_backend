package vn.com.fpt.sep490_g28_summer2024_be.sercurity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import vn.com.fpt.sep490_g28_summer2024_be.dto.authentication.UserResponse;

import java.util.Collection;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomAccountDetails implements OAuth2User, UserDetails {
    private UserResponse userResponse;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomAccountDetails(UserResponse userResponse) {
        this.userResponse = userResponse;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return userResponse.getPassword();
    }

    @Override
    public String getUsername() {
        return userResponse.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return userResponse.getIsActive();
    }


    @Override
    public String getName() {
        return userResponse.getFullname();
    }
}

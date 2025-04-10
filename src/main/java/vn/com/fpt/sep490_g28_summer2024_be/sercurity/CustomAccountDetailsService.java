package vn.com.fpt.sep490_g28_summer2024_be.sercurity;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.stereotype.Service;
import vn.com.fpt.sep490_g28_summer2024_be.common.ErrorCode;
import vn.com.fpt.sep490_g28_summer2024_be.dto.authentication.UserResponse;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Account;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Role;
import vn.com.fpt.sep490_g28_summer2024_be.repository.AccountRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CustomAccountDetailsService
        extends DefaultOAuth2UserService
        implements UserDetailsService {

    private final AccountRepository accountRepository;
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(ErrorCode.USER_NOT_EXISTED.getMessage()));
        ModelMapper modelMapper = new ModelMapper();
        UserResponse userResponse = modelMapper.map(account, UserResponse.class);
        userResponse.setScope(account.getRole().getRoleName());
        return CustomAccountDetails.builder()
                .userResponse(userResponse)
                .authorities(mapRolesToAuthorities(Collections.singletonList(account.getRole())))
                .build();
    }

    private Collection<GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(mapRole(role.getRoleName()))).collect(Collectors.toSet());
    }

    private String mapRole(String role){
        return "ROLE_"+String.join("_",role.split("\\s+")).toUpperCase();
    }
}

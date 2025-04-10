package vn.com.fpt.sep490_g28_summer2024_be.dto.authentication;

import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String email;
    String fullname;
    String avatar;
    String password;
    Boolean isActive;
    String scope;
}

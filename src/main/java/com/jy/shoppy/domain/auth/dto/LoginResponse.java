package com.jy.shoppy.domain.auth.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponse {
    Long userId;
    String email;
    String username;
    String role;
}

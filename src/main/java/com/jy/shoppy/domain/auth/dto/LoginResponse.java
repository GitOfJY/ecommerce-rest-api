package com.jy.shoppy.domain.auth.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

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
    String gradeName;
    BigDecimal discountRate;
}

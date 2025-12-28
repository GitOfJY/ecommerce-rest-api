package com.jy.shoppy.domain.auth.dto;

import com.jy.shoppy.domain.user.entity.type.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RegisterUserResponse {
    private Long id;
    private String username;
    private String email;
    private String gradeName;
    private BigDecimal discountRate;
    private UserStatus status;
}

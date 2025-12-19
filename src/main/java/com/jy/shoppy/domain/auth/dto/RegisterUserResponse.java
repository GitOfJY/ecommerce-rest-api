package com.jy.shoppy.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterUserResponse {
    private Long id;
    private String username;
    private String email;
    private String gradeName;
}

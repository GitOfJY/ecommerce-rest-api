package com.jy.shoppy.domain.auth;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.auth.dto.LoginResponse;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    default LoginResponse toLoginResponse(Account account) {
        String role = account.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse(null);

        return LoginResponse.builder()
                .userId(account.getAccountId())
                .email(account.getEmail())
                .username(account.getUsername())
                .role(role)
                .build();
    }
}

package com.jy.shoppy.domain.auth.mapper;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.auth.dto.RegisterUserResponse;
import com.jy.shoppy.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Mapper(componentModel = "spring")
public interface  AccountMapper {
    @Mapping(source = "userGrade.name", target = "gradeName")
    RegisterUserResponse toRegisterResponse(User user);

    default Account toAccount(User user) {
        String roleName = user.getRole() != null && user.getRole().getName() != null
                ? user.getRole().getName()
                : "ROLE_USER";  // 기본값

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(roleName)
        );

        return Account.builder()
                .accountId(user.getId())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .username(user.getUsername())
                .authorities(authorities)
                .gradeName(user.getUserGrade().getName())
                .discountRate(user.getUserGrade().getDiscountRate())
                .build();
    }
}

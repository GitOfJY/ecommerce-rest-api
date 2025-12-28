package com.jy.shoppy.domain.auth.mapper;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.auth.dto.RegisterUserResponse;
import com.jy.shoppy.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface  AccountMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "userGrade.name", target = "gradeName")
    @Mapping(source = "userGrade.discountRate", target = "discountRate")
    @Mapping(source = "status", target = "status")
    RegisterUserResponse toRegisterResponse(User user);

    default Account toAccount(User user) {
        // 기본값
        String roleName = user.getRole() != null && user.getRole().getName() != null
                ? user.getRole().getName()
                : "ROLE_USER";

        String gradeName = user.getUserGrade().getName() != null
                ? user.getUserGrade().getName()
                : "BRONZE";

        BigDecimal discountRate = user.getUserGrade().getName() != null
                ? user.getUserGrade().getDiscountRate()
                : BigDecimal.valueOf(0.00);

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(roleName)
        );

        return Account.builder()
                .accountId(user.getId())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .username(user.getUsername())
                .authorities(authorities)
                .gradeName(gradeName)
                .discountRate(discountRate)
                .status(user.getStatus())
                .build();
    }
}

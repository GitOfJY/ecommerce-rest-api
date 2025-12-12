package com.jy.shoppy.domain.auth.mapper;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Mapper(componentModel = "spring")
public class AccountMapper {
    public Account toAccount(User user) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().getName()));

        return Account.builder()
                .accountId(user.getId())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .username(user.getUsername())
                .authorities(authorities)
                .build();
    }
}

package com.jy.shoppy.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jy.shoppy.domain.user.entity.type.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account implements UserDetails {
    Long accountId;
    String email;
    String password;
    String username;

    String gradeName;
    BigDecimal discountRate;

    List<GrantedAuthority> authorities;

    UserStatus status;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Builder
    @JsonCreator
    public Account(
            @JsonProperty("accountId") Long accountId,
            @JsonProperty("email") String email,
            @JsonProperty("password") String password,
            @JsonProperty("username") String username,
            @JsonProperty("gradeName") String gradeName,
            @JsonProperty("discountRate") BigDecimal discountRate,
            @JsonProperty("status") UserStatus status,
            @JsonProperty("authorities") List<GrantedAuthority> authorities
    ) {
        this.accountId = accountId;
        this.email = email;
        this.password = password;
        this.username = username;
        this.gradeName = gradeName;
        this.discountRate = discountRate;
        this.status = status;
        this.authorities = authorities;
    }
}

package com.jy.shoppy.global.config.security.token;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal;
    private final Object credentials;

    // 인증 전 - Filter에서 생성
    public AuthenticationToken(Object principal, Object credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false);
    }

    // 인증 후 - Provider에서 생성
    public AuthenticationToken(@JsonProperty("principal") Object principal,
                               @JsonProperty("credentials") Object credentials,
                               @JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}

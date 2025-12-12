package com.jy.shoppy.global.config.security.provider;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.global.config.security.service.CustomUserDetailsService;
import com.jy.shoppy.global.config.security.token.AuthenticationToken;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component("restAuthenticationProvider")
@RequiredArgsConstructor
public class RestAuthenticationProvider implements AuthenticationProvider {
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = (String) authentication.getCredentials();

        Account account = (Account) userDetailsService.loadUserByUsername(email);
        if (!passwordEncoder.matches(password, account.getPassword())){
            throw new BadCredentialsException(ServiceExceptionCode.UNAUTHORIZED_PASSWORD.toString());
        }

        return new AuthenticationToken(account, null, account.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return AuthenticationToken.class.isAssignableFrom(authentication);
    }
}

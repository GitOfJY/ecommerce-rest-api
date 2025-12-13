package com.jy.shoppy.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jy.shoppy.domain.auth.dto.LoginRequest;
import com.jy.shoppy.global.security.token.AuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;

public class RestAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RestAuthenticationFilter(String defaultFilterProcessesUrl) {
        super(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, defaultFilterProcessesUrl));
    }

//    public SecurityContextRepository getSecurityContextRepository(HttpSecurity http) {
//        SecurityContextRepository securityContextRepository = http.getSharedObject(SecurityContextRepository.class);
//        if (securityContextRepository == null) {
//            securityContextRepository = new DelegatingSecurityContextRepository(
//                    new RequestAttributeSecurityContextRepository(), new HttpSessionSecurityContextRepository()
//            );
//        }
//        return securityContextRepository;
//    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException, IOException {
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            throw new IllegalArgumentException("HTTP method not supported: " + request.getMethod());
        }

        LoginRequest loginRequest = objectMapper.readValue(
                request.getReader(), LoginRequest.class
        );

        if (!StringUtils.hasText(loginRequest.getEmail()) ||
                !StringUtils.hasText(loginRequest.getPassword())) {
            throw new AuthenticationServiceException("Email or Password is empty");
        }

        AuthenticationToken token = new AuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword()
        );

        return getAuthenticationManager().authenticate(token);
    }
}

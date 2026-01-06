package com.jy.shoppy.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import com.jy.shoppy.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("restFailureHandler")
public class RestAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        ServiceExceptionCode errorCode;

        if (exception instanceof BadCredentialsException) {
            errorCode = ServiceExceptionCode.INVALID_CREDENTIALS;
        } else if (exception instanceof UsernameNotFoundException) {
            errorCode = ServiceExceptionCode.CANNOT_FOUND_USER;
        } else {
            errorCode = ServiceExceptionCode.AUTHENTICATION_FAILED;
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .result(false)
                .error(ApiResponse.Error.of(errorCode.name(), errorCode.getMessage()))
                .build();

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}

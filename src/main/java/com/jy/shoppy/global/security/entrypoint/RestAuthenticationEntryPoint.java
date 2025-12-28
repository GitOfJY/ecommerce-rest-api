package com.jy.shoppy.global.security.entrypoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import com.jy.shoppy.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ServiceExceptionCode errorCode = ServiceExceptionCode.AUTHENTICATION_REQUIRED;

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .result(false)
                .error(ApiResponse.Error.of(errorCode.name(), errorCode.getMessage()))
                .build();

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}

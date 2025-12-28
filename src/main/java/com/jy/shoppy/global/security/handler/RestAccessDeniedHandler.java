package com.jy.shoppy.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import com.jy.shoppy.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ServiceExceptionCode errorCode = ServiceExceptionCode.ACCESS_DENIED;

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .result(false)
                .error(ApiResponse.Error.of(errorCode.name(), errorCode.getMessage()))
                .build();

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}


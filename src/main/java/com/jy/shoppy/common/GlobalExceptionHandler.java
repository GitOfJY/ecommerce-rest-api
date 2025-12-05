package com.jy.shoppy.common;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.atomic.AtomicReference;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final String VALIDATE_ERROR = "VALIDATE_ERROR";
    private final String SERVER_ERROR = "SERVER_ERROR";

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<?> handleResponseException(ServiceException ex) {
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        AtomicReference<String> errors = new AtomicReference<>("");
        ex.getBindingResult().getAllErrors().forEach(c -> errors.set(c.getDefaultMessage()));

        return ApiResponse.badRequest(VALIDATE_ERROR, String.valueOf(errors));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> bindException(BindException ex) {
        AtomicReference<String> errors = new AtomicReference<>("");
        ex.getBindingResult().getAllErrors().forEach(c -> errors.set(c.getDefaultMessage()));

        return ApiResponse.badRequest(VALIDATE_ERROR, String.valueOf(errors));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();

        // 상품명 유니크 키 위반일 때
        if (message != null && message.contains("uk_product_name")) {
            return ApiResponse.badRequest("DUPLICATE_PRODUCT_NAME", "이미 존재하는 상품명입니다.");
        }

        // 그 외 다른 제약 조건 위반 등
        return ApiResponse.serverError("DATA_INTEGRITY", "데이터 무결성 오류가 발생했습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> serverException(Exception ex) {
        return ApiResponse.serverError(SERVER_ERROR, ex.getMessage());
    }
}

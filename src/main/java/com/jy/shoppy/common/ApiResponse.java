package com.jy.shoppy.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    int status;
    Boolean result;
    Error error;
    T message;

    // 기본 OK (200)
    public static <T> ApiResponse<T> success() {
        return success(null, HttpStatus.OK);
    }

    // 200 OK (데이터 있음)
    public static <T> ApiResponse<T> success(T message) {
        return success(message, HttpStatus.OK);
    }

    // 성공 상태코드 지정 가능 (200, 201, 204 등)
    public static <T> ApiResponse<T> success(T message, HttpStatus status) {
        return ApiResponse.<T>builder()
                .status(status.value())
                .result(true)
                .message(message)
                .build();
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(String code, String errorMessage) {
        return ResponseEntity.ok(ApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .result(false)
                .error(Error.of(code, errorMessage))
                .build());
    }

    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String code, String errorMessage) {
        return ResponseEntity.badRequest().body(ApiResponse.<T>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .result(false)
                .error(Error.of(code, errorMessage))
                .build());
    }

    public static <T> ResponseEntity<ApiResponse<T>> serverError(String code, String errorMessage) {
        return ResponseEntity.status(500).body(ApiResponse.<T>builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .result(false)
                .error(Error.of(code, errorMessage))
                .build());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Error(String errorCode, String errorMessage) {
        public static Error of(String errorCode, String errorMessage) {
            return new Error(errorCode, errorMessage);
        }
    }
}
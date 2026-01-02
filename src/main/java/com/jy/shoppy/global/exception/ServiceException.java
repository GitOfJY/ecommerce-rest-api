package com.jy.shoppy.global.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceException extends RuntimeException {
    String code;
    String message;
    HttpStatus status;

    public ServiceException(ServiceExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.code = exceptionCode.name();
        this.message = exceptionCode.getMessage();
        this.status = exceptionCode.getStatus();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
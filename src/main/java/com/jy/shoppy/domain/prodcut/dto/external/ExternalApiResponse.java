package com.jy.shoppy.domain.prodcut.dto.external;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 외부 상품 API 응답 최상위 구조
 */
@Getter
@NoArgsConstructor
public class ExternalApiResponse {
    private Boolean result;
    private Error error;
    private ExternalProductPage message;

    public boolean isSuccess() {
        return Boolean.TRUE.equals(result);
    }

    @Getter
    @NoArgsConstructor
    public static class Error {
        private String errorCode;
        private String errorMessage;
    }
}

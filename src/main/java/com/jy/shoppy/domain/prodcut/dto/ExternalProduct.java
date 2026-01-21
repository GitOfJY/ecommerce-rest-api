package com.jy.shoppy.domain.prodcut.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ExternalProduct {
    private Long id;                    // 외부 상품 ID
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 주문 가능 여부 계산
     * stock > 0 이면 true, 아니면 false
     */
    public boolean isOrderable() {
        return stock != null && stock > 0;
    }

    /**
     * 외부 상품 ID를 문자열로 변환 (내부 저장용)
     */
    public String getExternalId() {
        return String.valueOf(id);
    }
}

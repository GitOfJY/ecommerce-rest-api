package com.jy.shoppy.domain.prodcut.dto;

import com.jy.shoppy.domain.prodcut.entity.type.StockStatus;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SearchProductCond(
        Long categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String productKeyword,
        StockStatus stockStatus,
        Boolean isOrderable
) {}

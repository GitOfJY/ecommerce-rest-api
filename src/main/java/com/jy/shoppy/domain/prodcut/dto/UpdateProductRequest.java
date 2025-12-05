package com.jy.shoppy.domain.prodcut.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class UpdateProductRequest {
    private String name;

    private String description;

    @Min(0)
    private BigDecimal price;

    @Min(0)
    private Integer stock;

    private List<Long> categoryIds;
}

package com.jy.shoppy.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

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

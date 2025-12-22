package com.jy.shoppy.domain.prodcut.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jy.shoppy.domain.prodcut.entity.type.StockStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductResponse {
    Long id;
    String name;
    String description;
    BigDecimal price;
    Integer totalStock;
    StockStatus stockStatus;
    LocalDateTime createdAt;
    List<Long> categoryIds;
}

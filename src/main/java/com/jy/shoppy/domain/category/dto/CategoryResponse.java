package com.jy.shoppy.domain.category.dto;

import com.jy.shoppy.domain.prodcut.dto.ProductResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private List<ProductResponse> products;
}

package com.sparta.demo.service.dto;

import com.sparta.demo.entity.Category;
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

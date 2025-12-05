package com.jy.shoppy.service.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CategoryTreeResponse {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private  List<CategoryTreeResponse> children;
}

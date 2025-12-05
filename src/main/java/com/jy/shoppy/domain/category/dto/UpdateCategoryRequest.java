package com.jy.shoppy.domain.category.dto;

import lombok.Getter;
import org.wildfly.common.annotation.NotNull;

@Getter
public class UpdateCategoryRequest {
    @NotNull
    private Long id;

    private String name;

    private String description;

    private Long parentId;
}

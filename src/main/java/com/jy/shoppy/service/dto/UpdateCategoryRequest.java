package com.jy.shoppy.service.dto;

import lombok.Getter;
import lombok.Setter;
import org.wildfly.common.annotation.NotNull;

@Getter
public class UpdateCategoryRequest {
    @NotNull
    private Long id;

    private String name;

    private String description;

    private Long parentId;
}

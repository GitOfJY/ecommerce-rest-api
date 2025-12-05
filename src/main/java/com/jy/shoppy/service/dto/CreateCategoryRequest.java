package com.jy.shoppy.service.dto;

import com.jy.shoppy.entity.Category;
import com.jy.shoppy.entity.CategoryProduct;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private Long parentId;

    private List<Long> childrenIds;
}

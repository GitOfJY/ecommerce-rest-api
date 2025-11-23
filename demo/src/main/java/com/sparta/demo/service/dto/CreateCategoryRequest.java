package com.sparta.demo.service.dto;

import com.sparta.demo.entity.Category;
import com.sparta.demo.entity.CategoryProduct;
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

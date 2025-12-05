package com.jy.shoppy.domain.category.mapper;

import com.jy.shoppy.domain.category.entity.Category;
import com.jy.shoppy.domain.category.dto.CategoryResponse;
import com.jy.shoppy.domain.category.dto.CreateCategoryRequest;
import com.jy.shoppy.domain.prodcut.mapper.ProductMapper;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = { ProductMapper.class })
public interface CategoryMapper {
    Category toEntity(CreateCategoryRequest req);

    @Mapping(target = "parentId",
            expression = "java(category.getParent() != null ? category.getParent().getId() : null)")
    @Mapping(target = "products", source = "categoryProducts")
    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);
}
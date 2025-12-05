package com.jy.shoppy.mapper;

import com.jy.shoppy.entity.CategoryProduct;
import com.jy.shoppy.entity.Product;
import com.jy.shoppy.service.dto.CreateProductRequest;
import com.jy.shoppy.service.dto.ProductResponse;
import com.jy.shoppy.service.dto.UpdateProductRequest;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    // 단건 변환
    ProductResponse toResponse(Product product);

    // 리스트 변환: 개별 요소에 대해 위 toResponse를 재사용
    List<ProductResponse> toResponseList(List<Product> products);

    default ProductResponse fromCategoryProduct(CategoryProduct cp) {
        if (cp == null) return null;
        return toResponse(cp.getProduct());
    }

    // List<CategoryProduct> -> List<ProductResponse>
    default List<ProductResponse> fromCategoryProducts(List<CategoryProduct> cps) {
        if (cps == null || cps.isEmpty()) return Collections.emptyList();
        return cps.stream()
                .map(this::fromCategoryProduct)
                .toList();
    }

    // 수정 변환: 카테고리 매핑은 서비스에서 처리하므로 무시
    void updateProductFromDto(UpdateProductRequest dto, @MappingTarget Product entity);

    // 등록 변환: 관계 매핑은 서비스에서
    Product toEntity(CreateProductRequest req);

    default List<Long> toCategoryIds(List<CategoryProduct> cps) {
        if (cps == null || cps.isEmpty()) return Collections.emptyList();
        return cps.stream()
                .map(cp -> cp.getCategory().getId())
                .distinct()
                .collect(Collectors.toList());
    }
}

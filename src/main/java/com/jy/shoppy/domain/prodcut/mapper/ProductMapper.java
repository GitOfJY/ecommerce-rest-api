package com.jy.shoppy.domain.prodcut.mapper;

import com.jy.shoppy.domain.category.entity.CategoryProduct;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.dto.CreateProductRequest;
import com.jy.shoppy.domain.prodcut.dto.ProductResponse;
import com.jy.shoppy.domain.prodcut.dto.UpdateProductRequest;
import com.jy.shoppy.domain.prodcut.entity.ProductOption;
import com.jy.shoppy.domain.prodcut.entity.type.StockStatus;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    // 단건 변환
    @Mapping(target = "totalStock", source = "totalStock")
    @Mapping(target = "stockStatus", source = "stockStatus")
    @Mapping(target = "categoryIds", expression = "java(extractCategoryIds(product))")
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

    default List<Long> extractCategoryIds(Product product) {
        return product.getCategoryProducts().stream()
                .map(cp -> cp.getCategory().getId())
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

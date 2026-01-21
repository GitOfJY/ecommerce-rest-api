package com.jy.shoppy.domain.prodcut.mapper;

import com.jy.shoppy.domain.prodcut.dto.ExternalProduct;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.entity.type.ProductSource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", imports = {ProductSource.class, LocalDateTime.class})
public interface ExternalProductMapper {
    /**
     * 외부 상품 DTO → Product 엔티티 변환 (신규 등록용)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalProductId", expression = "java(dto.getExternalId())")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "isOrderable", expression = "java(dto.isOrderable())")
    @Mapping(target = "source", expression = "java(ProductSource.EXTERNAL)")
    @Mapping(target = "lastSyncedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "orderProducts", ignore = true)
    @Mapping(target = "categoryProducts", ignore = true)
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "images", ignore = true)
    Product toEntity(ExternalProduct dto);

    List<Product> toEntityList(List<ExternalProduct> dtos);

    /**
     * 기존 Product 엔티티 업데이트 - default 메서드로 직접 구현
     * (MapStruct 자동 매핑 X, 엔티티 메서드 직접 호출)
     */
    default void updateFromExternal(ExternalProduct dto, Product entity) {
        entity.updateFromExternal(
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                dto.isOrderable()
        );
    }
}
package com.jy.shoppy.domain.prodcut.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Redis 기반 상품 정렬 조건")
public record RedisSortProductCond (
    @Schema(description = "정렬 기준 (price, rating, date)", example = "price")
    String sortBy,

    @Schema(description = "오름차순 여부 (true: 오름차순, false/null: 내림차순)", example = "true")
    Boolean ascending,

    @Schema(description = "최소 가격", example = "10000")
    Double minPrice,

    @Schema(description = "최대 가격", example = "50000")
    Double maxPrice,

    @Schema(description = "카테고리 ID", example = "1")
    Long categoryId
){
    // 기본값 설정
    public RedisSortProductCond {
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "date"; // 기본값: 등록일 정렬
        }

        if (ascending == null) {
            ascending = false; // 기본값: 내림차순
        }
    }
}

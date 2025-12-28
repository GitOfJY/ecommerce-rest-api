package com.jy.shoppy.domain.prodcut.entity.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "정렬 기준")
public enum SortType {
    PRICE_ASC("가격 낮은순"),
    PRICE_DESC("가격 높은순"),
    DATE_ASC("오래된순"),
    DATE_DESC("최신순"),
    RATING_DESC("평점 높은순"),
    REVIEW_DESC("리뷰 많은순");

    private final String description;
}

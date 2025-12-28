package com.jy.shoppy.domain.prodcut.dto;

import com.jy.shoppy.domain.prodcut.entity.type.SortType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import static com.jy.shoppy.domain.prodcut.entity.type.SortType.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "상품 정렬 조건 (DB 쿼리용)")
public class SortProductCond {
    @Schema(
            description = "정렬 타입",
            example = "PRICE_ASC",
            allowableValues = {"PRICE_ASC", "PRICE_DESC", "DATE_ASC", "DATE_DESC", "RATING_DESC", "REVIEW_DESC"}
    )
    private SortType sortType;

    // SortType을 기반으로 Sort 객체 생성
    public Sort toSort() {
        if (sortType == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt"); // 기본값: 최신순
        }

        return switch (sortType) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price");
            case DATE_ASC -> Sort.by(Sort.Direction.ASC, "createdAt");
            case DATE_DESC -> Sort.by(Sort.Direction.DESC, "createdAt");
            case RATING_DESC -> Sort.by(Sort.Direction.DESC, "averageRating");
            case REVIEW_DESC -> Sort.by(Sort.Direction.DESC, "reviewCount");
        };
    }
}

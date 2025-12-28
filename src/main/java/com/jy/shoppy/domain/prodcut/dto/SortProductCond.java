package com.jy.shoppy.domain.prodcut.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "상품 정렬 조건 (DB 쿼리용)")
public class SortProductCond {
    private Sort.Direction priceSort;
    private Sort.Direction createdAtSort;
}

package com.jy.shoppy.domain.prodcut.dto.external;

import com.jy.shoppy.domain.prodcut.dto.ExternalProduct;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 외부 상품 API 페이지 응답
 */
@Getter
@NoArgsConstructor
public class ExternalProductPage {
    private List<ExternalProduct> contents;
    private Pageable pageable;

    public boolean hasNextPage() {
        return pageable != null && !pageable.isLast();
    }

    @Getter
    @NoArgsConstructor
    public static class Pageable {
        private Long offset;
        private Long pageNumber;
        private Long pageSize;
        private Long pageElements;
        private Long totalPages;
        private Long totalElements;
        private boolean first;
        private boolean last;
    }
}

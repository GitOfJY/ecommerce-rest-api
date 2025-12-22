package com.jy.shoppy.domain.prodcut.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SortProductCond {
    private Sort.Direction priceSort;
    private Sort.Direction createdAtSort;
}

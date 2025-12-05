package com.jy.shoppy.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SortProductCond {
    private Sort.Direction priceSort;
    private Sort.Direction createdAtSort;
}

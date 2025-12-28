package com.jy.shoppy.domain.prodcut.controller;

import com.jy.shoppy.domain.prodcut.dto.*;
import com.jy.shoppy.domain.prodcut.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.jy.shoppy.global.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Product", description = "상품 조회 API")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @Operation(
            summary = "상품 단건 조회 API",
            description = "상품 단건 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getOne(id), HttpStatus.OK));
    }

    @Operation(
            summary = "상품 전체 조회 API (DB 정렬)",
            description = "상품 전체 조회합니다."
                    + "다중 정렬 조건 (가격 + 등록일)"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAll(SortProductCond cond, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.getAll(cond, pageable), HttpStatus.OK));
    }

    @Operation(
            summary = "상품 검색 API (DB 검색)",
            description = "상품을 검색합니다." +
                    "(검색 및 필터링 조건: 카테고리, 가격 범위, 상품명 키워드, 재고상태)"
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(SearchProductCond cond, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.searchProductsPage(cond, pageable), HttpStatus.OK));
    }

    @Operation(
            summary = "상품 목록 조회 (Redis 정렬)",
            description = "Redis ZSET을 활용한 빠른 정렬 조회 (가격/평점/등록일)"
    )
    @GetMapping("/redis")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsWithRedis(
            @Parameter(description = "정렬 기준 (price, rating, date)")
            @RequestParam(defaultValue = "date") String sortBy,

            @Parameter(description = "오름차순 여부 (true: 오름차순, false/null: 내림차순)")
            @RequestParam(required = false) Boolean ascending,

            @Parameter(description = "최소 가격")
            @RequestParam(required = false) Double minPrice,

            @Parameter(description = "최대 가격")
            @RequestParam(required = false) Double maxPrice,

            @Parameter(description = "카테고리 ID")
            @RequestParam(required = false) Long categoryId,

            Pageable pageable) {

        RedisSortProductCond cond = new RedisSortProductCond(
                sortBy, ascending, minPrice, maxPrice, categoryId
        );
        Page<ProductResponse> page = productService.getAllWithRedis(cond, pageable);

        return ResponseEntity.ok(ApiResponse.success(page, HttpStatus.OK));
    }
}
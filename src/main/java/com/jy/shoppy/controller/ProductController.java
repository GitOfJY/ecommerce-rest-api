package com.jy.shoppy.controller;

import com.jy.shoppy.common.ApiResponse;
import com.jy.shoppy.service.ProductService;
import com.jy.shoppy.service.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @Operation(
            summary = "상품 등록 API",
            description = "새로운 상품을 등록합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(@RequestBody @Valid CreateProductRequest req) {
        Long id = productService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(id, HttpStatus.CREATED));
    }

    @Operation(
            summary = "상품 단건 조회 API",
            description = "상품 단건 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getOne(id), HttpStatus.OK));
    }

    @Operation(
            summary = "상품 전체 조회 API",
            description = "상품 전체 조회합니다."
                    + "다중 정렬 조건 (가격 + 등록일)"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAll(SortProductCond cond, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.getAll(cond, pageable), HttpStatus.OK));
    }

    @Operation(
            summary = "상품 검색 API",
            description = "상품을 검색합니다." +
                    "(검색 및 필터링 조건: 카테고리, 가격 범위, 상품명 키워드, 재고상태)"
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(SearchProductCond cond, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.searchProductsPage(cond, pageable), HttpStatus.OK));
    }

    @Operation(
            summary = "상품 수정 API",
            description = "상품 ID로 상품을 수정합니다."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(@PathVariable Long id,
                                                               @RequestBody @Valid UpdateProductRequest req) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(productService.update(id, req), HttpStatus.OK));
    }

    @Operation(
            summary = "상품 삭제 API",
            description = "상품 ID로 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Long>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.deleteProduct(id)));
    }
}
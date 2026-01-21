package com.jy.shoppy.domain.prodcut.controller;

import com.jy.shoppy.domain.prodcut.dto.*;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.entity.type.SortType;
import com.jy.shoppy.domain.prodcut.repository.ProductRepository;
import com.jy.shoppy.domain.prodcut.service.ProductRedisService;
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

import java.util.List;

@Tag(name = "Product", description = "상품 조회 API")
@RestController
@RequestMapping("/api/products")
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
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAll(
            @RequestParam(required = false) SortType sortType,
            Pageable pageable) {
        SortProductCond cond = SortProductCond.builder()
                .sortType(sortType)
                .build();
        return ResponseEntity.ok(ApiResponse.success(productService.getAll(cond, pageable), HttpStatus.OK));
    }

    @Operation(
            summary = "상품 검색 API (DB 검색)",
            description = "상품을 검색합니다." +
                    "(검색 및 필터링 조건: 카테고리, 가격 범위, 상품명 키워드, 재고상태, 주문가능여부)"
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(SearchProductCond cond, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.searchProductsPage(cond, pageable), HttpStatus.OK));
    }

    @Operation(
            summary = "내부/외부 상품 목록 조회",
            description = "카테고리, 주문가능여부, 가격, 키워드로 필터링하고 페이징 처리합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            SearchProductCond cond,
            Pageable pageable
    ) {
        Page<ProductResponse> response = productService.searchProductsPage(cond, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, HttpStatus.OK));
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

    // -----------------------------------------------------------
    private final ProductRepository productRepository;
    private final ProductRedisService  productRedisService;

    @PostMapping("/redis/reload")
    @Operation(
            summary = "[개발용] Redis 데이터 수동 재적재",
            description = "모든 상품 데이터를 Redis에 수동 적재합니다. (추후삭제)"
    )
    public ResponseEntity<ApiResponse<String>> reloadRedisData() {
        List<Product> products = productRepository.findAll();

        int count = 0;
        for (Product product : products) {
            productRedisService.saveProduct(product);
            count++;
        }

        String message = String.format("총 %d개 상품이 Redis에 재적재되었습니다.", count);
        return ResponseEntity.ok(ApiResponse.success(message, HttpStatus.OK));
    }
}
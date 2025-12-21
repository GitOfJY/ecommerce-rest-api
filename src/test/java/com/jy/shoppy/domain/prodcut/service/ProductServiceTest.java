package com.jy.shoppy.domain.prodcut.service;

import com.jy.shoppy.domain.category.entity.Category;
import com.jy.shoppy.domain.category.repository.CategoryRepository;
import com.jy.shoppy.domain.prodcut.dto.CreateProductRequest;
import com.jy.shoppy.domain.prodcut.dto.ProductResponse;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.entity.ProductOption;
import com.jy.shoppy.domain.prodcut.entity.type.StockStatus;
import com.jy.shoppy.domain.prodcut.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Transactional
class ProductServiceTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void init() {
        // 테스트용 카테고리 생성 (격리된 테스트 데이터)
        testCategory = Category.builder()
                .name("테스트 카테고리")
                .description("테스트용 카테고리입니다")
                .build();
        categoryRepository.save(testCategory);
    }

    @Test
    @DisplayName("상품 등록 성공 - 카테고리와 옵션 포함")
    void create_product_with_category_option() {
        // given
        List<CreateProductRequest.ProductOptionRequest> options = List.of(
                createOptionRequest("빨강", "S", 10, 1000),
                createOptionRequest("빨강", "M", 1, 1000),
                createOptionRequest("빨강", "L", 9, 2000)
        );

        CreateProductRequest request = CreateProductRequest.builder()
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .price(new BigDecimal("10000"))
                .options(options)
                .categoryIds(List.of(testCategory.getId()))
                .build();

        // when
        ProductResponse response = productService.create(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();

        // 저장된 상품 조회
        Product savedProduct = productRepository.findById(response.getId())
                .orElseThrow();

        // 기본 정보 검증
        assertThat(savedProduct.getName()).isEqualTo("테스트 상품");
        assertThat(savedProduct.getDescription()).isEqualTo("테스트 상품 설명");
        assertThat(savedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(savedProduct.isHasOptions()).isTrue();

        // 옵션 검증
        List<ProductOption> savedOptions = savedProduct.getOptions();
        assertThat(savedOptions).hasSize(3);
        assertThat(savedOptions)
                .extracting("color", "size", "stock")
                .containsExactlyInAnyOrder(
                        tuple("빨강", "S", 10),
                        tuple("빨강", "M", 1),
                        tuple("빨강", "L", 9)
                );

        // 총 재고 검증 (10 + 1 + 9 = 20)
        assertThat(savedProduct.getTotalStock()).isEqualTo(20);

        // 재고 상태 검증 (총 20개 > 5 → IN_STOCK)
        assertThat(savedProduct.getStockStatus()).isEqualTo(StockStatus.IN_STOCK);

        // 카테고리 검증
        assertThat(savedProduct.getCategoryProducts()).hasSize(1);
        assertThat(savedProduct.getCategoryProducts().get(0).getCategory().getId())
                .isEqualTo(testCategory.getId());
    }

    @Test
    @DisplayName("상품 등록 성공 - 옵션 없이")
    void create_product_without_options_success() {
        List<CreateProductRequest.ProductOptionRequest> option = List.of(
                createOptionRequest(null, null, 10, 0)
        );

        CreateProductRequest request = CreateProductRequest.builder()
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .price(new BigDecimal("10000"))
                .categoryIds(List.of(testCategory.getId()))
                .options(option)
                .build();
        ProductResponse response = productService.create(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();

        // 저장된 상품 조회
        Product savedProduct = productRepository.findById(response.getId())
                .orElseThrow();

        // 기본 정보 검증
        assertThat(savedProduct.getName()).isEqualTo("테스트 상품");
        assertThat(savedProduct.getDescription()).isEqualTo("테스트 상품 설명");
        assertThat(savedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(savedProduct.isHasOptions()).isTrue();

        // 옵션 검증
        List<ProductOption> savedOptions = savedProduct.getOptions();
        assertThat(savedOptions).hasSize(1);

        // 총 재고 검증 (10 + 1 + 9 = 20)
        assertThat(savedProduct.getTotalStock()).isEqualTo(10);

        // 재고 상태 검증 (총 20개 > 5 → IN_STOCK)
        assertThat(savedProduct.getStockStatus()).isEqualTo(StockStatus.IN_STOCK);

        assertThat(savedProduct.getCategoryProducts()).hasSize(1);
        assertThat(savedProduct.getCategoryProducts().get(0).getCategory().getId())
                .isEqualTo(testCategory.getId());
    }

    @Test
    @DisplayName("상품 등록 성공 - 재고 부족 상태")
    void create_product_with_low_stock() {
        List<CreateProductRequest.ProductOptionRequest> option = List.of(
                createOptionRequest(null, null, 1, 0)
        );

        CreateProductRequest request = CreateProductRequest.builder()
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .price(new BigDecimal("10000"))
                .categoryIds(List.of(testCategory.getId()))
                .options(option)
                .build();
        ProductResponse response = productService.create(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();

        // 저장된 상품 조회
        Product savedProduct = productRepository.findById(response.getId())
                .orElseThrow();

        // 기본 정보 검증
        assertThat(savedProduct.getName()).isEqualTo("테스트 상품");
        assertThat(savedProduct.getDescription()).isEqualTo("테스트 상품 설명");
        assertThat(savedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(savedProduct.isHasOptions()).isTrue();

        // 재고 상태 검증
        assertThat(savedProduct.getStockStatus()).isEqualTo(StockStatus.LOW_STOCK);

        assertThat(savedProduct.getCategoryProducts()).hasSize(1);
        assertThat(savedProduct.getCategoryProducts().get(0).getCategory().getId())
                .isEqualTo(testCategory.getId());
    }

    @Test
    @DisplayName("상품 등록 성공 - 품절 상태")
    void create_product_with_out_of_stock() {
        List<CreateProductRequest.ProductOptionRequest> option = List.of(
                createOptionRequest(null, null, 0, 0)
        );

        CreateProductRequest request = CreateProductRequest.builder()
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .price(new BigDecimal("10000"))
                .categoryIds(List.of(testCategory.getId()))
                .options(option)
                .build();
        ProductResponse response = productService.create(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();

        // 저장된 상품 조회
        Product savedProduct = productRepository.findById(response.getId())
                .orElseThrow();

        // 기본 정보 검증
        assertThat(savedProduct.getName()).isEqualTo("테스트 상품");
        assertThat(savedProduct.getDescription()).isEqualTo("테스트 상품 설명");
        assertThat(savedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(savedProduct.isHasOptions()).isTrue();

        // 재고 상태 검증
        assertThat(savedProduct.getStockStatus()).isEqualTo(StockStatus.OUT_OF_STOCK);
    }

//    @Test
//    @DisplayName("상품 수정 성공")
//    void update_product_option() {
//
//    }

    //    @Test
//    @DisplayName("상품 옵션 수정 성공")
//    void update_product_option() {
//
//    }

    private CreateProductRequest.ProductOptionRequest createOptionRequest(
            String color,
            String size,
            int stock,
            int additionalPrice) {

        return CreateProductRequest.ProductOptionRequest.builder()
                .color(color)
                .size(size)
                .stock(stock)
                .additionalPrice(new BigDecimal(additionalPrice))
                .build();
    }
}
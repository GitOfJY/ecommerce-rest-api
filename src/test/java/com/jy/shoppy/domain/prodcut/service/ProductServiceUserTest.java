package com.jy.shoppy.domain.prodcut.service;

import com.jy.shoppy.domain.category.entity.Category;
import com.jy.shoppy.domain.category.repository.CategoryRepository;
import com.jy.shoppy.domain.prodcut.dto.CreateProductRequest;
import com.jy.shoppy.domain.prodcut.dto.ProductResponse;
import com.jy.shoppy.domain.prodcut.dto.SearchProductCond;
import com.jy.shoppy.domain.prodcut.dto.SortProductCond;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.entity.type.StockStatus;
import com.jy.shoppy.domain.prodcut.repository.ProductQueryRepository;
import com.jy.shoppy.domain.prodcut.repository.ProductRepository;
import com.jy.shoppy.global.exception.ServiceException;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Transactional
@Slf4j
public class ProductServiceUserTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductQueryRepository productQueryRepository;

    @Autowired
    private EntityManager em;

    private Category electronicsCategory;
    private Category clothingCategory;
    private Product laptop;
    private Product smartphone;
    private Product tshirt;

    @BeforeEach
    void init() throws InterruptedException {
        // 카테고리 생성
        electronicsCategory = createAndSaveCategory("전자제품", "전자제품 카테고리");
        clothingCategory = createAndSaveCategory("의류", "의류 카테고리");

        // 테스트 상품 생성
        laptop = createAndSaveProduct(
                "노트북",
                "고성능 노트북",
                new BigDecimal("1500000"),
                10,
                electronicsCategory
        );

        smartphone = createAndSaveProduct(
                "스마트폰",
                "최신 스마트폰",
                new BigDecimal("800000"),
                3,
                electronicsCategory
        );

        tshirt = createAndSaveProduct(
                "티셔츠",
                "편안한 티셔츠",
                new BigDecimal("25000"),
                0,
                clothingCategory
        );

        em.flush();
        em.clear();
    }
    @Nested
    @DisplayName("상품 조회 테스트")
    class GetProductTest {
        @Test
        @DisplayName("상품 단건 조회 성공")
        void get_one_product_success() {
            // when
            ProductResponse response = productService.getOne(laptop.getId());

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(laptop.getId());
            assertThat(response.getName()).isEqualTo("노트북");
            assertThat(response.getDescription()).isEqualTo("고성능 노트북");
            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("1500000"));
            assertThat(response.getTotalStock()).isEqualTo(10);
            assertThat(response.getStockStatus()).isEqualTo(StockStatus.IN_STOCK);
        }

        @Test
        @DisplayName("존재하지 않는 상품 조회 실패")
        void get_one_product_not_found() {
            // when & then
            assertThatThrownBy(() -> productService.getOne(999L))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("상품 전체 조회 - 정렬 조건 없음")
        void get_all_products_without_sort() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            SortProductCond cond = new SortProductCond();

            // when
            Page<ProductResponse> result = productService.getAll(cond, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        /*
        @Test
        @DisplayName("상품 전체 조회 - 가격 오름차순 정렬")
        void get_all_products_sorted_by_price_asc() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            SortProductCond cond = SortProductCond.builder()
                    .priceSort(Sort.Direction.ASC)
                    .build();

            // when
            Page<ProductResponse> result = productService.getAll(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent().get(0).getName()).isEqualTo("티셔츠");
            assertThat(result.getContent().get(1).getName()).isEqualTo("스마트폰");
            assertThat(result.getContent().get(2).getName()).isEqualTo("노트북");
        }

        @Test
        @DisplayName("상품 전체 조회 - 가격 내림차순 정렬")
        void get_all_products_sorted_by_price_desc() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            SortProductCond cond = SortProductCond.builder()
                    .priceSort(Sort.Direction.DESC)
                    .build();

            // when
            Page<ProductResponse> result = productService.getAll(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent().get(0).getName()).isEqualTo("노트북");
            assertThat(result.getContent().get(1).getName()).isEqualTo("스마트폰");
            assertThat(result.getContent().get(2).getName()).isEqualTo("티셔츠");
        }

        @Test
        @DisplayName("상품 전체 조회 - 등록일 정렬")
        void get_all_products_sorted_by_created_date() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            SortProductCond cond = SortProductCond.builder()
                    .createdAtSort(Sort.Direction.DESC)
                    .build();

            // 내림차순(DESC): 큰 값(나중에 생성된 것, 최근 등록)에서 작은 값

            // when
            Page<ProductResponse> result = productService.getAll(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent().get(0).getName()).isEqualTo("티셔츠");
        }

        @Test
        @DisplayName("정렬 조건 디버깅")
        void debug_sort_condition() {
            SortProductCond cond = SortProductCond.builder()
                    .createdAtSort(Sort.Direction.DESC)
                    .build();

            log.info("createdAtSort: {}", cond.getCreatedAtSort());
            log.info("priceSort: {}", cond.getPriceSort());

            // 직접 QueryRepository 호출해서 확인
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> result = productQueryRepository.sortProducts(cond, pageable);

            result.getContent().forEach(p ->
                    log.info("id: {}, name: {}, createdAt: {}", p.getId(), p.getName(), p.getCreatedAt())
            );
        }

        @Test
        @DisplayName("상품 전체 조회 - 페이징")
        void get_all_products_with_paging() {
            // given
            Pageable pageable = PageRequest.of(0, 2);
            SortProductCond cond = new SortProductCond();

            // when
            Page<ProductResponse> result = productService.getAll(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.hasNext()).isTrue();
        }*/
    }

    @Nested
    @DisplayName("상품 검색 테스트")
    class SearchProductTest {

        @Test
        @DisplayName("상품명으로 검색 성공")
        void search_products_by_name() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            SearchProductCond cond = SearchProductCond.builder()
                    .productKeyword("노트북")
                    .build();

            // when
            Page<ProductResponse> result = productService.searchProductsPage(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("노트북");
        }

        @Test
        @DisplayName("부분 키워드로 검색 성공")
        void search_products_by_partial_keyword() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            SearchProductCond cond = SearchProductCond.builder()
                    .productKeyword("스마트")
                    .build();

            // when
            Page<ProductResponse> result = productService.searchProductsPage(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("스마트폰");
        }

        @Test
        @DisplayName("카테고리로 검색 성공")
        void search_products_by_category() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            SearchProductCond cond = SearchProductCond.builder()
                    .categoryId(electronicsCategory.getId())
                    .build();

            // when
            Page<ProductResponse> result = productService.searchProductsPage(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting("name")
                    .containsExactlyInAnyOrder("노트북", "스마트폰");
        }

        @Test
        @DisplayName("가격 범위로 검색 성공 - 최소 가격")
        void search_products_by_min_price() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            SearchProductCond cond = SearchProductCond.builder()
                    .minPrice(new BigDecimal("500000"))
                    .build();

            // when
            Page<ProductResponse> result = productService.searchProductsPage(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting("name")
                    .containsExactlyInAnyOrder("노트북", "스마트폰");
        }

        @Test
        @DisplayName("가격 범위로 검색 성공 - 최대 가격")
        void search_products_by_max_price() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            SearchProductCond cond = SearchProductCond.builder()
                    .maxPrice(new BigDecimal("100000"))
                    .build();

            // when
            Page<ProductResponse> result = productService.searchProductsPage(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("티셔츠");
        }

        @Test
        @DisplayName("가격 범위로 검색 성공 - 최소/최대 가격")
        void search_products_by_price_range() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            SearchProductCond cond = SearchProductCond.builder()
                    .minPrice(new BigDecimal("100000"))
                    .maxPrice(new BigDecimal("1000000"))
                    .build();

            // when
            Page<ProductResponse> result = productService.searchProductsPage(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("스마트폰");
        }

        @Test
        @DisplayName("재고 상태로 검색 성공 - IN_STOCK")
        void search_products_by_stock_status_in_stock() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            SearchProductCond cond = SearchProductCond.builder()
                    .stockStatus(StockStatus.IN_STOCK)
                    .build();

            // when
            Page<ProductResponse> result = productService.searchProductsPage(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("노트북");
            assertThat(result.getContent().get(0).getStockStatus()).isEqualTo(StockStatus.IN_STOCK);
        }

        @Test
        @DisplayName("재고 상태로 검색 성공 - LOW_STOCK")
        void search_products_by_stock_status_low_stock() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            SearchProductCond cond = SearchProductCond.builder()
                    .stockStatus(StockStatus.LOW_STOCK)
                    .build();

            // when
            Page<ProductResponse> result = productService.searchProductsPage(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("스마트폰");
            assertThat(result.getContent().get(0).getStockStatus()).isEqualTo(StockStatus.LOW_STOCK);
        }

        @Test
        @DisplayName("재고 상태로 검색 성공 - OUT_OF_STOCK")
        void search_products_by_stock_status_out_of_stock() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            SearchProductCond cond = SearchProductCond.builder()
                    .stockStatus(StockStatus.OUT_OF_STOCK)
                    .build();

            // when
            Page<ProductResponse> result = productService.searchProductsPage(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("티셔츠");
            assertThat(result.getContent().get(0).getStockStatus()).isEqualTo(StockStatus.OUT_OF_STOCK);
        }

        @Test
        @DisplayName("복합 조건으로 검색 성공 - 카테고리 + 가격 범위")
        void search_products_by_multiple_conditions() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            SearchProductCond cond = SearchProductCond.builder()
                    .categoryId(electronicsCategory.getId())
                    .minPrice(new BigDecimal("500000"))
                    .maxPrice(new BigDecimal("1000000"))
                    .build();

            // when
            Page<ProductResponse> result = productService.searchProductsPage(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("스마트폰");
        }

        @Test
        @DisplayName("복합 조건으로 검색 성공 - 키워드 + 재고 상태")
        void search_products_by_keyword_and_stock_status() {
            // given
            // 추가 상품 생성 (노트북 2개)
            createAndSaveProduct(
                    "게이밍 노트북",
                    "고사양 게이밍 노트북",
                    new BigDecimal("2000000"),
                    2,
                    electronicsCategory
            );

            Pageable pageable = PageRequest.of(0, 10);
            SearchProductCond cond = SearchProductCond.builder()
                    .productKeyword("노트북")
                    .stockStatus(StockStatus.IN_STOCK)
                    .build();

            // when
            Page<ProductResponse> result = productService.searchProductsPage(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("노트북");
        }

        @Test
        @DisplayName("검색 결과 없음")
        void search_products_no_results() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            SearchProductCond cond = SearchProductCond.builder()
                    .productKeyword("존재하지않는상품")
                    .build();

            // when
            Page<ProductResponse> result = productService.searchProductsPage(cond, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("검색 결과 페이징")
        void search_products_with_paging() {
            // given
            // 추가 상품들 생성 (전자제품 카테고리에 여러 개)
            for (int i = 1; i <= 5; i++) {
                createAndSaveProduct(
                        "상품 " + i,
                        "설명 " + i,
                        new BigDecimal("10000"),
                        10,
                        electronicsCategory
                );
            }

            Pageable pageable = PageRequest.of(0, 3);
            SearchProductCond cond = SearchProductCond.builder()
                    .categoryId(electronicsCategory.getId())
                    .build();

            // when
            Page<ProductResponse> result = productService.searchProductsPage(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(7); // 기존 2개 + 새로 추가된 5개
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.hasNext()).isTrue();
        }
    }

    private Category createAndSaveCategory(String name, String description) {
        Category category = Category.builder()
                .name(name)
                .description(description)
                .build();
        return categoryRepository.save(category);
    }

    private Product createAndSaveProduct(
            String name,
            String description,
            BigDecimal price,
            int stock,
            Category category) {

        CreateProductRequest.ProductOptionRequest option = CreateProductRequest.ProductOptionRequest.builder()
                .color(null)
                .size(null)
                .stock(stock)
                .additionalPrice(BigDecimal.ZERO)
                .build();

        CreateProductRequest request = CreateProductRequest.builder()
                .name(name)
                .description(description)
                .price(price)
                .options(List.of(option))
                .categoryIds(List.of(category.getId()))
                .build();

        ProductResponse response = productService.create(request);
        return productRepository.findById(response.getId()).orElseThrow();
    }
}

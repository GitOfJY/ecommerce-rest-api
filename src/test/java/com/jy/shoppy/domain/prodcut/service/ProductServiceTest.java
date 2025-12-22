package com.jy.shoppy.domain.prodcut.service;

import com.jy.shoppy.domain.address.entity.Address;
import com.jy.shoppy.domain.address.entity.DeliveryAddress;
import com.jy.shoppy.domain.category.entity.Category;
import com.jy.shoppy.domain.category.repository.CategoryRepository;
import com.jy.shoppy.domain.category.service.CategoryService;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.order.repository.OrderRepository;
import com.jy.shoppy.domain.prodcut.dto.CreateProductRequest;
import com.jy.shoppy.domain.prodcut.dto.ProductResponse;
import com.jy.shoppy.domain.prodcut.dto.UpdateProductOptionRequest;
import com.jy.shoppy.domain.prodcut.dto.UpdateProductRequest;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.entity.ProductOption;
import com.jy.shoppy.domain.prodcut.entity.type.StockStatus;
import com.jy.shoppy.domain.prodcut.repository.ProductRepository;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

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

        // 총 재고 검증
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

    @Test
    @DisplayName("존재하지 않는 카테고리로 상품 등록 실패")
    void create_product_with_invalid_category() {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("테스트 상품")
                .price(new BigDecimal("10000"))
                .categoryIds(List.of(999L))
                .options(List.of(createOptionRequest(null, null, 10, 0)))
                .build();

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("상품 수정 기본 정보 성공")
    void update_product() {
        // given
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

        Product savedProduct = productRepository.findById(response.getId())
                .orElseThrow();

        UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                .name("수정 테스트")
                .description("수정 테스트 description")
                .price(new BigDecimal("5000"))
                .build();

        // when
        ProductResponse updateResponse = productService.update(savedProduct.getId(), updateRequest);
        Product updateProduct = productRepository.findById(updateResponse.getId()).orElseThrow();

        // then
        assertThat(updateProduct.getName()).isEqualTo("수정 테스트");
        assertThat(updateProduct.getDescription()).isEqualTo("수정 테스트 description");
        assertThat(updateProduct.getPrice()).isEqualTo(new BigDecimal("5000"));
    }

    @Test
    @DisplayName("존재하지 않는 상품 수정 실패")
    void update_nonexistent_product() {
        UpdateProductRequest request = UpdateProductRequest.builder()
                .name("수정")
                .build();

        assertThatThrownBy(() -> productService.update(999L, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("상품 옵션 수정 성공")
    void update_product_option() {
        // given
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

        Product savedProduct = productRepository.findById(response.getId())
                .orElseThrow();

        List<UpdateProductOptionRequest> newOptions = List.of(
                updateOptionRequest("빨강", "S", 10, 1000),
                updateOptionRequest("빨강", "M", 1, 1000),
                updateOptionRequest("빨강", "L", 9, 2000)
        );

        UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                .name("수정 테스트")
                .description("수정 테스트 description")
                .price(new BigDecimal("5000"))
                .options(newOptions)
                .build();

        // when
        ProductResponse updateResponse = productService.update(savedProduct.getId(), updateRequest);
        Product updateProduct = productRepository.findById(updateResponse.getId()).orElseThrow();

        // then
        assertThat(updateProduct).isNotNull();

        List<ProductOption> updateProductOptions = updateProduct.getOptions();
        assertThat(updateProductOptions).hasSize(3);
        assertThat(updateProductOptions)
                .extracting("color", "size", "stock")
                .containsExactlyInAnyOrder(
                        tuple("빨강", "S", 10),
                        tuple("빨강", "M", 1),
                        tuple("빨강", "L", 9)
                );
    }

    @Test
    @DisplayName("상품 카테고리 수정 성공")
    void update_product_category() {
        // given
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
        Product savedProduct = productRepository.findById(response.getId())
                .orElseThrow();

        Category updateCategory = Category.builder()
                .name("수정 테스트 카테고리")
                .description("수정 테스트용 카테고리입니다")
                .build();
        categoryRepository.save(updateCategory);

        Category updateChildCategory = Category.builder()
                .parent(updateCategory)
                .name("수정 자식 테스트 카테고리")
                .description("수정 테스트용 카테고리입니다")
                .build();
        categoryRepository.save(updateChildCategory);

        UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                .name("수정 테스트")
                .description("수정 테스트 description")
                .price(new BigDecimal("5000"))
                .categoryIds(List.of(updateChildCategory.getId()))
                .build();

        // when
        ProductResponse updateResponse = productService.update(savedProduct.getId(), updateRequest);
        Product updateProduct = productRepository.findById(updateResponse.getId()).orElseThrow();

        // then
        assertThat(updateProduct).isNotNull();
        assertThat(updateProduct.getCategoryProducts()).hasSize(1);
        assertThat(updateProduct.getCategoryProducts().get(0).getCategory().getId())
                .isEqualTo(updateChildCategory.getId());
    }

    @Test
    @DisplayName("상품 전체 수정 성공")
    void update_product_all() {
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
        ProductResponse response = productService.create(request);
        Product savedProduct = productRepository.findById(response.getId())
                .orElseThrow();

        Category updateCategory = Category.builder()
                .name("수정 테스트 카테고리")
                .description("수정 테스트용 카테고리입니다")
                .build();
        categoryRepository.save(testCategory);

        List<UpdateProductOptionRequest> updateOptions = List.of(
                updateOptionRequest("빨강", "S", 1, 1000),
                updateOptionRequest("빨강", "M", 18, 0),
                updateOptionRequest("빨강", "L", 1, 0)
        );

        UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                .name("수정 테스트")
                .description("수정 테스트 description")
                .price(new BigDecimal("5000"))
                .categoryIds(List.of(updateCategory.getId()))
                .options(updateOptions)
                .build();

        // when
        ProductResponse updateResponse = productService.update(savedProduct.getId(), updateRequest);
        Product updateProduct = productRepository.findById(updateResponse.getId()).orElseThrow();

        // then
        assertThat(updateProduct).isNotNull();
        assertThat(updateProduct.getName()).isEqualTo("수정 테스트");
        assertThat(updateProduct.getDescription()).isEqualTo("수정 테스트 description");
        assertThat(updateProduct.getPrice()).isEqualTo(new BigDecimal("5000"));

        List<ProductOption> updateProductOptions = updateProduct.getOptions();
        assertThat(updateProductOptions).hasSize(3);
        assertThat(updateProductOptions)
                .extracting("color", "size", "stock")
                .containsExactlyInAnyOrder(
                        tuple("빨강", "S", 1),
                        tuple("빨강", "M", 18),
                        tuple("빨강", "L", 1)
                );
        assertThat(updateProduct.getTotalStock()).isEqualTo(20);

        assertThat(updateProduct.getCategoryProducts()).hasSize(1);
        assertThat(updateProduct.getCategoryProducts().get(0).getCategory().getId())
                .isEqualTo(updateCategory.getId());
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void delete_product_success() {
        // given
        Product savedProduct = createAndSaveProduct(5);

        // when
        Long deletedId = productService.deleteProduct(savedProduct.getId());

        // then
        assertThat(deletedId).isEqualTo(savedProduct.getId());
        assertThat(productRepository.findById(savedProduct.getId())).isEmpty();
    }


    @Test
    @DisplayName("상품 삭제 실패 - CANNOT_DELETE_ORDER_COMPLETED")
    void delete_product_fail() {
        // given
        Product savedProduct = createAndSaveProduct(10);

        // 테스트용 유저 생성
        User user = User.builder()
                .email("test@test.com")
                .passwordHash("password")
                .username("테스터")
                .build();
        userRepository.save(user);

        // 배송지 생성
        Address address = Address.builder()
                .zipCode("12345")
                .city("서울시")
                .street("강남대로 123")
                .build();

        DeliveryAddress deliveryAddress = DeliveryAddress.builder()
                .recipientName("테스터")
                .recipientPhone("010-1234-5678")
                .recipientEmail("test@test.com")
                .address(address)
                .build();

        // 주문 생성 (COMPLETED 상태)
        ProductOption option = savedProduct.getOptions().get(0);
        OrderProduct orderProduct = OrderProduct.createOrderProduct(
                savedProduct,
                option.getColor(),
                option.getSize(),
                savedProduct.getPrice(),
                1
        );

        Order order = Order.createOrder(user, deliveryAddress, List.of(orderProduct), null);
        order.complete();  // 주문 완료 처리
        orderRepository.save(order);

        // when & then
        assertThatThrownBy(() -> productService.deleteProduct(savedProduct.getId()))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", ServiceExceptionCode.CANNOT_CANCEL_ORDER_CANCELED);

    }

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

    private UpdateProductOptionRequest updateOptionRequest(String color,
                                                           String size,
                                                           int stock,
                                                           int additionalPrice
    ) {
        return UpdateProductOptionRequest.builder()
                .color(color)
                .size(size)
                .stock(stock)
                .additionalPrice(new BigDecimal(additionalPrice))
                .build();
    }

    private Product createAndSaveProduct(int stock) {
        List<CreateProductRequest.ProductOptionRequest> options = List.of(
                createOptionRequest(null, null, stock, 0)
        );

        CreateProductRequest request = CreateProductRequest.builder()
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .price(new BigDecimal("10000"))
                .categoryIds(List.of(testCategory.getId()))
                .options(options)
                .build();

        ProductResponse response = productService.create(request);
        return productRepository.findById(response.getId()).orElseThrow();
    }
}
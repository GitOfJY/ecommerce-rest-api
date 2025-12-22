package com.jy.shoppy.domain.prodcut.service;

import com.jy.shoppy.domain.auth.dto.RegisterUserRequest;
import com.jy.shoppy.domain.auth.dto.RegisterUserResponse;
import com.jy.shoppy.domain.auth.service.AuthService;
import com.jy.shoppy.domain.category.entity.Category;
import com.jy.shoppy.domain.category.repository.CategoryRepository;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import com.jy.shoppy.domain.order.repository.OrderRepository;
import com.jy.shoppy.domain.prodcut.dto.CreateProductRequest;
import com.jy.shoppy.domain.prodcut.dto.ProductResponse;
import com.jy.shoppy.domain.prodcut.dto.UpdateProductOptionRequest;
import com.jy.shoppy.domain.prodcut.dto.UpdateProductRequest;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.entity.ProductOption;
import com.jy.shoppy.domain.prodcut.entity.type.StockStatus;
import com.jy.shoppy.domain.prodcut.repository.ProductOptionRepository;
import com.jy.shoppy.domain.prodcut.repository.ProductRepository;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.global.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Transactional
@Slf4j
class ProductServiceAdminTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

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

    @Nested
    @DisplayName("상품 등록 테스트")
    class CreateProductTest {

        @Test
        @DisplayName("카테고리와 옵션을 포함한 상품 등록 성공")
        void create_product_with_category_and_options() {
            // given
            List<CreateProductRequest.ProductOptionRequest> options = List.of(
                    createOptionRequest("빨강", "S", 10, 1000),
                    createOptionRequest("빨강", "M", 1, 1000),
                    createOptionRequest("빨강", "L", 9, 2000)
            );

            CreateProductRequest request = createProductRequest(
                    "테스트 상품",
                    "테스트 상품 설명",
                    new BigDecimal("10000"),
                    options,
                    List.of(testCategory.getId())
            );

            // when
            ProductResponse response = productService.create(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotNull();

            Product savedProduct = productRepository.findById(response.getId()).orElseThrow();

            assertProductBasicInfo(savedProduct, "테스트 상품", "테스트 상품 설명", new BigDecimal("10000"));
            assertProductOptions(savedProduct, 3, 20);
            assertThat(savedProduct.getStockStatus()).isEqualTo(StockStatus.IN_STOCK);
            assertProductCategory(savedProduct, testCategory.getId());
        }

        @Test
        @DisplayName("옵션 없이 상품 등록 성공")
        void create_product_without_options() {
            // given
            CreateProductRequest request = createProductRequestWithSingleOption(
                    "테스트 상품",
                    "테스트 상품 설명",
                    new BigDecimal("10000"),
                    null, null, 10, 0
            );

            // when
            ProductResponse response = productService.create(request);

            // then
            Product savedProduct = productRepository.findById(response.getId()).orElseThrow();

            assertProductBasicInfo(savedProduct, "테스트 상품", "테스트 상품 설명", new BigDecimal("10000"));
            assertThat(savedProduct.getOptions()).hasSize(1);
            assertThat(savedProduct.getTotalStock()).isEqualTo(10);
            assertThat(savedProduct.getStockStatus()).isEqualTo(StockStatus.IN_STOCK);
            assertProductCategory(savedProduct, testCategory.getId());
        }

        @Test
        @DisplayName("재고 부족 상태로 상품 등록 성공")
        void create_product_with_low_stock() {
            // given
            CreateProductRequest request = createProductRequestWithSingleOption(
                    "테스트 상품",
                    "테스트 상품 설명",
                    new BigDecimal("10000"),
                    null, null, 1, 0
            );

            // when
            ProductResponse response = productService.create(request);

            // then
            Product savedProduct = productRepository.findById(response.getId()).orElseThrow();

            assertProductBasicInfo(savedProduct, "테스트 상품", "테스트 상품 설명", new BigDecimal("10000"));
            assertThat(savedProduct.getStockStatus()).isEqualTo(StockStatus.LOW_STOCK);
            assertProductCategory(savedProduct, testCategory.getId());
        }

        @Test
        @DisplayName("품절 상태로 상품 등록 성공")
        void create_product_with_out_of_stock() {
            // given
            CreateProductRequest request = createProductRequestWithSingleOption(
                    "테스트 상품",
                    "테스트 상품 설명",
                    new BigDecimal("10000"),
                    null, null, 0, 0
            );

            // when
            ProductResponse response = productService.create(request);

            // then
            Product savedProduct = productRepository.findById(response.getId()).orElseThrow();

            assertProductBasicInfo(savedProduct, "테스트 상품", "테스트 상품 설명", new BigDecimal("10000"));
            assertThat(savedProduct.getStockStatus()).isEqualTo(StockStatus.OUT_OF_STOCK);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리로 상품 등록 실패")
        void create_product_with_invalid_category() {
            // given
            CreateProductRequest request = createProductRequestWithSingleOption(
                    "테스트 상품",
                    "테스트 설명",
                    new BigDecimal("10000"),
                    null, null, 10, 0
            );
            request = CreateProductRequest.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .price(request.getPrice())
                    .options(request.getOptions())
                    .categoryIds(List.of(999L))
                    .build();

            // when & then
            CreateProductRequest finalRequest = request;
            assertThatThrownBy(() -> productService.create(finalRequest))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("상품 수정 테스트")
    class UpdateProductTest {

        @Test
        @DisplayName("기본 정보 수정 성공")
        void update_product_basic_info() {
            // given
            Product savedProduct = createAndSaveProduct(0);

            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .name("수정 테스트")
                    .description("수정 테스트 description")
                    .price(new BigDecimal("5000"))
                    .build();

            // when
            ProductResponse updateResponse = productService.update(savedProduct.getId(), updateRequest);

            // then
            Product updatedProduct = productRepository.findById(updateResponse.getId()).orElseThrow();

            assertProductBasicInfo(updatedProduct, "수정 테스트", "수정 테스트 description", new BigDecimal("5000"));
        }

        @Test
        @DisplayName("존재하지 않는 상품 수정 실패")
        void update_nonexistent_product() {
            // given
            UpdateProductRequest request = UpdateProductRequest.builder()
                    .name("수정")
                    .build();

            // when & then
            assertThatThrownBy(() -> productService.update(999L, request))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("옵션 수정 성공")
        void update_product_options() {
            // given
            Product savedProduct = createAndSaveProduct(0);

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

            // then
            Product updatedProduct = productRepository.findById(updateResponse.getId()).orElseThrow();

            assertThat(updatedProduct).isNotNull();
            assertProductOptions(updatedProduct, 3, 20);
        }

        @Test
        @DisplayName("카테고리 수정 성공")
        void update_product_category() {
            // given
            Product savedProduct = createAndSaveProduct(0);

            Category updateCategory = createAndSaveCategory("수정 테스트 카테고리", "수정 테스트용", null);
            Category updateChildCategory = createAndSaveCategory(
                    "수정 자식 테스트 카테고리",
                    "수정 테스트용",
                    updateCategory
            );

            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .name("수정 테스트")
                    .description("수정 테스트 description")
                    .price(new BigDecimal("5000"))
                    .categoryIds(List.of(updateChildCategory.getId()))
                    .build();

            // when
            ProductResponse updateResponse = productService.update(savedProduct.getId(), updateRequest);

            // then
            Product updatedProduct = productRepository.findById(updateResponse.getId()).orElseThrow();

            assertThat(updatedProduct).isNotNull();
            assertProductCategory(updatedProduct, updateChildCategory.getId());
        }

        @Test
        @DisplayName("전체 정보 수정 성공")
        void update_product_all() {
            // given
            List<CreateProductRequest.ProductOptionRequest> options = List.of(
                    createOptionRequest("빨강", "S", 10, 1000),
                    createOptionRequest("빨강", "M", 1, 1000),
                    createOptionRequest("빨강", "L", 9, 2000)
            );

            CreateProductRequest request = createProductRequest(
                    "테스트 상품",
                    "테스트 상품 설명",
                    new BigDecimal("10000"),
                    options,
                    List.of(testCategory.getId())
            );
            ProductResponse response = productService.create(request);
            Product savedProduct = productRepository.findById(response.getId()).orElseThrow();

            Category updateCategory = createAndSaveCategory("수정 테스트 카테고리", "수정 테스트용", null);

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

            // then
            Product updatedProduct = productRepository.findById(updateResponse.getId()).orElseThrow();

            assertThat(updatedProduct).isNotNull();
            assertProductBasicInfo(updatedProduct, "수정 테스트", "수정 테스트 description", new BigDecimal("5000"));
            assertProductCategory(updatedProduct, updateCategory.getId());

            assertThat(updatedProduct.getOptions())
                    .extracting("color", "size", "stock")
                    .containsExactlyInAnyOrder(
                            tuple("빨강", "S", 1),
                            tuple("빨강", "M", 18),
                            tuple("빨강", "L", 1)
                    );
        }
    }

    @Nested
    @DisplayName("상품 삭제 테스트")
    class DeleteProductTest {

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
        @DisplayName("주문 완료된 상품 삭제 실패")
        // TODO : order Test 후 수정
        void delete_product_with_completed_order() {
            // given
            Product savedProduct = createAndSaveProduct(10);
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111", 1L);
            Order order = createTestOrder(user, OrderStatus.COMPLETED);
            createTestOrderProduct(savedProduct, order, 1);

            // when & then
            assertThatThrownBy(() -> productService.deleteProduct(savedProduct.getId()))
                    .isInstanceOf(ServiceException.class);
        }
    }



    private CreateProductRequest.ProductOptionRequest createOptionRequest(
            String color, String size, int stock, int additionalPrice) {
        return CreateProductRequest.ProductOptionRequest.builder()
                .color(color)
                .size(size)
                .stock(stock)
                .additionalPrice(new BigDecimal(additionalPrice))
                .build();
    }

    private UpdateProductOptionRequest updateOptionRequest(
            String color, String size, int stock, int additionalPrice) {
        return UpdateProductOptionRequest.builder()
                .color(color)
                .size(size)
                .stock(stock)
                .additionalPrice(new BigDecimal(additionalPrice))
                .build();
    }

    private CreateProductRequest createProductRequest(
            String name, String description, BigDecimal price,
            List<CreateProductRequest.ProductOptionRequest> options,
            List<Long> categoryIds) {
        return CreateProductRequest.builder()
                .name(name)
                .description(description)
                .price(price)
                .options(options)
                .categoryIds(categoryIds)
                .build();
    }

    private CreateProductRequest createProductRequestWithSingleOption(
            String name, String description, BigDecimal price,
            String color, String size, int stock, int additionalPrice) {
        List<CreateProductRequest.ProductOptionRequest> options = List.of(
                createOptionRequest(color, size, stock, additionalPrice)
        );
        return createProductRequest(name, description, price, options, List.of(testCategory.getId()));
    }

    private Product createAndSaveProduct(int stock) {
        CreateProductRequest request = createProductRequestWithSingleOption(
                "테스트 상품",
                "테스트 상품 설명",
                new BigDecimal("10000"),
                null, null, stock, 0
        );

        ProductResponse response = productService.create(request);
        return productRepository.findById(response.getId()).orElseThrow();
    }

    private Category createAndSaveCategory(String name, String description, Category parent) {
        Category category = Category.builder()
                .name(name)
                .description(description)
                .parent(parent)
                .build();
        return categoryRepository.save(category);
    }

    private User createTestUser(String username, String email, String phone, Long roleId) {
        RegisterUserRequest req = RegisterUserRequest.builder()
                .username(username)
                .email(email)
                .password("Test1234@")
                .phone(phone)
                .roleId(roleId)
                .build();
        RegisterUserResponse response = authService.register(req);
        return userRepository.findById(response.getId()).orElseThrow();
    }

    private Order createTestOrder(User user, OrderStatus status) {
        Order order = Order.builder()
                .user(user)
                .status(status)
                .orderDate(LocalDateTime.now())
                .totalPrice(BigDecimal.valueOf(10000))
                .build();
        return orderRepository.save(order);
    }

    private void createTestOrderProduct(Product product, Order order, int quantity) {
        OrderProduct orderProduct = OrderProduct.builder()
                .product(product)
                .order(order)
                .quantity(quantity)
                .build();
        // return orderProductRepository.save(orderProduct);
    }

    private void assertProductBasicInfo(Product product, String name, String description, BigDecimal price) {
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getDescription()).isEqualTo(description);
        assertThat(product.getPrice()).isEqualByComparingTo(price);
    }

    private void assertProductOptions(Product product, int optionCount, int totalStock) {
        List<ProductOption> options = product.getOptions();
        assertThat(options).hasSize(optionCount);

        if (optionCount == 3) {
            assertThat(options)
                    .extracting("color", "size", "stock")
                    .containsExactlyInAnyOrder(
                            tuple("빨강", "S", 10),
                            tuple("빨강", "M", 1),
                            tuple("빨강", "L", 9)
                    );
        }

        assertThat(product.getTotalStock()).isEqualTo(totalStock);
    }

    private void assertProductCategory(Product product, Long expectedCategoryId) {
        assertThat(product.getCategoryProducts()).hasSize(1);
        assertThat(product.getCategoryProducts().get(0).getCategory().getId())
                .isEqualTo(expectedCategoryId);
    }
}
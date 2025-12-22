package com.jy.shoppy.domain.cart.service;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.auth.dto.RegisterUserRequest;
import com.jy.shoppy.domain.auth.dto.RegisterUserResponse;
import com.jy.shoppy.domain.auth.service.AuthService;
import com.jy.shoppy.domain.cart.dto.CartProductRequest;
import com.jy.shoppy.domain.cart.entity.Cart;
import com.jy.shoppy.domain.cart.repository.CartRepository;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.entity.ProductOption;
import com.jy.shoppy.domain.prodcut.repository.ProductOptionRepository;
import com.jy.shoppy.domain.prodcut.repository.ProductRepository;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
class CartServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    private User testUser;
    private Product testProductNoOption;
    private Product testProductWithOption;

    @BeforeEach
    void init() {
        testUser = createTestUser();
        testProductNoOption = createTestProductNoOption();
        testProductWithOption = createTestProductWithOptions();
    }

    @Nested
    @DisplayName("[회원] 테스트")
    class UserTest {
        @Test
        @DisplayName("장바구니 상품 추가")
        void user_add_cart_product() {
            // given - testUser, testProductNoOption, testProductWithOption
            Account user = creatAccount(testUser);

            CartProductRequest requestNoOption = CartProductRequest.builder()
                    .productId(testProductNoOption.getId())
                    .quantity(1)
                    .build();

            CartProductRequest requestOption = CartProductRequest.builder()
                    .productId(testProductWithOption.getId())
                    .color("빨강")
                    .size("S")
                    .quantity(1)
                    .build();

            // when
            cartService.addToCart(user, requestNoOption, null);
            cartService.addToCart(user, requestOption, null);

            // then
            Optional<Cart> cart = cartRepository.findByUserId(testUser.getId());
            assertThat(cart.isPresent()).isTrue();
            assertThat(cart.get().getUser().getUsername()).isEqualTo(testUser.getUsername());
            assertThat(cart.get().getCartProducts().size()).isEqualTo(2);
        }

        // 장바구니 목록 조회 API
        // 장바구니 옵션 수정 API
        // 장바구니 상품 삭제 API
        //장바구니 상품 전체 삭제 API

    }

    @Nested
    @DisplayName("[비회원] 테스트")
    class GuestTest {

    }

    private User createTestUser() {
        RegisterUserRequest req = RegisterUserRequest.builder()
                .username("testUser")
                .email("testUser@test.com")
                .password("Test1234@")
                .phone("010-9999-9999")
                .roleId(1L)
                .build();
        RegisterUserResponse response = authService.register(req);
        return userRepository.findById(response.getId()).get();
    }

    private Product createTestProductNoOption() {
        Product product = Product.builder()
                .name("기본 티셔츠")
                .description("옵션이 없는 기본 티셔츠입니다.")
                .price(BigDecimal.valueOf(29000))
                .build();
        return productRepository.save(product);
    }

    private Product createTestProductWithOptions() {
        Product product = Product.builder()
                .name("옵션 티셔츠")
                .description("색상과 사이즈를 선택할 수 있는 티셔츠입니다.")
                .price(BigDecimal.valueOf(39000))
                .build();

        Product savedProduct = productRepository.save(product);

        // 옵션 생성: 빨강 × S, M, L
        createProductOption(savedProduct, "빨강", "S", 10);
        createProductOption(savedProduct, "빨강", "M", 20);
        createProductOption(savedProduct, "빨강", "L", 15);

        // 옵션 생성: 파랑 × S, M, L
        createProductOption(savedProduct, "파랑", "S", 8);
        createProductOption(savedProduct, "파랑", "M", 25);
        createProductOption(savedProduct, "파랑", "L", 12);

        // 옵션 생성: 검정 × S, M, L
        createProductOption(savedProduct, "검정", "S", 30);
        createProductOption(savedProduct, "검정", "M", 50);
        createProductOption(savedProduct, "검정", "L", 40);

        return savedProduct;
    }

    private void createProductOption(Product product, String color, String size, int stock) {
        ProductOption option = ProductOption.builder()
                .product(product)
                .color(color)
                .size(size)
                .stock(stock)
                .additionalPrice(BigDecimal.ZERO)
                .build();

        productOptionRepository.save(option);
    }

    private Account creatAccount(User user) {
        return Account.builder()
                .accountId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }
}
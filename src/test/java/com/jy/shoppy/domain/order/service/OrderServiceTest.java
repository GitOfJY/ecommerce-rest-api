package com.jy.shoppy.domain.order.service;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.auth.dto.RegisterUserRequest;
import com.jy.shoppy.domain.auth.dto.RegisterUserResponse;
import com.jy.shoppy.domain.auth.service.AuthService;
import com.jy.shoppy.domain.order.dto.*;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import com.jy.shoppy.domain.order.repository.OrderRepository;
import com.jy.shoppy.domain.prodcut.dto.OrderProductRequest;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.entity.ProductOption;
import com.jy.shoppy.domain.prodcut.repository.ProductOptionRepository;
import com.jy.shoppy.domain.prodcut.repository.ProductRepository;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.global.exception.ServiceException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class OrderServiceTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User testUser;
    private Product testProductNoOption;
    private Product testProductWithOption;
    private ProductOption redSmallOption;
    private ProductOption redMediumOption;

    @BeforeEach
    void init() {
        testUser = createTestUser();
        testProductNoOption = createTestProductNoOption();
        testProductWithOption = createTestProductWithOptions();

        // 테스트용 옵션 조회
        redSmallOption = productOptionRepository
                .findByProductIdAndColorAndSize(testProductWithOption.getId(), "빨강", "S")
                .orElseThrow();
        redMediumOption = productOptionRepository
                .findByProductIdAndColorAndSize(testProductWithOption.getId(), "빨강", "M")
                .orElseThrow();
    }

    @Nested
    @DisplayName("[회원] 주문 테스트")
    class MemberOrderTest {
        @Test
        @DisplayName("회원 주문 생성 - 새 배송지")
        void create_member_order_with_new_address() {
            // given
            Account account = createAccount(testUser);

            OrderProductRequest productRequest = OrderProductRequest.builder()
                    .productId(testProductWithOption.getId())
                    .color("빨강")
                    .size("S")
                    .quantity(2)
                    .build();

            CreateMemberOrderRequest request = CreateMemberOrderRequest.builder()
                    .products(List.of(productRequest))
                    .deliveryAddressId(null)  // 새 배송지
                    .recipientName("홍길동")
                    .recipientEmail("hong@example.com")
                    .recipientPhone("010-1234-5678")
                    .zipCode("06234")
                    .city("서울시")
                    .street("강남구 테헤란로 123")
                    .detail("101동 202호")
                    .build();

            // when
            OrderResponse response = orderService.create(account, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getOrderNumber()).isNotNull();
            assertThat(response.getOrderNumber()).startsWith("ORD");
            assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(response.getTotalPrice()).isEqualTo(
                    redSmallOption.getTotalPrice().multiply(BigDecimal.valueOf(2))
            );

            // 재고 확인
            ProductOption updatedOption = productOptionRepository
                    .findByProductIdAndColorAndSize(testProductWithOption.getId(), "빨강", "S")
                    .orElseThrow();
            assertThat(updatedOption.getStock()).isEqualTo(10 - 2);  // 초기 재고 10개에서 2개 차감
        }

        @Test
        @DisplayName("회원 주문 조회")
        void find_member_order() {
            // given
            Account account = createAccount(testUser);
            OrderResponse createdOrder = createMemberOrder(account);

            // when
            OrderResponse foundOrder = orderService.findMyOrderById(account, createdOrder.getId());

            // then
            assertThat(foundOrder).isNotNull();
            assertThat(foundOrder.getId()).isEqualTo(createdOrder.getId());
            assertThat(foundOrder.getOrderNumber()).isEqualTo(createdOrder.getOrderNumber());
        }

        @Test
        @DisplayName("회원 주문 목록 조회")
        void find_member_orders() {
            // given
            Account account = createAccount(testUser);
            createMemberOrder(account);
            createMemberOrder(account);

            // when
            List<OrderResponse> orders = orderService.findMyOrders(account);

            // then
            assertThat(orders).hasSize(2);
        }

        @Test
        @DisplayName("회원 주문 취소")
        void cancel_member_order() {
            // given
            Account account = createAccount(testUser);
            OrderResponse createdOrder = createMemberOrder(account);

            // 초기 재고 확인
            ProductOption beforeOption = productOptionRepository
                    .findByProductIdAndColorAndSize(testProductWithOption.getId(), "빨강", "S")
                    .orElseThrow();
            int stockBeforeCancel = beforeOption.getStock();

            // when
            OrderResponse canceledOrder = orderService.cancelMyOrder(account, createdOrder.getId());

            // then
            assertThat(canceledOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);

            // 재고 복구 확인
            ProductOption afterOption = productOptionRepository
                    .findByProductIdAndColorAndSize(testProductWithOption.getId(), "빨강", "S")
                    .orElseThrow();
            assertThat(afterOption.getStock()).isEqualTo(stockBeforeCancel + 2);  // 2개 복구
        }

        @Test
        @DisplayName("회원 주문 생성 실패 - 재고 부족")
        void create_member_order_fail_out_of_stock() {
            // given
            Account account = createAccount(testUser);

            OrderProductRequest productRequest = OrderProductRequest.builder()
                    .productId(testProductWithOption.getId())
                    .color("빨강")
                    .size("S")
                    .quantity(100)  // 재고(10개)보다 많은 수량
                    .build();
            CreateMemberOrderRequest request = CreateMemberOrderRequest.builder()
                    .products(List.of(productRequest))
                    .recipientName("홍길동")
                    .recipientEmail("hong@example.com")
                    .recipientPhone("010-1234-5678")
                    .zipCode("06234")
                    .city("서울시")
                    .street("강남구 테헤란로 123")
                    .build();

            // when & then
            assertThatThrownBy(() -> orderService.create(account, request))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("[비회원] 주문 테스트")
    class GuestOrderTest {
        @Test
        @DisplayName("비회원 주문 생성")
        void create_guest_order() {
            // given
            OrderProductRequest productRequest = OrderProductRequest.builder()
                    .productId(testProductWithOption.getId())
                    .color("빨강")
                    .size("M")
                    .quantity(1)
                    .build();

            CreateGuestOrderRequest request = CreateGuestOrderRequest.builder()
                    .products(List.of(productRequest))
                    .guestPassword("guest1234")
                    .name("김손님")
                    .email("guest@example.com")
                    .phone("010-9999-8888")
                    .zipCode("06234")
                    .city("서울시")
                    .street("강남구 테헤란로 456")
                    .detail("303호")
                    .build();

            // when
            OrderResponse response = orderService.createGuestOrder(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getOrderNumber()).isNotNull();
            assertThat(response.getOrderNumber()).startsWith("ORD");
            assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.PENDING);

            // 재고 확인
            ProductOption updatedOption = productOptionRepository
                    .findByProductIdAndColorAndSize(testProductWithOption.getId(), "빨강", "M")
                    .orElseThrow();
            assertThat(updatedOption.getStock()).isEqualTo(20 - 1);
        }

        @Test
        @DisplayName("비회원 주문 조회")
        void find_guest_order() {
            // given
            OrderResponse createdOrder = createGuestOrder();

            GuestOrderRequest request = new GuestOrderRequest(
                    createdOrder.getOrderNumber(),
                    "김손님",
                    "guest1234"
            );

            // when
            OrderResponse foundOrder = orderService.findGuestOrder(request);

            // then
            assertThat(foundOrder).isNotNull();
            assertThat(foundOrder.getOrderNumber()).isEqualTo(createdOrder.getOrderNumber());
        }

        @Test
        @DisplayName("비회원 주문 조회 실패 - 잘못된 주문자명")
        void find_guest_order_fail_wrong_name() {
            // given
            OrderResponse createdOrder = createGuestOrder();

            GuestOrderRequest request = new GuestOrderRequest(
                    createdOrder.getOrderNumber(),
                    "잘못된이름",
                    "guest1234"
            );

            // when & then
            assertThatThrownBy(() -> orderService.findGuestOrder(request))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("비회원 주문 조회 실패 - 잘못된 비밀번호")
        void find_guest_order_fail_wrong_password() {
            // given
            OrderResponse createdOrder = createGuestOrder();

            GuestOrderRequest request = new GuestOrderRequest(
                    createdOrder.getOrderNumber(),
                    "김손님",
                    "wrongpassword"
            );

            // when & then
            assertThatThrownBy(() -> orderService.findGuestOrder(request))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("비회원 주문 취소")
        void cancel_guest_order() {
            // given
            OrderResponse createdOrder = createGuestOrder();

            // 초기 재고 확인
            ProductOption beforeOption = productOptionRepository
                    .findByProductIdAndColorAndSize(testProductWithOption.getId(), "빨강", "M")
                    .orElseThrow();
            int stockBeforeCancel = beforeOption.getStock();

            GuestOrderCancelRequest request = new GuestOrderCancelRequest(
                    createdOrder.getOrderNumber(),
                    "김손님",
                    "guest1234"
            );

            // when
            OrderResponse canceledOrder = orderService.cancelGuestOrder(request);

            // then
            assertThat(canceledOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);

            // 재고 복구 확인
            ProductOption afterOption = productOptionRepository
                    .findByProductIdAndColorAndSize(testProductWithOption.getId(), "빨강", "M")
                    .orElseThrow();
            assertThat(afterOption.getStock()).isEqualTo(stockBeforeCancel + 1);
        }

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
        return userRepository.findById(response.getId()).orElseThrow();
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

        return savedProduct;
    }

    private void createProductOption(Product product, String color, String size, int stock) {
        ProductOption option = ProductOption.builder()
                .product(product)
                .color(color)
                .size(size)
                .stock(stock)
                .additionalPrice(BigDecimal.valueOf(1000))
                .build();
        productOptionRepository.save(option);
    }

    private Account createAccount(User user) {
        return Account.builder()
                .accountId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    private OrderResponse createMemberOrder(Account account) {
        OrderProductRequest productRequest = OrderProductRequest.builder()
                .productId(testProductWithOption.getId())
                .color("빨강")
                .size("S")
                .quantity(2)
                .build();

        CreateMemberOrderRequest request = CreateMemberOrderRequest.builder()
                .products(List.of(productRequest))
                .recipientName("홍길동")
                .recipientEmail("hong@example.com")
                .recipientPhone("010-1234-5678")
                .zipCode("06234")
                .city("서울시")
                .street("강남구 테헤란로 123")
                .detail("101동 202호")
                .build();

        return orderService.create(account, request);
    }

    private OrderResponse createGuestOrder() {
        OrderProductRequest productRequest = OrderProductRequest.builder()
                .productId(testProductWithOption.getId())
                .color("빨강")
                .size("M")
                .quantity(1)
                .build();

        CreateGuestOrderRequest request = CreateGuestOrderRequest.builder()
                .products(List.of(productRequest))
                .guestPassword("guest1234")
                .name("김손님")
                .email("guest@example.com")
                .phone("010-9999-8888")
                .zipCode("06234")
                .city("서울시")
                .street("강남구 테헤란로 456")
                .detail("303호")
                .build();

        return orderService.createGuestOrder(request);
    }
}
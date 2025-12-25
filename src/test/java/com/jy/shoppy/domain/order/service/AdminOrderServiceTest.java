package com.jy.shoppy.domain.order.service;

import com.jy.shoppy.domain.address.entity.Address;
import com.jy.shoppy.domain.address.entity.DeliveryAddress;
import com.jy.shoppy.domain.address.repository.DeliveryAddressRepository;
import com.jy.shoppy.domain.auth.dto.RegisterUserRequest;
import com.jy.shoppy.domain.auth.dto.RegisterUserResponse;
import com.jy.shoppy.domain.auth.service.AuthService;
import com.jy.shoppy.domain.category.entity.Category;
import com.jy.shoppy.domain.category.repository.CategoryRepository;
import com.jy.shoppy.domain.guest.entity.Guest;
import com.jy.shoppy.domain.guest.repository.GuestRepository;
import com.jy.shoppy.domain.order.dto.OrderResponse;
import com.jy.shoppy.domain.order.dto.SearchOrderCond;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import com.jy.shoppy.domain.order.repository.OrderRepository;
import com.jy.shoppy.domain.prodcut.dto.CreateProductRequest;
import com.jy.shoppy.domain.prodcut.dto.ProductResponse;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.repository.ProductRepository;
import com.jy.shoppy.domain.prodcut.service.ProductService;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.entity.UserGrade;
import com.jy.shoppy.domain.user.repository.UserGradeRepository;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.global.exception.ServiceException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Slf4j
class AdminOrderServiceTest {

    @Autowired
    private AdminOrderService adminOrderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGradeRepository userGradeRepository;

    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Category testCategory;

    @BeforeEach
    void init() {
        // 테스트용 카테고리 생성
        testCategory = Category.builder()
                .name("테스트 카테고리")
                .description("테스트용 카테고리입니다")
                .build();
        categoryRepository.save(testCategory);
}

    @Nested
    @DisplayName("주문 조회 테스트")
    class FindOrderTest {
        @Test
        @DisplayName("주문 ID로 상세 조회 성공")
        void findById_success() {
            // given
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111");
            DeliveryAddress address = createTestDeliveryAddress(user);
            Order order = createTestMemberOrder(user, address, OrderStatus.PENDING, new BigDecimal("50000"));

            // when
            OrderResponse response = adminOrderService.findById(order.getId());

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(order.getId());
            assertThat(response.getOrderNumber()).isEqualTo(order.getOrderNumber());
            assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(response.getTotalPrice()).isEqualByComparingTo(new BigDecimal("50000"));
        }

        @Test
        @DisplayName("존재하지 않는 주문 조회 실패")
        void findById_notFound() {
            // when & then
            assertThatThrownBy(() -> adminOrderService.findById(999L))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("전체 주문 조회 성공")
        void findAll_success() {
            // given
            User user1 = createTestUser("user1", "user1@test.com", "010-1111-1111");
            User user2 = createTestUser("user2", "user2@test.com", "010-2222-2222");

            DeliveryAddress address1 = createTestDeliveryAddress(user1);
            DeliveryAddress address2 = createTestDeliveryAddress(user2);

            createTestMemberOrder(user1, address1, OrderStatus.PENDING, new BigDecimal("30000"));
            createTestMemberOrder(user2, address2, OrderStatus.COMPLETED, new BigDecimal("50000"));

            Guest guest = createTestGuest();
            createTestGuestOrder(guest, OrderStatus.PENDING, new BigDecimal("20000"));

            // when
            List<OrderResponse> orders = adminOrderService.findAll();

            // then
            assertThat(orders).hasSizeGreaterThanOrEqualTo(3);
        }
    }

    @Nested
    @DisplayName("주문 완료 처리 테스트")
    class CompleteOrderTest {

        @Test
        @DisplayName("회원 주문 완료 처리 - 등급 유지")
        void complete_memberOrder_gradeNotChanged() {
            // given
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111");
            DeliveryAddress address = createTestDeliveryAddress(user);
            Order order = createTestMemberOrder(user, address, OrderStatus.PENDING, new BigDecimal("50000"));

            BigDecimal beforeTotalAmount = user.getTotalPurchaseAmount();
            UserGrade beforeGrade = user.getUserGrade();

            // when
            OrderResponse response = adminOrderService.complete(order.getId());

            // then
            User updatedUser = userRepository.findById(user.getId()).orElseThrow();

            assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
            assertThat(updatedUser.getTotalPurchaseAmount())
                    .isEqualByComparingTo(beforeTotalAmount.add(new BigDecimal("50000")));
            assertThat(updatedUser.getUserGrade().getId()).isEqualTo(beforeGrade.getId()); // BRONZE 유지
        }

        @Test
        @DisplayName("회원 주문 완료 처리 - BRONZE에서 SILVER로 등급 승급")
        void complete_memberOrder_upgradeToSilver() {
            // given
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111");
            DeliveryAddress address = createTestDeliveryAddress(user);
            Order order = createTestMemberOrder(user, address, OrderStatus.PENDING, new BigDecimal("150000"));

            // when
            OrderResponse response = adminOrderService.complete(order.getId());

            // then
            User updatedUser = userRepository.findById(user.getId()).orElseThrow();

            assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
            assertThat(updatedUser.getTotalPurchaseAmount()).isEqualByComparingTo(new BigDecimal("150000"));
            assertThat(updatedUser.getUserGrade().getName()).isEqualTo("SILVER");
        }

        @Test
        @DisplayName("회원 주문 완료 처리 - BRONZE에서 GOLD로 등급 승급")
        void complete_memberOrder_upgradeToGold() {
            // given
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111");
            DeliveryAddress address = createTestDeliveryAddress(user);
            Order order = createTestMemberOrder(user, address, OrderStatus.PENDING, new BigDecimal("600000"));

            // when
            OrderResponse response = adminOrderService.complete(order.getId());

            // then
            User updatedUser = userRepository.findById(user.getId()).orElseThrow();

            assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
            assertThat(updatedUser.getTotalPurchaseAmount()).isEqualByComparingTo(new BigDecimal("600000"));
            assertThat(updatedUser.getUserGrade().getName()).isEqualTo("VIP");
        }

        @Test
        @DisplayName("회원 주문 완료 처리 - 여러 번 주문하여 단계적 승급")
        void complete_memberOrder_multipleOrders() {
            // given
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111");
            DeliveryAddress address = createTestDeliveryAddress(user);

            Order order1 = createTestMemberOrder(user, address, OrderStatus.PENDING, new BigDecimal("80000"));
            Order order2 = createTestMemberOrder(user, address, OrderStatus.PENDING, new BigDecimal("80000"));
            Order order3 = createTestMemberOrder(user, address, OrderStatus.PENDING, new BigDecimal("400000"));

            // when
            adminOrderService.complete(order1.getId());

            // userGrade와 함께 조회
            User afterFirst = userRepository.findByIdWithGrade(user.getId()).orElseThrow();
            assertThat(afterFirst.getUserGrade().getName()).isEqualTo("BRONZE");

            adminOrderService.complete(order2.getId());
            User afterSecond = userRepository.findByIdWithGrade(user.getId()).orElseThrow();
            assertThat(afterSecond.getUserGrade().getName()).isEqualTo("SILVER");

            adminOrderService.complete(order3.getId());
            User afterThird = userRepository.findByIdWithGrade(user.getId()).orElseThrow();
            assertThat(afterThird.getUserGrade().getName()).isEqualTo("VIP");

            // then
            assertThat(afterThird.getTotalPurchaseAmount()).isEqualByComparingTo(new BigDecimal("560000"));
        }

        @Test
        @DisplayName("비회원 주문 완료 처리 성공")
        void complete_guestOrder_success() {
            // given
            Guest guest = createTestGuest();
            Order order = createTestGuestOrder(guest, OrderStatus.PENDING, new BigDecimal("30000"));

            // when
            OrderResponse response = adminOrderService.complete(order.getId());

            // then
            assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("이미 완료된 주문 재완료 시도 실패")
        void complete_alreadyCompleted() {
            // given
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111");
            DeliveryAddress address = createTestDeliveryAddress(user);
            Order order = createTestMemberOrder(user, address, OrderStatus.COMPLETED, new BigDecimal("50000"));

            // when & then
            assertThatThrownBy(() -> adminOrderService.complete(order.getId()))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("취소된 주문 완료 시도 실패")
        void complete_canceledOrder() {
            // given
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111");
            DeliveryAddress address = createTestDeliveryAddress(user);
            Order order = createTestMemberOrder(user, address, OrderStatus.CANCELED, new BigDecimal("50000"));

            // when & then
            assertThatThrownBy(() -> adminOrderService.complete(order.getId()))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("주문 취소 테스트")
    class CancelOrderTest {
        @Test
        @DisplayName("주문 취소 성공")
        void cancel_success() {
            // given
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111");
            DeliveryAddress address = createTestDeliveryAddress(user);
            Order order = createTestMemberOrder(user, address, OrderStatus.PENDING, new BigDecimal("50000"));

            // when
            OrderResponse response = adminOrderService.cancel(order.getId());

            // then
            assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
        }

        @Test
        @DisplayName("완료된 주문 취소 실패")
        void cancel_completedOrder() {
            // given
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111");
            DeliveryAddress address = createTestDeliveryAddress(user);
            Order order = createTestMemberOrder(user, address, OrderStatus.COMPLETED, new BigDecimal("50000"));

            // when & then
            assertThatThrownBy(() -> adminOrderService.cancel(order.getId()))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("주문 검색 테스트")
    class SearchOrderTest {
        @Test
        @DisplayName("주문 상태로 검색 성공")
        void searchByStatus() {
            // given
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111");
            DeliveryAddress address = createTestDeliveryAddress(user);

            createTestMemberOrder(user, address, OrderStatus.PENDING, new BigDecimal("30000"));
            createTestMemberOrder(user, address, OrderStatus.PENDING, new BigDecimal("40000"));
            createTestMemberOrder(user, address, OrderStatus.COMPLETED, new BigDecimal("50000"));

            SearchOrderCond cond = new SearchOrderCond(null, OrderStatus.PENDING, null, null);
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<OrderResponse> result = adminOrderService.searchOrdersPage(cond, pageable);

            // then
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
            assertThat(result.getContent())
                    .allMatch(order -> order.getOrderStatus() == OrderStatus.PENDING);
        }

        @Test
        @DisplayName("특정 사용자의 주문 검색 성공")
        void searchByUserId() {
            // given
            User user1 = createTestUser("user1", "user1@test.com", "010-1111-1111");
            User user2 = createTestUser("user2", "user2@test.com", "010-2222-2222");

            DeliveryAddress address1 = createTestDeliveryAddress(user1);
            DeliveryAddress address2 = createTestDeliveryAddress(user2);

            createTestMemberOrder(user1, address1, OrderStatus.PENDING, new BigDecimal("30000"));
            createTestMemberOrder(user1, address1, OrderStatus.COMPLETED, new BigDecimal("40000"));
            createTestMemberOrder(user2, address2, OrderStatus.PENDING, new BigDecimal("50000"));

            SearchOrderCond cond = new SearchOrderCond(user1.getId(), null, null, null);
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<OrderResponse> result = adminOrderService.searchOrdersPage(cond, pageable);

            // then
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
            assertThat(result.getContent())
                    .allMatch(order -> order.getUserName().equals(user1.getUsername()));
        }
    }

    private User createTestUser(String username, String email, String phone) {
        RegisterUserRequest req = RegisterUserRequest.builder()
                .username(username)
                .email(email)
                .password("Test1234@")
                .phone(phone)
                .roleId(1L)
                .build();
        RegisterUserResponse response = authService.register(req);
        return userRepository.findByIdWithGrade(response.getId()).orElseThrow();
    }

    private DeliveryAddress createTestDeliveryAddress(User user) {
        DeliveryAddress address = DeliveryAddress.builder()
                .user(user)
                .recipientName("테스트수령인")
                .recipientPhone("010-1234-5678")
                .recipientEmail("test@test.com")
                .address(Address.builder()
                        .zipCode("12345")
                        .city("서울시")
                        .street("강남구 테헤란로 123")
                        .detail("테스트빌딩 101호")
                        .build())
                .build();
        return deliveryAddressRepository.save(address);
    }

    private Guest createTestGuest() {
        Guest guest = Guest.builder()
                .name("비회원테스트")
                .email("guest@test.com")
                .phone("010-9999-9999")
                .zipcode("54321")
                .city("서울시")
                .street("서초구 서초대로 456")
                .detail("비회원빌딩 202호")
                .passwordHash(passwordEncoder.encode("guestpass123"))
                .build();
        return guestRepository.save(guest);
    }

    private Order createTestMemberOrder(User user, DeliveryAddress deliveryAddress, OrderStatus status, BigDecimal totalPrice) {
        Order order = Order.builder()
                .user(user)
                .guest(null)
                .deliveryAddress(deliveryAddress)
                .orderNumber(generateTestOrderNumber())
                .status(status)
                .orderDate(LocalDateTime.now())
                .totalPrice(totalPrice)
                .build();

        Product product = createAndSaveTestProduct(totalPrice);
        OrderProduct orderProduct = OrderProduct.builder()
                .order(order)
                .product(product)
                .selectedColor("Black")
                .selectedSize("M")
                .orderPrice(totalPrice)
                .quantity(1)
                .build();

        order.addOrderProduct(orderProduct);
        order.calculateAndSetTotalPrice();

        return orderRepository.save(order);
    }

    private Order createTestGuestOrder(Guest guest, OrderStatus status, BigDecimal totalPrice) {
        Order order = Order.builder()
                .user(null)
                .guest(guest)
                .deliveryAddress(null)
                .orderNumber(generateTestOrderNumber())
                .status(status)
                .orderDate(LocalDateTime.now())
                .totalPrice(totalPrice)
                .build();

        Product product = createAndSaveTestProduct(totalPrice);
        OrderProduct orderProduct = OrderProduct.builder()
                .order(order)
                .product(product)
                .selectedColor("Black")
                .selectedSize("M")
                .orderPrice(totalPrice)
                .quantity(1)
                .build();

        order.addOrderProduct(orderProduct);
        order.calculateAndSetTotalPrice();

        return orderRepository.save(order);
    }

    private Product createAndSaveTestProduct(BigDecimal price) {
        String uniqueName = "테스트 상품-" + UUID.randomUUID().toString().substring(0, 8);

        CreateProductRequest request = CreateProductRequest.builder()
                .name(uniqueName)
                .description("테스트용 상품입니다")
                .price(price)
                .options(List.of(
                        CreateProductRequest.ProductOptionRequest.builder()
                                .color("Black")
                                .size("M")
                                .stock(100)
                                .additionalPrice(BigDecimal.ZERO)
                                .build()
                ))
                .categoryIds(List.of(testCategory.getId()))
                .build();

        ProductResponse response = productService.create(request);
        return productRepository.findById(response.getId()).orElseThrow();
    }

    private String generateTestOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD" + datePart + randomPart;
    }
}
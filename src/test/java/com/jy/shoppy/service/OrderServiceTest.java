package com.jy.shoppy.service;

import com.jy.shoppy.domain.order.service.OrderService;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import com.jy.shoppy.domain.order.mapper.OrderMapper;
import com.jy.shoppy.domain.order.repository.OrderRepository;
import com.jy.shoppy.domain.prodcut.repository.ProductRepository;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.domain.order.dto.CreateOrderRequest;
import com.jy.shoppy.domain.prodcut.dto.OrderProductRequest;
import com.jy.shoppy.domain.order.dto.OrderResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    UserRepository userRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderMapper orderMapper;

    @InjectMocks
    OrderService orderService;

    @Test
    @DisplayName("주문 생성 성공 - 유저 + 상품 리스트 + 수량으로 주문 생성")
    void createOrder_success() {
        // given
        Long userId = 1L;
        Long productId1 = 10L;
        Long productId2 = 20L;

        // 요청 DTO 준비
        OrderProductRequest item1 = new OrderProductRequest();
        setField(item1, productId1, 2);

        OrderProductRequest item2 = new OrderProductRequest();
        setField(item2, productId2, 3);

        CreateOrderRequest req = new CreateOrderRequest();
        setCreateOrderReq(req, userId, List.of(item1, item2), "서울시 강남구 어딘가");

        // 유저, 상품, 매퍼 mock 세팅
        User user = User.builder()
                .id(userId)
                .username("tester")
                .build();

        Product product1 = Product.builder()
                .id(productId1)
                .name("상품1")
                .price(new BigDecimal("1000"))
                .stock(100)
                .build();

        Product product2 = Product.builder()
                .id(productId2)
                .name("상품2")
                .price(new BigDecimal("2000"))
                .stock(100)
                .build();

        // findById, findAllById 동작 설정
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(productRepository.findAllById(List.of(productId1, productId2)))
                .willReturn(List.of(product1, product2));

        // orderRepository.save() 는 보통 저장 후 같은 객체 리턴
        given(orderRepository.save(Mockito.<Order>any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // mapper.toResponse() mock
        OrderResponse mockResponse = OrderResponse.builder()
                .id(1L)
                .userName("tester")
                .shippingAddress("서울시 강남구 어딘가")
                .totalPrice(new BigDecimal("8000")) // 1000*2 + 2000*3 (예시)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.PENDING)
                .build();

        given(orderMapper.toResponse(Mockito.<Order>any())).willReturn(mockResponse);

        // when
        OrderResponse result = orderService.create(req);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserName()).isEqualTo("tester");
        assertThat(result.getShippingAddress()).isEqualTo("서울시 강남구 어딘가");
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PENDING);

        verify(userRepository).findById(userId);
        verify(productRepository).findAllById(List.of(productId1, productId2));
        verify(orderRepository).save(Mockito.<Order>any());
        verify(orderMapper).toResponse(Mockito.<Order>any());
    }

    private void setField(OrderProductRequest req, Long productId, Integer quantity) {
        // 여기서 직접 setter 쓸 수 있으면 그걸로 교체하면 됨
        // 예: req.setProductId(productId); req.setQuantity(quantity);
        try {
            var f1 = OrderProductRequest.class.getDeclaredField("productId");
            f1.setAccessible(true);
            f1.set(req, productId);

            var f2 = OrderProductRequest.class.getDeclaredField("quantity");
            f2.setAccessible(true);
            f2.set(req, quantity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setCreateOrderReq(CreateOrderRequest req, Long userId, List<OrderProductRequest> products, String addr) {
        try {
            var uf = CreateOrderRequest.class.getDeclaredField("userId");
            uf.setAccessible(true);
            uf.set(req, userId);

            var pf = CreateOrderRequest.class.getDeclaredField("products");
            pf.setAccessible(true);
            pf.set(req, products);

            var af = CreateOrderRequest.class.getDeclaredField("shippingAddress");
            af.setAccessible(true);
            af.set(req, addr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
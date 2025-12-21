package com.jy.shoppy.domain.order.service;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.address.entity.DeliveryAddress;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.address.repository.DeliveryAddressRepository;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.entity.ProductOption;
import com.jy.shoppy.domain.prodcut.repository.ProductOptionRepository;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.order.mapper.OrderMapper;
import com.jy.shoppy.domain.order.repository.OrderQueryRepository;
import com.jy.shoppy.domain.order.repository.OrderRepository;
import com.jy.shoppy.domain.prodcut.repository.ProductRepository;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.domain.order.dto.CreateOrderRequest;
import com.jy.shoppy.domain.prodcut.dto.OrderProductRequest;
import com.jy.shoppy.domain.order.dto.OrderResponse;
import com.jy.shoppy.domain.order.dto.SearchOrderCond;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final ProductOptionRepository productOptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse create(Account account, CreateOrderRequest req) {
        User user = userRepository.findById(account.getAccountId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));

        return createOrderInternal(user, req, null);
    }

    @Transactional
    public OrderResponse createGuestOrder(CreateOrderRequest req) {
        // 비회원 비밀번호 검증
        if (!StringUtils.hasText(req.getGuestPassword())) {
            throw new ServiceException(ServiceExceptionCode.GUEST_PASSWORD_REQUIRED);
        }

        String encodedPassword = passwordEncoder.encode(req.getGuestPassword());
        return createOrderInternal(null, req, encodedPassword);
    }

    private OrderResponse createOrderInternal(User user, CreateOrderRequest req, String encodedGuestPassword) {
        // 배송지 생성
        DeliveryAddress deliveryAddress;
        if (user != null) {
            // 회원
            deliveryAddress = DeliveryAddress.createDeliveryAddress(user, req);
        } else {
            // 비회원
            deliveryAddress = DeliveryAddress.createGuestDeliveryAddress(req);
        }
        
        deliveryAddressRepository.save(deliveryAddress);

        List<OrderProduct> orderProducts = new ArrayList<>();
        for (OrderProductRequest item : req.getProducts()) {
            // 1. 상품 조회
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT));

            // 2. ProductOption 조회
            ProductOption productOption = findProductOption(
                    item.getProductId(),
                    item.getColor(),
                    item.getSize()
            );

            // 3. 재고 차감 (서비스 레이어)
            decreaseStock(productOption, item.getQuantity());

            // 4. 주문 가격 계산 (기본가 + 옵션 추가금)
            BigDecimal orderPrice = productOption.getTotalPrice();

            OrderProduct orderProduct = OrderProduct.createOrderProduct(
                    product,
                    item.getColor(),
                    item.getSize(),
                    orderPrice,
                    item.getQuantity()
            );
            orderProducts.add(orderProduct);
        }
        // 주문 생성
        Order order = Order.createOrder(user, deliveryAddress, orderProducts, encodedGuestPassword);
        orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    public OrderResponse findMyOrderById(Account account, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, account.getAccountId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_ORDER));
        return orderMapper.toResponse(order);
    }

    public List<OrderResponse> findMyOrders(Account account) {
        List<Order> orders = orderRepository.findByUserId(account.getAccountId());
        return orderMapper.toResponseList(orders);
    }

    @Transactional
    public OrderResponse cancelMyOrder(Account account, Long orderId) {
        // 주문 조회
        Order order = orderRepository.findByIdAndUserId(orderId, account.getAccountId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_ORDER));

        // 재고 복구
        order.getOrderProducts().forEach(this::restoreStock);

        // 주문 취소
        order.cancel();
        return orderMapper.toResponse(order);
    }

    public Page<OrderResponse> searchOrdersPage(Account account, SearchOrderCond cond, Pageable pageable) {
        SearchOrderCond userCond = new SearchOrderCond(
                account.getAccountId(),
                cond.orderStatus(),
                cond.startDate(),
                cond.endDate()
        );

        Page<Order> page = orderQueryRepository.searchOrdersPage(userCond, pageable);
        return page.map(orderMapper::toResponse);
    }

    private ProductOption findProductOption(Long productId, String color, String size) {
        return productOptionRepository
                .findByProductIdAndColorAndSize(productId, color, size)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.INVALID_PRODUCT_OPTION));
    }

    private void decreaseStock(ProductOption productOption, int quantity) {
        productOption.decreaseStock(quantity);
    }

    private void restoreStock(OrderProduct orderProduct) {
        ProductOption productOption = findProductOption(
                orderProduct.getProduct().getId(),
                orderProduct.getSelectedColor(),
                orderProduct.getSelectedSize()
        );
        productOption.increaseStock(orderProduct.getQuantity());
    }
}

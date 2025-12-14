package com.jy.shoppy.domain.order.service;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.order.entity.DeliveryAddress;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.order.repository.DeliveryAddressRepository;
import com.jy.shoppy.domain.prodcut.entity.Product;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse create(Account account, CreateOrderRequest req) {
        Long userId = account.getAccountId() == null ? null : account.getAccountId();
        User user = null;

        // 회원 -> 사용자 조회
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));
        }

        // 비회원 - guestPassword null chk
        if (user == null && !StringUtils.hasText(req.getGuestPassword())) {
            throw new ServiceException(ServiceExceptionCode.GUEST_PASSWORD_REQUIRED);
        }

        // 비회원 비밀번호 인코딩
        String encodedGuestPassword = null;
        if (userId == null && StringUtils.hasText(req.getGuestPassword())) {
            encodedGuestPassword = passwordEncoder.encode(req.getGuestPassword());
        }


        // 배송지 생성
        DeliveryAddress deliveryAddress = DeliveryAddress.createDeliveryAddress(user, req);
        deliveryAddressRepository.save(deliveryAddress);

        // 상품 조회
        List<Long> productIds = req.getProducts().stream()
                .map(OrderProductRequest::getProductId)
                .toList();
        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != req.getProducts().size()) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT);
        }

        // (productId, 수량) 맵핑
        Map<Long, Integer> quantityMap = req.getProducts().stream()
                .collect(Collectors.toMap(
                        OrderProductRequest::getProductId,
                        OrderProductRequest::getQuantity
                ));

        // 주문상품 생성
        List<OrderProduct> orderProducts = new ArrayList<>();
        for (Product product : products) {
            OrderProduct orderProduct =
                    OrderProduct.createOrderProduct(product, product.getPrice(), quantityMap.get(product.getId()));
            orderProducts.add(orderProduct);
        }

        // 주문 생성
        Order order = Order.createOrder(user, deliveryAddress, orderProducts, encodedGuestPassword);

        // 주문 저장 > (Product 재고/상태, Order, OrderProduct 전부 dirty checking 반영) ??
        orderRepository.save(order);
        return orderMapper.toResponse(order);
    }

    public OrderResponse findById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_ORDER));
        return orderMapper.toResponse(order);
    }

    public List<OrderResponse> findAll() {
        List<Order> orders = orderRepository.findAll();
        return orderMapper.toResponseList(orders);
    }

    @Transactional
    public OrderResponse cancel(Long orderId) {
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_ORDER));
        // 주문 취소
        order.cancel();
        return orderMapper.toResponse(order);
    }

    public Page<OrderResponse> searchOrdersPage(SearchOrderCond cond, Pageable pageable) {
        Page<Order> page = orderQueryRepository.searchOrdersPage(cond, pageable);
        return page.map(orderMapper::toResponse);
    }
}

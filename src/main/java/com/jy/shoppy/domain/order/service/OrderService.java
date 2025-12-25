package com.jy.shoppy.domain.order.service;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.address.entity.DeliveryAddress;
import com.jy.shoppy.domain.guest.entity.Guest;
import com.jy.shoppy.domain.guest.repository.GuestRepository;
import com.jy.shoppy.domain.order.dto.*;
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
import com.jy.shoppy.domain.prodcut.dto.OrderProductRequest;
import com.jy.shoppy.domain.user.service.UserGradeService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private final GuestRepository guestRepository;

    @Transactional
    public OrderResponse create(Account account, CreateMemberOrderRequest req) {
        User user = userRepository.findById(account.getAccountId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));

        // 배송지 생성
        DeliveryAddress deliveryAddress;
        if (req.getDeliveryAddressId() != null) {
            // 기존 배송지 사용
            deliveryAddress = deliveryAddressRepository
                    .findByIdAndUserId(req.getDeliveryAddressId(), user.getId())
                    .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_DELIVERY_ADDRESS));
        } else {
            // 새 배송지 생성
            deliveryAddress = DeliveryAddress.createDeliveryAddress(user, req);
            deliveryAddressRepository.save(deliveryAddress);
        }

        return createOrderInternal(user, null, req.getProducts(), deliveryAddress);
    }

    @Transactional
    public OrderResponse createGuestOrder(CreateGuestOrderRequest req) {
        // 비회원 비밀번호 검증
        if (!StringUtils.hasText(req.getGuestPassword())) {
            throw new ServiceException(ServiceExceptionCode.GUEST_PASSWORD_REQUIRED);
        }
        String encodedPassword = passwordEncoder.encode(req.getGuestPassword());

        // 비회원 생성
        Guest guest = Guest.createGuest(req, encodedPassword);
        guestRepository.save(guest);

        return createOrderInternal(null, guest, req.getProducts(), null);
    }

    private OrderResponse createOrderInternal(User user, Guest guest, List<OrderProductRequest> products, DeliveryAddress deliveryAddress) {
        List<OrderProduct> orderProducts = new ArrayList<>();
        for (OrderProductRequest item : products) {
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

        // 주문번호 생성
        String orderNumber = generateOrderNumber();

        // 주문 생성
        Order order = Order.createOrder(
                user,
                guest,
                deliveryAddress,     // 배송지 (비회원이면 null)
                orderProducts,
                orderNumber
        );
        orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD" + datePart + randomPart;
        // 예: ORD202412267F3B2A1C
    }

    public OrderResponse findMyOrderById(Account account, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, account.getAccountId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_ORDER));
        return orderMapper.toResponse(order);
    }

    public OrderResponse findGuestOrder(GuestOrderRequest req) {
        // 1. 주문번호로 주문 조회
        Order order = orderRepository.findGuestOrderByOrderNumber(req.getOrderNumber())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_ORDER));
        validateGuestOrder(order, req.getName(), req.getPassword());
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

    @Transactional
    public OrderResponse cancelGuestOrder(GuestOrderCancelRequest req) {
        // 1. 주문 조회 및 검증
        Order order = orderRepository.findGuestOrderByOrderNumber(req.getOrderNumber())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_ORDER));
        validateGuestOrder(order, req.getName(), req.getPassword());

        // 2. 재고 복구
        order.getOrderProducts().forEach(this::restoreStock);

        // 3. 주문 취소
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

    private void validateGuestOrder(Order order, String name, String password) {
        // 비회원 주문인지 확인
        if (order.getGuest() == null) {
            throw new ServiceException(ServiceExceptionCode.NOT_GUEST_ORDER);
        }

        // 주문자명 확인
        if (!order.getGuest().getName().equals(name)) {
            throw new ServiceException(ServiceExceptionCode.GUEST_ORDER_NOT_MATCH);
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, order.getGuest().getPasswordHash())) {
            throw new ServiceException(ServiceExceptionCode.INVALID_GUEST_PASSWORD);
        }
    }
}

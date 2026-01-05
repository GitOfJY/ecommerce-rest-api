package com.jy.shoppy.domain.order.service;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.address.entity.DeliveryAddress;
import com.jy.shoppy.domain.coupon.dto.CouponDiscountResponse;
import com.jy.shoppy.domain.coupon.service.CouponService;
import com.jy.shoppy.domain.guest.entity.Guest;
import com.jy.shoppy.domain.guest.repository.GuestRepository;
import com.jy.shoppy.domain.order.dto.*;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.address.repository.DeliveryAddressRepository;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import com.jy.shoppy.domain.point.service.PointService;
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
    private final CouponService couponService;
    private final PointService pointService;
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

        return createOrderInternal(account, user, null, req.getProducts(), deliveryAddress, req.getCouponUserId(), req.getUsePoints());
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

        return createOrderInternal(null, null, guest, req.getProducts(), null, null, 0);
    }

    private OrderResponse createOrderInternal(Account account, User user, Guest guest, List<OrderProductRequest> products, DeliveryAddress deliveryAddress, Long couponUserId, Integer usePoints) {
        List<OrderProduct> orderProducts = new ArrayList<>();

        // 1. 회원 등급 할인율 조회
        BigDecimal discountRate = BigDecimal.ZERO;
        if (user != null && user.getUserGrade() != null) {
            discountRate = user.getUserGrade().getDiscountRate();
        }

        // 2. 주문 상품 생성 및 금액 계산
        BigDecimal totalAmount = BigDecimal.ZERO;

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
            BigDecimal basePrice = productOption.getTotalPrice();

            // 5. 등급 할인 적용
            BigDecimal discountedPrice = applyDiscount(basePrice, discountRate);

            OrderProduct orderProduct = OrderProduct.createOrderProduct(
                    product,
                    item.getColor(),
                    item.getSize(),
                    discountedPrice,
                    item.getQuantity()
            );
            orderProducts.add(orderProduct);

            // 총 금액 계산
            BigDecimal itemTotal = discountedPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        // 3. 쿠폰 할인 계산
        BigDecimal couponDiscount = BigDecimal.ZERO;
        if (couponUserId != null && account != null) {
            // 쿠폰 할인 금액 계산 (CouponService 호출)
            couponDiscount = calculateCouponDiscount(account, couponUserId, products);
        }

        // 4. 적립금 사용 검증 및 차감
        int pointsUsed = 0;
        if (usePoints != null && usePoints > 0 && user != null) {
            pointsUsed = usePoints;

            // 4-1. 보유 적립금 확인
            if (!user.hasEnoughPoints(pointsUsed)) {
                throw new ServiceException(ServiceExceptionCode.INSUFFICIENT_POINTS);
            }

            // 4-2. 최소 사용 금액 체크 (예: 1,000원 이상)
            if (pointsUsed < 1000) {
                throw new ServiceException(ServiceExceptionCode.POINTS_MINIMUM_USE);
            }

            // 4-3. 최대 사용 금액 체크 (총 금액의 50% 이하)
            BigDecimal maxPoints = totalAmount.subtract(couponDiscount)
                    .multiply(new BigDecimal("0.5"))
                    .setScale(0, BigDecimal.ROUND_DOWN);

            if (pointsUsed > maxPoints.intValue()) {
                throw new ServiceException(ServiceExceptionCode.POINTS_EXCEED_LIMIT);
            }
        }

        // 5. 최종 금액 = 총 금액 - 쿠폰 할인 - 적립금 사용
        BigDecimal finalAmount = totalAmount
                .subtract(couponDiscount)
                .subtract(BigDecimal.valueOf(pointsUsed));

        // 6. 주문번호 생성
        String orderNumber = generateOrderNumber();

        // 7. 주문 생성
        Order order = Order.createOrder(
                user,
                guest,
                deliveryAddress,
                orderProducts,
                orderNumber,
                couponUserId,
                couponDiscount,
                pointsUsed
        );

        orderRepository.save(order);

        // 8. 쿠폰 사용 처리
        if (couponUserId != null && account != null) {
            couponService.useCoupon(couponUserId, order.getId(), account);
            log.info("쿠폰 사용 완료 - 주문ID: {}, 쿠폰ID: {}, 할인금액: {}",
                    order.getId(), couponUserId, couponDiscount);
        }

        // 9. 적립금 사용 처리
        if (pointsUsed > 0 && user != null) {
            pointService.usePoints(user, order, pointsUsed);
            log.info("적립금 사용 완료 - 주문ID: {}, 사용자ID: {}, 사용금액: {}원",
                    order.getId(), user.getId(), pointsUsed);
        }

        return orderMapper.toResponse(order);
    }

    /**
     * 주문 완료 처리 (결제 완료 후)
     */
    @Transactional
    public OrderResponse completeOrder(Account account, Long orderId) {
        // 1. 주문 조회
        Order order = orderRepository.findByIdAndUserId(orderId, account.getAccountId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_ORDER));

        // 2. 이미 완료된 주문인지 확인
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new ServiceException(ServiceExceptionCode.ALREADY_COMPLETED_ORDER);
        }

        // 3. 주문 상태를 COMPLETED로 변경
        order.complete();

        // 4. 적립금 지급
        if (order.getUser() != null) {
            User user = order.getUser();
            pointService.earnPoints(user, order);
            log.info("적립금 지급 완료 - 주문ID: {}, 사용자ID: {}", order.getId(), user.getId());
        }

        return orderMapper.toResponse(order);
    }

    /**
     * 쿠폰 할인 금액 계산
     */
    private BigDecimal calculateCouponDiscount(
            Account account,
            Long couponUserId,
            List<OrderProductRequest> products
    ) {
        try {
            // OrderProductsRequest 생성
            OrderProductsRequest request = new OrderProductsRequest(products);

            // 쿠폰 할인 계산
            CouponDiscountResponse response = couponService.calculateCouponDiscount(
                    couponUserId,
                    request,
                    account
            );

            // 쿠폰 적용 가능 여부 확인
            if (!response.getIsApplicable()) {
                throw new ServiceException(ServiceExceptionCode.COUPON_NOT_APPLICABLE);
            }
            return response.getDiscountAmount();

        } catch (ServiceException e) {
            log.error("쿠폰 할인 계산 실패 - 쿠폰ID: {}, 사유: {}", couponUserId, e.getMessage());
            throw e;
        }
    }

    // 등급 할인율 적용
    private BigDecimal applyDiscount(BigDecimal price, BigDecimal discountRate) {
        if (discountRate == null || discountRate.compareTo(BigDecimal.ZERO) == 0) {
            return price;
        }

        // 할인 금액 = 원가 * 할인율
        BigDecimal discountAmount = price.multiply(discountRate);

        // 할인된 가격 = 원가 - 할인 금액
        BigDecimal discountedPrice = price.subtract(discountAmount);

        // 소수점 이하 반올림
        return discountedPrice.setScale(0, BigDecimal.ROUND_HALF_UP);
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
        // 1. 주문 조회
        Order order = orderRepository.findByIdAndUserId(orderId, account.getAccountId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_ORDER));

        // 2. 재고 복구
        order.getOrderProducts().forEach(this::restoreStock);

        // 3. 쿠폰 복구
        if (order.getCouponUserId() != null) {
            couponService.restoreCoupon(order.getCouponUserId());
            log.info("쿠폰 복구 완료 - 주문ID: {}", order.getId());
        }

        // 4. 적립금 복구
        if (order.getUser() != null) {
            User user = order.getUser();

            // 4-1. 사용한 적립금 반환
            if (order.getPointsUsed() != null && order.getPointsUsed() > 0) {
                pointService.restoreUsedPoints(user, order, order.getPointsUsed());
                log.info("사용 적립금 반환 완료 - 주문ID: {}, 반환금액: {}원",
                        order.getId(), order.getPointsUsed());
            }

            // 4-2. 적립받은 적립금 회수 (주문 완료된 경우만)
            if (order.getStatus() == OrderStatus.COMPLETED) {
                pointService.cancelEarnedPoints(user, order);
                log.info("적립금 회수 완료 - 주문ID: {}", order.getId());
            }
        }

        // 5. 주문 취소
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

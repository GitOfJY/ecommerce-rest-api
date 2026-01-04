package com.jy.shoppy.domain.order.entity;

import com.jy.shoppy.domain.address.entity.DeliveryAddress;
import com.jy.shoppy.domain.guest.entity.Guest;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // 테이블명 복수형 권장
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id")
    private Guest guest;

    @Column(unique = true, nullable = false, length = 50)
    private String orderNumber;

    @Builder.Default
    @BatchSize(size = 20)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderProduct>  orderProducts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id")
    private DeliveryAddress deliveryAddress;

    private BigDecimal totalPrice;

    @Column(name = "coupon_user_id")
    private Long couponUserId;

    @Column(name = "coupon_discount", precision = 13, scale = 2)
    private BigDecimal couponDiscount;

    public void addOrderProduct(OrderProduct orderProduct) {
        orderProducts.add(orderProduct);
        orderProduct.assignOrder(this);
    }

    // 주문 생성
    public static Order createOrder(User user,
                                    Guest guest,
                                    DeliveryAddress deliveryAddress,
                                    List<OrderProduct> orderProducts,
                                    String orderNumber,
                                    Long couponUserId,
                                    BigDecimal couponDiscount
                                    ) {
        if (user == null && guest == null) {
            throw new IllegalArgumentException("주문자 정보가 필요합니다");
        }
        if (user != null && guest != null) {
            throw new IllegalArgumentException("회원과 비회원을 동시에 지정할 수 없습니다");
        }
        if (user != null && deliveryAddress == null) {
            throw new IllegalArgumentException("회원 주문은 배송지가 필요합니다");
        }

        Order order = Order.builder()
                .user(user)
                .guest(guest)
                .orderNumber(orderNumber)
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .deliveryAddress(deliveryAddress)
                .couponUserId(couponUserId)
                .couponDiscount(couponDiscount != null ? couponDiscount : BigDecimal.ZERO)
                .build();

        for (OrderProduct orderProduct : orderProducts) {
            order.addOrderProduct(orderProduct);
        }

        order.calculateAndSetTotalPrice();
        return order;
    }

    public void calculateAndSetTotalPrice() {
        this.totalPrice = getTotalPrice();
    }

    // 전체 주문 가격 조회
    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderProduct orderProduct : orderProducts) {
            totalPrice = totalPrice.add(orderProduct.getTotalPrice());
        }
        return totalPrice;
    }

    /**
     * 최종 결제 금액 (쿠폰 할인 적용 후)
     */
    public BigDecimal getFinalPrice() {
        BigDecimal total = getTotalPrice();
        BigDecimal discount = couponDiscount != null ? couponDiscount : BigDecimal.ZERO;
        return total.subtract(discount);
    }

    // 주문 취소
    public void cancel() {
        if (OrderStatus.COMPLETED.equals(status)) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_CANCEL_ORDER_COMPLETED);
        }

        if (OrderStatus.CANCELED.equals(status)) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_CANCEL_ORDER_CANCELED);
        }

        // TODO : "이미 배송완료된 상품은 취소가 불가능합니다."

        this.status = OrderStatus.CANCELED;

        // 재고 복구 Service에서 처리
    }

    public void complete() {
        if (OrderStatus.COMPLETED.equals(status)) {
            throw new ServiceException(ServiceExceptionCode.ALREADY_COMPLETED_ORDER);
        }

        if (OrderStatus.CANCELED.equals(status)) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_CANCEL_ORDER_CANCELED);
        }
        this.status = OrderStatus.COMPLETED;
    }

    public String getZipcode() {
        if (isGuestOrder()) {
            return guest.getZipcode();
        }
        return deliveryAddress != null ? deliveryAddress.getAddress().getZipCode() : null;
    }

    public String getCity() {
        if (isGuestOrder()) {
            return guest.getCity();
        }
        return deliveryAddress != null ? deliveryAddress.getAddress().getCity() : null;
    }

    public String getStreet() {
        if (isGuestOrder()) {
            return guest.getStreet();
        }
        return deliveryAddress != null ? deliveryAddress.getAddress().getStreet() : null;
    }

    public String getDetail() {
        if (isGuestOrder()) {
            return guest.getDetail();
        }
        return deliveryAddress != null ? deliveryAddress.getAddress().getDetail() : null;
    }

    public String getRecipientName() {
        if (isGuestOrder()) {
            return guest.getName();
        }
        return deliveryAddress != null ? deliveryAddress.getRecipientName() : null;
    }

    public String getReceiverPhone() {
        if (isGuestOrder()) {
            return guest.getPhone();
        }
        return deliveryAddress != null ? deliveryAddress.getRecipientPhone() : null;
    }

    public String getUserName() {
        if (isGuestOrder()) {
            return guest.getName();
        }
        return user != null ? user.getUsername() : null;
    }

    public String getRecipientEmail() {
        if (isGuestOrder()) {
            return guest.getEmail();
        }
        return deliveryAddress != null ? deliveryAddress.getRecipientEmail() : null;
    }

    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (getZipcode() != null) {
            address.append("[").append(getZipcode()).append("] ");
        }
        if (getCity() != null) {
            address.append(getCity()).append(" ");
        }
        if (getStreet() != null) {
            address.append(getStreet());
        }
        if (getDetail() != null && !getDetail().isBlank()) {
            address.append(" ").append(getDetail());
        }
        return address.toString();
    }

    public boolean isGuestOrder() {
        return guest != null;
    }
}

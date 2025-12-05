package com.jy.shoppy.domain.order.entity;

import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;

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

    @Builder.Default
    @BatchSize(size = 20)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderProduct>  orderProducts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    private BigDecimal totalPrice;

    public void addOrderProduct(OrderProduct orderProduct) {
        orderProducts.add(orderProduct);
        orderProduct.assignOrder(this);
    }

    // 주문 생성
    public static Order createOrder(User user, List<OrderProduct> orderProducts) {
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
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
        for (OrderProduct orderProduct : orderProducts) {
            orderProduct.cancel();
        }
    }
}

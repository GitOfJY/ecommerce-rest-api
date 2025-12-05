package com.jy.shoppy.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;

@Entity
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString
@Table(name = "order_product")
public class OrderProduct {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private BigDecimal orderPrice;

    private Integer quantity;

    public static OrderProduct createOrderProduct(Product product, BigDecimal orderPrice, int quantity) {
        // 재고차감
        product.removeStock(quantity);

        OrderProduct orderProduct = OrderProduct.builder()
                .product(product)
                .orderPrice(orderPrice)
                .quantity(quantity).build();
        return orderProduct;
    }

    public void cancel() {
        getProduct().addStock(quantity);
    }

    public BigDecimal getTotalPrice() {
        return orderPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Order 설정
    void assignOrder(Order order) {
        this.order = order;
    }
}

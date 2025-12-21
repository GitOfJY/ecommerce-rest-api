package com.jy.shoppy.domain.prodcut.entity;

import com.jy.shoppy.domain.prodcut.entity.type.StockStatus;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product_options")
public class ProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(length = 50)
    private String color;

    @Column(length = 20)
    private String size;

    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal additionalPrice;

    @Column(nullable = false)
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockStatus stockStatus;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static ProductOption createOption(Product product, String color, String size, int stock, BigDecimal additionalPrice) {
        StockStatus status;
        if (stock == 0) {
            status = StockStatus.OUT_OF_STOCK;
        } else if (stock <= 5) {
            status = StockStatus.LOW_STOCK;
        } else {
            status = StockStatus.IN_STOCK;
        }

        ProductOption option = ProductOption.builder()
                .product(product)
                .color(color)
                .size(size)
                .stock(stock)
                .stockStatus(status)
                .additionalPrice(additionalPrice == null ? BigDecimal.ZERO : additionalPrice)
                .build();
        return option;
    }

    // 재고 관리 메서드들
    public void increaseStock(int quantity) {
        this.stock += quantity;
        updateStockStatus();
    }

    public void decreaseStock(int quantity) {
        int restStock = this.stock - quantity;
        if (restStock < 0) {
            throw new ServiceException(ServiceExceptionCode.INSUFFICIENT_STOCK);
        }
        this.stock = restStock;
        updateStockStatus();
    }

    // 총 가격 계산 (기본 상품 가격 + 옵션 추가 금액)
    public BigDecimal getTotalPrice() {
        return product.getPrice().add(additionalPrice);
    }

    @PrePersist
    @PreUpdate
    private void updateStockStatus() {
        if (this.stock == 0) {
            this.stockStatus = StockStatus.OUT_OF_STOCK;
        } else if (this.stock <= 5) {
            this.stockStatus = StockStatus.LOW_STOCK;
        } else {
            this.stockStatus = StockStatus.IN_STOCK;
        }
    }
}

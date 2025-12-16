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

    @Column(nullable = false, length = 50)
    private String color;

    @Column(nullable = false, length = 20)
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

    // 재고 관리 메서드들
    public void addStock(int quantity) {
        this.stock += quantity;
        updateStockStatus();
    }

    public void removeStock(int quantity) {
        int restStock = this.stock - quantity;
        if (restStock < 0) {
            throw new ServiceException(ServiceExceptionCode.INSUFFICIENT_STOCK);
        }
        this.stock = restStock;
        updateStockStatus();
    }

    private void updateStockStatus() {
        if (this.stock == 0) {
            this.stockStatus = StockStatus.OUT_OF_STOCK;
        } else if (this.stock <= 5) {
            this.stockStatus = StockStatus.LOW_STOCK;
        } else {
            this.stockStatus = StockStatus.IN_STOCK;
        }
    }

    // 총 가격 계산 (기본 상품 가격 + 옵션 추가 금액)
    public BigDecimal getTotalPrice() {
        return product.getPrice().add(additionalPrice);
    }
}

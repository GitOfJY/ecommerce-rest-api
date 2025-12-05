package com.jy.shoppy.entity;

import com.jy.shoppy.common.ServiceException;
import com.jy.shoppy.common.ServiceExceptionCode;
import com.jy.shoppy.entity.type.StockStatus;
import com.jy.shoppy.service.dto.UpdateProductRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "product",
        uniqueConstraints = @UniqueConstraint(name = "uk_product_name", columnNames = "name")
)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockStatus stockStatus;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "product")
    private List<OrderProduct> orderProducts;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryProduct> categoryProducts = new ArrayList<>();

    public List<CategoryProduct> getCategoryProducts() {
        if (categoryProducts == null) {
            categoryProducts = new ArrayList<>();
        }
        return categoryProducts;
    }

    public void updateProduct(UpdateProductRequest req) {
        this.name = req.getName();
        this.description = req.getDescription();
        this.price = req.getPrice();
        this.stock = req.getStock();
        this.updatedAt = LocalDateTime.now();
        updateStockStatus();
    }

    // 처음 저장될 때도 상태 세팅되게
    @PrePersist
    @PreUpdate
    private void syncStockStatus() {
        // 혹시 누락된 경우를 위해 JPA 레벨에서도 한 번 더 안전망
        updateStockStatus();
    }

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
}
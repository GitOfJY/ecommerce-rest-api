package com.jy.shoppy.domain.prodcut.entity;

import com.jy.shoppy.domain.category.entity.CategoryProduct;
import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.prodcut.dto.CreateProductRequest;
import com.jy.shoppy.domain.prodcut.entity.type.StockStatus;
import com.jy.shoppy.domain.prodcut.dto.UpdateProductRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
        name = "products",
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
    private BigDecimal price;  // 기본 가격 (옵션 없을 때)

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "product")
    private List<OrderProduct> orderProducts;

    @Builder.Default
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryProduct> categoryProducts = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOption> options = new ArrayList<>();

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
        this.updatedAt = LocalDateTime.now();
    }

    // 전체 재고 계산 (모든 옵션의 합)
    public Integer getTotalStock() {
        return options.stream()
                .mapToInt(ProductOption::getStock)
                .sum();
    }

    // 재고 상태 계산
    public StockStatus getStockStatus() {
        if (options.isEmpty()) {
            return StockStatus.OUT_OF_STOCK;
        }

        int totalStock = getTotalStock();
        if (totalStock == 0) {
            return StockStatus.OUT_OF_STOCK;
        } else if (totalStock <= 5) {
            return StockStatus.LOW_STOCK;
        } else {
            return StockStatus.IN_STOCK;
        }
    }

    public void addCategory(CategoryProduct categoryProduct) {
        if (this.categoryProducts == null) {
            this.categoryProducts = new ArrayList<>();
        }
        this.categoryProducts.add(categoryProduct);
    }

    public void clearCategories() {
        this.categoryProducts.clear();
    }

    public List<ProductOption> getOptions() {
        if (options == null) {
            options = new ArrayList<>();
        }
        return options;
    }

    public void addOption(ProductOption option) {
        if (this.options == null) {
            this.options = new ArrayList<>();
        }
        this.options.add(option);
    }

    public void clearOptions() {
        this.options.clear();
    }

    public boolean hasOption() {
        return options.stream()
                .anyMatch(opt -> opt.getColor() != null || opt.getSize() != null);
    }
}
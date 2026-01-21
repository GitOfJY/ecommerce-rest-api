package com.jy.shoppy.domain.prodcut.entity;

import com.jy.shoppy.domain.category.entity.CategoryProduct;
import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.prodcut.entity.type.ProductSource;
import com.jy.shoppy.domain.prodcut.entity.type.StockStatus;
import com.jy.shoppy.domain.prodcut.dto.UpdateProductRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_external_product_id", columnNames = "externalProductId")
        },
        indexes = {
                @Index(name = "idx_product_source", columnList = "source"),
                @Index(name = "idx_product_orderable", columnList = "isOrderable")
        }
)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 외부 상품 ID (외부 상품만 값이 있음, 내부 상품은 null)
    @Column(unique = true)
    private String externalProductId;

    // 상품 출처 (INTERNAL / EXTERNAL)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProductSource source = ProductSource.INTERNAL;

    // 마지막 외부 동기화 시점
    private LocalDateTime lastSyncedAt;

    // 주문 가능 여부 (외부 상품: stock > 0 이면 true)
    @Column(nullable = false)
    @Builder.Default
    private Boolean isOrderable = true;

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

    // 평균 평점 캐시 (리뷰 작성/수정/삭제 시 업데이트)
    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating;

    // 총 리뷰 개수 캐시
    @Column
    private Integer reviewCount;

    @Builder.Default
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryProduct> categoryProducts = new ArrayList<>();

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductOption> options = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

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

    public void updateAverageRating(BigDecimal averageRating, int reviewCount) {
        this.averageRating = averageRating != null
                ? averageRating.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        this.reviewCount = reviewCount;
    }

    public void addImage(ProductImage image) {
        this.images.add(image);
    }

    public void clearImages() {
        this.images.clear();
    }

    public String getThumbnailUrl() {
        return this.images.stream()
                .filter(ProductImage::getIsThumbnail)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }

    /**
     * 외부 상품 데이터로 업데이트
     */
    public void updateFromExternal(String name, String description,
                                   BigDecimal price) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.lastSyncedAt = LocalDateTime.now();
    }

    /**
     * 외부 상품 여부 확인
     */
    public boolean isExternal() {
        return this.source == ProductSource.EXTERNAL;
    }

    /**
     * 동기화 시간 갱신 (변경 없이 동기화만 확인된 경우)
     */
    public void markSynced() {
        this.lastSyncedAt = LocalDateTime.now();
    }

    /**
     * 주문 불가 처리 (외부에서 삭제된 상품)
     */
    public void markAsNotOrderable() {
        this.isOrderable = false;
        this.lastSyncedAt = LocalDateTime.now();
    }

    /**
     * 주문 가능 상태 변경
     */
    public void updateOrderable(boolean isOrderable) {
        this.isOrderable = isOrderable;
    }

    public void updateFromExternal(String name, String description,
                                   BigDecimal price, boolean isOrderable) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.isOrderable = isOrderable;
        this.lastSyncedAt = LocalDateTime.now();
    }
}
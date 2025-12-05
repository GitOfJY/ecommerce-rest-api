package com.jy.shoppy.entity;

import com.jy.shoppy.service.CategoryService;
import com.jy.shoppy.service.dto.UpdateCategoryRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent; // 부모 카테고리

    @OneToMany(mappedBy = "parent")
    @BatchSize(size = 500)
    private List<Category> children = new ArrayList<>(); // 자식 카테고리 목록
    // 참고
    // https://hstory0208.tistory.com/entry/%EB%8B%A4%EB%8B%A8%EA%B3%84-%EA%B3%84%EC%B8%B5%ED%98%95-%EA%B5%AC%EC%A1%B0%EB%A5%BC-%EA%B0%80%EC%A7%84-%EC%97%94%ED%8B%B0%ED%8B%B0-%EA%B5%AC%ED%98%84%EA%B3%BC-%EC%84%B1%EB%8A%A5-%EC%B5%9C%EC%A0%81%ED%99%94-%EA%B3%BC%EC%A0%95-%EB%8F%8C%EC%95%84%EB%B3%B4%EA%B8%B0

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryProduct> categoryProducts = new ArrayList<>();

    public void addParentCategory(Category category) {
        this.parent = category;
    }

    public void addChildrenCategories(List<Category> categories) {
        this.children.addAll(categories);
    }

    public void updateCategory(UpdateCategoryRequest req, Category parentCategory) {
        this.name = req.getName();
        this.description = req.getDescription();
        this.parent = parentCategory;
    }
}

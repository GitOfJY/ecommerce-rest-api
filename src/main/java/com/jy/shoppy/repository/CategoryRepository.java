package com.jy.shoppy.repository;

import com.jy.shoppy.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    boolean existsByParentId(Long parentId);

    @Query("""
        select distinct c
        from Category c
        left join fetch c.categoryProducts cp
        left join fetch cp.product p
        """)
    List<Category> findAllWithProducts();

    /**
     * 전체 트리의 루트들(부모가 없는 카테고리들)부터 시작
     * children 1단계는 fetch join, 그 아래 레벨은 LAZY + BatchSize로 따라옴
     */
    @Query("""
        select distinct c
        from Category c
        left join fetch c.children ch
        where c.parent is null
    """)
    List<Category> findRootCategoriesWithChildren();

    // 카테고리 별 최다 판매 순위 Top 10 조회
    // LEFT JOIN + GROUP BY로 카테고리별 통계 쿼리 작성

}

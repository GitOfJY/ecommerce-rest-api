package com.jy.shoppy.domain.category.service;

import com.jy.shoppy.domain.category.entity.Category;
import com.jy.shoppy.domain.category.mapper.CategoryMapper;
import com.jy.shoppy.domain.category.repository.CategoryRepository;
import com.jy.shoppy.domain.category.dto.CategoryResponse;
import com.jy.shoppy.domain.category.dto.CategoryTreeResponse;
import com.jy.shoppy.domain.category.dto.CreateCategoryRequest;
import com.jy.shoppy.domain.category.dto.UpdateCategoryRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public Long create(CreateCategoryRequest req) {
        // 카테고리 이름 중복 방지
        if (categoryRepository.existsByName(req.getName())) {
            throw new ServiceException(ServiceExceptionCode.DUPLICATED_CATEGORY_NAME);
        }

        // 카테고리 부모 id 확인
        if (req.getParentId() != null && !categoryRepository.existsById(req.getParentId())) {
            throw new ServiceException(ServiceExceptionCode.NOT_FOUND_PARENT_CATEGORY);
        }

        // name, description, parentId
        Category category = categoryMapper.toEntity(req);

        // addParentCategory
        if (req.getParentId() != null) {
            Category parentCategory = categoryRepository.findById(req.getParentId())
                    .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_PARENT_CATEGORY));
            category.addParentCategory(parentCategory);
        }

        // addChildrenCategories
        if (req.getChildrenIds() != null && !req.getChildrenIds().isEmpty()) {
            List<Category> childrenCategories = categoryRepository.findAllById(req.getChildrenIds());
            category.addChildrenCategories(childrenCategories);

        }
        return categoryRepository.save(category).getId();
    }

    @Transactional
    public Long update(UpdateCategoryRequest req) {
        // 카테고리 확인
        Category category = categoryRepository.findById(req.getId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_CATEGORY));

        // 부모 id가 있다면 카테고리 부모 id 확인
        if (req.getParentId() != null && !categoryRepository.existsById(req.getParentId())) {
            throw new ServiceException(ServiceExceptionCode.NOT_FOUND_PARENT_CATEGORY);
        }

        if (req.getParentId() != null) {
            Category parentCategory = categoryRepository.findById(req.getParentId())
                    .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_CATEGORY));;
            category.updateCategory(req, parentCategory);
        } else {
            category.updateCategory(req, null);
        }
        return category.getId();
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryMapper.toResponseList(categoryRepository.findAllWithProducts());
    }

    public void deleteById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_CATEGORY));

        // 1) 하위 카테고리 존재 여부 체크
        if (!category.getChildren().isEmpty()) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_DELETE_HAS_CHILD);
        }

        // 2) 카테고리에 속한 상품 존재 여부 체크
         if (!category.getCategoryProducts().isEmpty()) {
             throw new ServiceException(ServiceExceptionCode.CANNOT_DELETE_HAS_PRODUCTS);
         }

        categoryRepository.deleteById(id);
    }

    // 전체 카테고리 트리 조회 (루트들 기준)
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> roots = categoryRepository.findRootCategoriesWithChildren();

        return roots.stream()
                .map(this::buildTree)   // 재귀적으로 children까지 DTO로 변환
                .toList();
    }

    private CategoryTreeResponse buildTree(Category category) {
        // 자식들 먼저 DTO로 변환
        List<CategoryTreeResponse> childDtos = category.getChildren() == null
                ? List.of()
                : category.getChildren().stream()
                .map(this::buildTree)
                .toList();

        return CategoryTreeResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .children(childDtos)
                .build();
    }

    // 카테고리 별 최다 판매 순위 Top 10 조회

}

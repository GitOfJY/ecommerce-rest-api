package com.jy.shoppy.service;

import com.jy.shoppy.domain.category.service.CategoryService;
import com.jy.shoppy.domain.category.dto.CreateCategoryRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@Transactional
@Rollback(value = true)
class CategoryServiceTest {
    @Autowired
    private CategoryService categoryService;

    @Test
    void 부모_카테고리_생성() {
        // given
        CreateCategoryRequest req = CreateCategoryRequest.builder()
                .name("최상위 카테고리")
                .description("테스트입니다.")
                .parentId(null)
                .build();

        // when
        Long id = categoryService.create(req);

        // then
        assertNotNull(id);
        log.debug("생성된 카테고리 ID = {}", id);
    }

}
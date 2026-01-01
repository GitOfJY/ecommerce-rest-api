package com.jy.shoppy.domain.review.repository;

import com.jy.shoppy.domain.review.entity.ReviewComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

    /**
     * 특정 리뷰의 모든 댓글 조회
     */
    List<ReviewComment> findByReviewIdOrderByCreatedAtAsc(Long reviewId);

    /**
     * 특정 사용자의 모든 댓글 조회
     */
    List<ReviewComment> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<ReviewComment> findByReviewId(Long reviewId, Pageable pageable);

    Page<ReviewComment> findByUserId(Long userId, Pageable pageable);
}

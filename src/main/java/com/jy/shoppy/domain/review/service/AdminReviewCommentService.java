package com.jy.shoppy.domain.review.service;

import com.jy.shoppy.domain.review.dto.CommentResponse;
import com.jy.shoppy.domain.review.entity.ReviewComment;
import com.jy.shoppy.domain.review.mapper.ReviewCommentMapper;
import com.jy.shoppy.domain.review.repository.ReviewCommentRepository;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReviewCommentService {
    private final ReviewCommentRepository commentRepository;
    private final ReviewCommentMapper commentMapper;

    /**
     * 전체 댓글 조회
     */
    @Transactional(readOnly = true)
    public Page<CommentResponse> getAllComments(Pageable pageable) {
        Page<ReviewComment> comments = commentRepository.findAll(pageable);
        return comments.map(commentMapper::toResponse);
    }

    /**
     * 댓글 상세 조회
     */
    @Transactional(readOnly = true)
    public CommentResponse getComment(Long commentId) {
        ReviewComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COMMENT));

        return commentMapper.toResponse(comment);
    }

    /**
     * ✅ 댓글 삭제 (관리자 - 권한 검증 없음)
     */
    @Transactional
    public void deleteComment(Long commentId) {
        // 1. 댓글 조회
        ReviewComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COMMENT));

        // 2. 권한 검증 없음 (관리자는 모든 댓글 삭제 가능)

        // 3. 댓글 삭제
        Long userId = comment.getUser().getId();
        commentRepository.delete(comment);

        log.info("[Admin] Comment deleted: commentId={}, userId={}", commentId, userId);
    }

    /**
     * 특정 리뷰의 댓글 조회
     */
    @Transactional(readOnly = true)
    public Page<CommentResponse> getReviewComments(Long reviewId, Pageable pageable) {
        Page<ReviewComment> comments = commentRepository.findByReviewId(reviewId, pageable);
        return comments.map(commentMapper::toResponse);
    }

    /**
     * 특정 사용자의 댓글 조회
     */
    @Transactional(readOnly = true)
    public Page<CommentResponse> getUserComments(Long userId, Pageable pageable) {
        Page<ReviewComment> comments = commentRepository.findByUserId(userId, pageable);
        return comments.map(commentMapper::toResponse);
    }
}
package com.jy.shoppy.domain.review.service;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.review.dto.CommentResponse;
import com.jy.shoppy.domain.review.dto.CreateCommentRequest;
import com.jy.shoppy.domain.review.dto.UpdateCommentRequest;
import com.jy.shoppy.domain.review.entity.Review;
import com.jy.shoppy.domain.review.entity.ReviewComment;
import com.jy.shoppy.domain.review.mapper.ReviewCommentMapper;
import com.jy.shoppy.domain.review.repository.ReviewCommentRepository;
import com.jy.shoppy.domain.review.repository.ReviewRepository;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewCommentService {

    private final ReviewCommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewCommentMapper commentMapper;

    /**
     * ✅ 댓글 작성 (일반 사용자 + 관리자 모두 사용)
     */
    @Transactional
    public CommentResponse create(Long reviewId, CreateCommentRequest req, Account account) {
        // 1. 사용자 조회
        User user = userRepository.findById(account.getAccountId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));

        // 2. 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_REVIEW));

        // 3. ✅ 댓글 생성 (isSellerReply 파라미터 제거)
        ReviewComment comment = ReviewComment.create(review, user, req.getContent());
        commentRepository.save(comment);

        log.info("Comment created: commentId={}, reviewId={}, userId={}, userRole={}",
                comment.getId(), reviewId, user.getId(), user.getRole().getName());

        return commentMapper.toResponse(comment);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public CommentResponse update(Long commentId, UpdateCommentRequest req, Account account) {
        // 1. 댓글 조회
        ReviewComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COMMENT));

        // 2. 권한 검증 (본인 확인)
        if (!comment.getUser().getId().equals(account.getAccountId())) {
            throw new ServiceException(ServiceExceptionCode.UNAUTHORIZED_ACCESS);
        }

        // 3. 댓글 수정
        comment.update(req.getContent());
        commentRepository.save(comment);

        log.info("Comment updated: commentId={}, userId={}", commentId, account.getAccountId());

        return commentMapper.toResponse(comment);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void delete(Long commentId, Account account) {
        // 1. 댓글 조회
        ReviewComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COMMENT));

        // 2. 권한 검증 (본인 확인)
        if (!comment.getUser().getId().equals(account.getAccountId())) {
            throw new ServiceException(ServiceExceptionCode.UNAUTHORIZED_ACCESS);
        }

        // 3. 댓글 삭제
        commentRepository.delete(comment);

        log.info("Comment deleted: commentId={}, userId={}", commentId, account.getAccountId());
    }

    /**
     * 특정 리뷰의 댓글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long reviewId) {
        List<ReviewComment> comments = commentRepository.findByReviewIdOrderByCreatedAtAsc(reviewId);
        return commentMapper.toResponseList(comments);
    }

    /**
     * 내가 작성한 댓글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getMyComments(Account account) {
        List<ReviewComment> comments = commentRepository.findByUserIdOrderByCreatedAtDesc(account.getAccountId());
        return commentMapper.toResponseList(comments);
    }
}
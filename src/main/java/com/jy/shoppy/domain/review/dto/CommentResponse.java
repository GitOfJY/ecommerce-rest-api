package com.jy.shoppy.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "댓글 응답")
public class CommentResponse {
    @Schema(description = "댓글 ID")
    private Long commentId;

    @Schema(description = "리뷰 ID")
    private Long reviewId;

    @Schema(description = "작성자 ID")
    private Long userId;

    @Schema(description = "작성자명")
    private String username;

    @Schema(description = "댓글 내용")
    private String content;

    @Schema(description = "작성자 역할 (ROLE_USER, ROLE_ADMIN)", example = "ROLE_USER")
    private String userRole;

    @Schema(description = "작성일")
    private LocalDateTime createdAt;

    @Schema(description = "수정일")
    private LocalDateTime updatedAt;

    @Schema(description = "부모 댓글 ID (null이면 최상위 댓글)")
    private Long parentCommentId;

    @Schema(description = "댓글 깊이 (0: 최상위, 1: 1단계 대댓글, ...)")
    private Integer depth;

    @Schema(description = "대댓글 목록")
    private List<CommentResponse> childComments;

    public void addChildComment(List<CommentResponse> children) {
        if (this.childComments == null) {
            this.childComments = new ArrayList<>();
        }
        this.childComments.addAll(children);
    }
}
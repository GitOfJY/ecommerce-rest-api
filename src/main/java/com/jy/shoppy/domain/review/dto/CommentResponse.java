package com.jy.shoppy.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
}
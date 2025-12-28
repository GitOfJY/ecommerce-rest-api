package com.jy.shoppy.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "리뷰 응답")
public class ReviewResponse {

    @Schema(description = "리뷰 ID")
    private Long reviewId;

    @Schema(description = "작성자 닉네임")
    private String username;

    @Schema(description = "상품 ID")
    private Long productId;

    @Schema(description = "상품명")
    private String productName;

    @Schema(description = "리뷰 내용")
    private String content;

    @Schema(description = "별점 (1-5)")
    private Integer rating;

    @Schema(description = "사이즈 평가 (1-5)")
    private Integer sizeRating;

    @Schema(description = "색감 평가 (1-5)")
    private Integer colorRating;

    @Schema(description = "두께감 평가 (1-5)")
    private Integer thicknessRating;

    @Schema(description = "리뷰 이미지 목록")
    private List<String> imageUrls;

    @Schema(description = "도움이 돼요 개수")
    private Integer helpfulCount;

    @Schema(description = "댓글 개수")
    private Integer commentCount;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;
}
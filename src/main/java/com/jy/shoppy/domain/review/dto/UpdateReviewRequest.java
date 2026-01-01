package com.jy.shoppy.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리뷰 수정 요청")
public class UpdateReviewRequest {

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 1~5 사이여야 합니다.")
    @Max(value = 5, message = "평점은 1~5 사이여야 합니다.")
    @Schema(description = "평점 (1~5)", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer rating;

    @Min(value = 1, message = "사이즈 평가는 1~5 사이여야 합니다.")
    @Max(value = 5, message = "사이즈 평가는 1~5 사이여야 합니다.")
    @Schema(
            description = "사이즈 평가 (1:매우작음, 2:작음, 3:보통, 4:큼, 5:매우큼)",
            example = "3",
            nullable = true
    )
    private Integer sizeRating;

    @Min(value = 1, message = "색상 평가는 1~5 사이여야 합니다.")
    @Max(value = 5, message = "색상 평가는 1~5 사이여야 합니다.")
    @Schema(
            description = "색상 평가 (1:매우어두움, 2:어두움, 3:보통, 4:밝음, 5:매우밝음)",
            example = "4",
            nullable = true
    )
    private Integer colorRating;

    @Min(value = 1, message = "두께 평가는 1~5 사이여야 합니다.")
    @Max(value = 5, message = "두께 평가는 1~5 사이여야 합니다.")
    @Schema(
            description = "두께 평가 (1:매우얇음, 2:얇음, 3:보통, 4:두꺼움, 5:매우두꺼움)",
            example = "3",
            nullable = true
    )
    private Integer thicknessRating;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(min = 20, max = 500, message = "리뷰는 20자 이상 500자 이하로 작성해주세요.")
    @Schema(
            description = "리뷰 내용 (20~500자)",
            example = "수정된 리뷰 내용입니다. 상품이 정말 좋아서 다시 작성합니다!",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String content;

    @Size(max = 5, message = "이미지는 최대 5개까지 등록 가능합니다.")
    @Schema(
            description = "리뷰 이미지 URL 목록 (최대 5개)",
            example = "[\"http://localhost:8080/uploads/reviews/image1.jpg\", \"http://localhost:8080/uploads/reviews/image2.jpg\"]",
            nullable = true
    )
    private List<String> imageUrls;
}
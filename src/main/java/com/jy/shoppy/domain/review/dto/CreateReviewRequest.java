package com.jy.shoppy.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "리뷰 작성 요청")
public class CreateReviewRequest {
    @NotNull(message = "주문 상품 ID는 필수입니다")
    @Schema(description = "주문 상품 ID", example = "1")
    private Long orderProductId;

    @NotNull(message = "별점은 필수입니다")
    @Min(value = 1, message = "별점은 1점 이상이어야 합니다")
    @Max(value = 5, message = "별점은 5점 이하여야 합니다")
    @Schema(description = "상품 별점 (1-5)", example = "5")
    private Integer rating;

    @Min(value = 1)
    @Max(value = 5)
    @Schema(description = "사이즈 평가 (1:매우작음, 2:작음, 3:보통, 4:큼, 5:매우큼)", example = "3")
    private Integer sizeRating;

    @Min(value = 1)
    @Max(value = 5)
    @Schema(description = "색감 평가 (1:매우어두움, 2:어두움, 3:보통, 4:밝음, 5:매우밝음)", example = "3")
    private Integer colorRating;

    @Min(value = 1)
    @Max(value = 5)
    @Schema(description = "두께감 평가 (1:매우얇음, 2:얇음, 3:보통, 4:두꺼움, 5:매우두꺼움)", example = "4")
    private Integer thicknessRating;

    @NotBlank(message = "리뷰 내용은 필수입니다")
    @Size(min = 20, max = 500, message = "리뷰는 20자 이상 500자 이하로 작성해주세요")
    @Schema(description = "리뷰 내용 (20-500자)", example = "사이즈도 딱 맞고 색상도 사진과 동일해요...")
    private String content;

    @Schema(description = "리뷰 이미지 URL 목록 (최대 5개)",
            example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    @Size(max = 5, message = "이미지는 최대 5개까지 등록 가능합니다")
    private List<String> imageUrls;
}

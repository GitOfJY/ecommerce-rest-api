package com.jy.shoppy.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "댓글 작성 요청")
public class CreateCommentRequest {
    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(min = 1, max = 500, message = "댓글은 1자 이상 500자 이하로 작성해주세요.")
    @Schema(description = "댓글 내용 (1~500자)", example = "좋은 리뷰 감사합니다!")
    private String content;
}

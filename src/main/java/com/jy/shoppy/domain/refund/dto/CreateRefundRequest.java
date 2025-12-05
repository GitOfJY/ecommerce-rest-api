package com.jy.shoppy.domain.refund.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRefundRequest {
    @NotBlank
    private Long userId;

    @NotBlank
    private Long orderId;

    @NotBlank
    private String reason;
}

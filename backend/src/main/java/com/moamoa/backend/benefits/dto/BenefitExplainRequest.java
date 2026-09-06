package com.moamoa.backend.benefits.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BenefitExplainRequest(
        @NotBlank(message = "question은 필수입니다.") @Size(max = 500, message = "question은 500자를 넘을 수 없습니다.") String question
) {
}

package com.moamoa.backend.wage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record DeductionRequest(
        @NotBlank(message = "deductions[].name은 필수입니다.") String name,
        @NotNull(message = "deductions[].amount는 필수입니다.") @PositiveOrZero(message = "deductions[].amount는 0 이상이어야 합니다.") Long amount
) {
}

package com.moamoa.backend.wage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record WageCheckRequest(
        @NotNull(message = "base_wage는 필수입니다.") @Positive(message = "base_wage는 0보다 커야 합니다.") Long baseWage,
        @NotNull(message = "work_hours_per_week는 필수입니다.") @Positive(message = "work_hours_per_week는 0보다 커야 합니다.") Integer workHoursPerWeek,
        @NotNull(message = "overtime_hours는 필수입니다.") @PositiveOrZero(message = "overtime_hours는 0 이상이어야 합니다.") Integer overtimeHours,
        @NotNull(message = "overtime_pay는 필수입니다.") @PositiveOrZero(message = "overtime_pay는 0 이상이어야 합니다.") Long overtimePay,
        @NotNull(message = "deductions는 필수입니다.") @Valid List<DeductionRequest> deductions
) {
}

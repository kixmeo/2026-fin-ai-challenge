package com.moamoa.backend.common.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// AI 응답과 POST /api/wage-check 공개 응답이 동일한 모양이라 그대로 재사용함
public record MinimumWageCheck(
        boolean pass,
        long hourlyWage,
        @JsonProperty("minimum_wage_2026") int minimumWage2026
) {
}

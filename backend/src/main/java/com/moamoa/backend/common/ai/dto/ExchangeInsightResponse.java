package com.moamoa.backend.common.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

// AI 응답과 GET /api/exchange-rate/insight 공개 응답이 동일한 모양이라 그대로 재사용함
public record ExchangeInsightResponse(
        String currency,
        LocalDate date,
        double currentRate,
        @JsonProperty("percentile_30d") int percentile30d,
        @JsonProperty("percentile_90d") int percentile90d,
        String volatilityLevel,
        double volatilityScore,
        String message
) {
}

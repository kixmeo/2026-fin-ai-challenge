package com.moamoa.backend.common.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// AI 서버는 window가 아니라 실제 최신 환율 + 90일 시계열을 그대로 요구함.
// rateHistory90d는 기본 SNAKE_CASE 변환이 "rate_history90d"로 잘못 바꿔버려서(숫자 앞에 밑줄이 안 붙음,
// ExchangeInsightResponse의 percentile_30d/90d와 동일한 문제) 명시적으로 지정해야 함
public record ExchangeInsightRequest(
        String currency,
        double currentRate,
        @JsonProperty("rate_history_90d") List<RatePoint> rateHistory90d
) {
}

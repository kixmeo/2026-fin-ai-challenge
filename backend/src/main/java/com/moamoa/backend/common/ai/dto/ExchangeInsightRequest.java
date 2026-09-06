package com.moamoa.backend.common.ai.dto;

import java.util.List;

// AI 서버는 무상태라 window가 아니라 실제 최신 환율 + 90일 시계열을 그대로 요구함
public record ExchangeInsightRequest(String currency, double currentRate, List<RatePoint> rateHistory90d) {
}

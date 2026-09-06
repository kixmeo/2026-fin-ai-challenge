package com.moamoa.backend.common.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

// AI 응답과 POST /api/wage-check 공개 응답이 동일한 모양이라 그대로 재사용함.
// 다만 이 필드는 두 쪽의 JSON 키가 서로 다름 - AI 서버는 minimum_wage로 응답하고,
// 공개 API 스펙은 minimum_wage_2026으로 나가야 함. @JsonProperty가 출력(및 기본 입력) 키를 정하고,
// @JsonAlias로 AI 응답의 실제 키(minimum_wage)도 입력으로 추가 인식하게 함
public record MinimumWageCheck(
        boolean pass,
        double hourlyWage,
        @JsonProperty("minimum_wage_2026") @JsonAlias("minimum_wage") int minimumWage2026
) {
}

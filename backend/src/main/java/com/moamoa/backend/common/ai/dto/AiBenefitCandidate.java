package com.moamoa.backend.common.ai.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;

import java.time.LocalDate;

public record AiBenefitCandidate(
        String benefitId,
        // AI 서버가 title을 필수로 요구하는데 예전엔 아예 안 보내고 있었음
        String title,
        long amount,
        LocalDate deadline,
        int requiredDocsCount,
        // DB엔 불투명한 JSON 문자열로 저장돼 있음(BenefitCandidate 참고) - AI 서버는 이걸 문자열이 아니라
        // 객체(eligibility_rule)로 기대하므로 @JsonRawValue로 그대로 삽입해야 함, 안 그러면 JSON 문자열 안에
        // 이스케이프된 문자열 하나로 보내져서 422가 남
        @JsonRawValue String eligibilityRule
) {
}

package com.moamoa.backend.common.ai.dto;

import java.time.LocalDate;

public record AiBenefitCandidate(
        String benefitId,
        long amount,
        LocalDate deadline,
        int requiredDocsCount,
        String eligibilityRule
) {
}

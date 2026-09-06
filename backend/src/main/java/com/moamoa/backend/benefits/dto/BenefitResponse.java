package com.moamoa.backend.benefits.dto;

import java.time.LocalDate;

public record BenefitResponse(
        String benefitId,
        String title,
        double score,
        long amount,
        LocalDate deadline,
        int requiredDocsCount
) {
}

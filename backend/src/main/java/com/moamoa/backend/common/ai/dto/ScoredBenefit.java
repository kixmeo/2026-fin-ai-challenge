package com.moamoa.backend.common.ai.dto;

public record ScoredBenefit(String benefitId, boolean eligible, double score) {
}

package com.moamoa.backend.benefits.dto;

import java.util.List;

public record BenefitsResponse(boolean needsMoreInfo, String sessionId, List<BenefitResponse> benefits) {
}

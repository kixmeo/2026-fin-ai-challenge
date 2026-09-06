package com.moamoa.backend.common.ai.dto;

import java.util.List;

public record BenefitScoreRequest(AiUserProfile userProfile, List<AiBenefitCandidate> benefitCandidates) {
}

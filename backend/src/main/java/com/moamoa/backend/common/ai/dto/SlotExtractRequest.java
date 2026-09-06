package com.moamoa.backend.common.ai.dto;

// AI 서버는 known_income/known_work_period가 아니라 current_profile(UserProfile 모양)을 기대함
public record SlotExtractRequest(String message, AiUserProfile currentProfile) {
}

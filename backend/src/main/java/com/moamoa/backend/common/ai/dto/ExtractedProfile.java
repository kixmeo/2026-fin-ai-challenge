package com.moamoa.backend.common.ai.dto;

// AI 응답과 POST /api/chat/message 공개 응답의 extracted_profile이 동일한 모양이라 그대로 재사용함
public record ExtractedProfile(Long income, Integer workPeriod) {
}

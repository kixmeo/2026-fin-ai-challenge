package com.moamoa.backend.common.ai.dto;

// reaskCount는 AI 서버 응답의 필수 필드라 여기 없으면 역직렬화가 실패함 - 아직 세션에 추적/저장하진 않음
public record SlotExtractResponse(String reply, ExtractedProfile extractedProfile, boolean isComplete, int reaskCount) {
}

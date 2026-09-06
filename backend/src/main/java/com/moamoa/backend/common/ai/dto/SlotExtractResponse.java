package com.moamoa.backend.common.ai.dto;

public record SlotExtractResponse(String reply, ExtractedProfile extractedProfile, boolean isComplete) {
}

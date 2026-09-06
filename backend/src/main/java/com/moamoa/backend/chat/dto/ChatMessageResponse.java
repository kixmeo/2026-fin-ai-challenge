package com.moamoa.backend.chat.dto;

import com.moamoa.backend.common.ai.dto.ExtractedProfile;

public record ChatMessageResponse(String reply, ExtractedProfile extractedProfile, boolean isComplete) {
}

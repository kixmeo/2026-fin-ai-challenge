package com.moamoa.backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
        @NotBlank(message = "session_id는 필수입니다.") String sessionId,
        @NotBlank(message = "message는 필수입니다.") @Size(max = 500, message = "message는 500자를 넘을 수 없습니다.") String message
) {
}

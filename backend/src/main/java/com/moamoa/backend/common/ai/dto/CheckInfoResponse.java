package com.moamoa.backend.common.ai.dto;

import java.util.List;

// AI 서버 실제 응답 필드명(needs_more_info, missing_fields)에 맞춤 - 예전 sufficient(boolean) 하나로는
// AI 응답을 역직렬화할 수 없어서 항상 매칭 실패였음
public record CheckInfoResponse(boolean needsMoreInfo, List<String> missingFields) {
}

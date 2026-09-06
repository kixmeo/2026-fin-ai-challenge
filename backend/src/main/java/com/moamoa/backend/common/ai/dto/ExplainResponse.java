package com.moamoa.backend.common.ai.dto;

import java.util.List;

// AI 응답과 POST /api/benefits/{id}/explain 공개 응답이 동일한 모양이라 그대로 재사용함
public record ExplainResponse(String answer, List<String> sources) {
}

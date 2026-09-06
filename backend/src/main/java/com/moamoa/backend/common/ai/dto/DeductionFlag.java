package com.moamoa.backend.common.ai.dto;

// AI 응답과 POST /api/wage-check 공개 응답이 동일한 모양이라 그대로 재사용함
public record DeductionFlag(String name, String level, String reason) {
}

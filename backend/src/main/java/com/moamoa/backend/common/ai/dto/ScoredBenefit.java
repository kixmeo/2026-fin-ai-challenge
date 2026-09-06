package com.moamoa.backend.common.ai.dto;

import java.util.List;

// reasons는 AI 서버 응답의 필수 필드라 여기 없으면 역직렬화가 실패함 - 공개 API 응답엔 아직 안 실어보냄
public record ScoredBenefit(String benefitId, boolean eligible, double score, List<String> reasons) {
}

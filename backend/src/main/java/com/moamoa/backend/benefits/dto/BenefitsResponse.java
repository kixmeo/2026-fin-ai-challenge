package com.moamoa.backend.benefits.dto;

import java.util.List;

public record BenefitsResponse(boolean needsMoreInfo, String sessionId, List<BenefitResponse> benefits) {

    // needsMoreInfo=true/false는 sessionId 유무와 항상 짝을 이뤄야 함 - 둘이 어긋나면 프론트가
    // 어느 화면(채팅 vs 혜택 목록)으로 가야 할지 잘못 판단하므로 생성 시점에 바로 막음
    public BenefitsResponse {
        if (needsMoreInfo && (sessionId == null || sessionId.isBlank())) {
            throw new IllegalArgumentException("needsMoreInfo가 true면 sessionId가 필요합니다");
        }
        if (!needsMoreInfo && sessionId != null) {
            throw new IllegalArgumentException("needsMoreInfo가 false면 sessionId는 없어야 합니다");
        }
    }
}

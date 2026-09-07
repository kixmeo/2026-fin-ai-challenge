package com.moamoa.backend.exchange;

import com.moamoa.backend.common.ApiException;
import com.moamoa.backend.common.ApiResponse;
import com.moamoa.backend.common.ErrorCode;
import com.moamoa.backend.common.JwtUsers;
import com.moamoa.backend.common.ai.AiServerClient;
import com.moamoa.backend.common.ai.dto.ExchangeInsightRequest;
import com.moamoa.backend.common.ai.dto.ExchangeInsightResponse;
import com.moamoa.backend.common.ai.dto.RatePoint;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/exchange-rate")
public class ExchangeController {

    // 현재 ExchangeRateRefreshJob이 실제 이력을 쌓아둔 통화만 지원 - 그 외는 90일 시계열이 아예 없어서
    // AI 호출 자체가 무의미함
    private static final List<String> SUPPORTED_CURRENCIES = List.of("USD", "PHP", "VND", "THB");
    private static final int HISTORY_DAYS = 90;

    private final AiServerClient aiServerClient;
    private final ExchangeRateSnapshotRepository snapshotRepository;

    public ExchangeController(AiServerClient aiServerClient, ExchangeRateSnapshotRepository snapshotRepository) {
        this.aiServerClient = aiServerClient;
        this.snapshotRepository = snapshotRepository;
    }

    @GetMapping("/insight")
    public ApiResponse<ExchangeInsightResponse> getInsight(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String currency,
            @RequestParam(defaultValue = "30") int window
    ) {
        JwtUsers.requireUserId(jwt);
        String normalized = currency == null ? "" : currency.toUpperCase();
        if (!SUPPORTED_CURRENCIES.contains(normalized)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "지원하지 않는 통화입니다: " + currency);
        }

        List<ExchangeRateSnapshot> snapshots = snapshotRepository
                .findByCurrencyAndRateDateGreaterThanEqualOrderByRateDateAsc(normalized, LocalDate.now().minusDays(HISTORY_DAYS));
        if (snapshots.isEmpty()) {
            throw new ApiException(ErrorCode.EXTERNAL_API_ERROR, "환율 데이터를 아직 확인할 수 없습니다: " + normalized);
        }
        ExchangeRateSnapshot latest = snapshots.get(snapshots.size() - 1);
        List<RatePoint> history = snapshots.stream()
                .map(s -> new RatePoint(s.getRateDate(), s.getRate().doubleValue()))
                .toList();

        ExchangeInsightResponse aiResponse = aiServerClient.exchangeInsight(
                new ExchangeInsightRequest(normalized, latest.getRate().doubleValue(), history));
        AiServerClient.requireField(aiResponse.volatilityLevel(), "AI 서버가 유효하지 않은 환율 정보를 반환했습니다.");
        AiServerClient.requireField(aiResponse.message(), "AI 서버가 유효하지 않은 환율 정보를 반환했습니다.");

        // AI 서버 응답엔 currency/date가 없음(실제 응답 확인 결과 - API 명세서 예시와 달리 그 두 필드는
        // 안 돌려줌) - 우리가 이미 확실히 알고 있는 값(요청한 통화, 최신 스냅샷 날짜)으로 채워서 응답함
        ExchangeInsightResponse response = new ExchangeInsightResponse(
                normalized, latest.getRateDate(), latest.getRate().doubleValue(),
                aiResponse.percentile30d(), aiResponse.percentile90d(),
                aiResponse.volatilityLevel(), aiResponse.volatilityScore(), aiResponse.message());
        return ApiResponse.success(response);
    }
}

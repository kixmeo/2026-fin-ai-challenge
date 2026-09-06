package com.moamoa.backend.exchange;

import com.moamoa.backend.common.ApiException;
import com.moamoa.backend.common.ApiResponse;
import com.moamoa.backend.common.ErrorCode;
import com.moamoa.backend.common.JwtUsers;
import com.moamoa.backend.common.ai.AiServerClient;
import com.moamoa.backend.common.ai.dto.ExchangeInsightRequest;
import com.moamoa.backend.common.ai.dto.ExchangeInsightResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchange-rate")
public class ExchangeController {

    private final AiServerClient aiServerClient;

    public ExchangeController(AiServerClient aiServerClient) {
        this.aiServerClient = aiServerClient;
    }

    @GetMapping("/insight")
    public ApiResponse<ExchangeInsightResponse> getInsight(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String currency,
            @RequestParam(defaultValue = "30") int window
    ) {
        JwtUsers.requireUserId(jwt);
        if (currency.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "currency는 필수입니다.");
        }

        ExchangeInsightResponse response = aiServerClient.exchangeInsight(new ExchangeInsightRequest(currency, window));
        AiServerClient.requireField(response.currency(), "AI 서버가 유효하지 않은 환율 정보를 반환했습니다.");
        AiServerClient.requireField(response.date(), "AI 서버가 유효하지 않은 환율 정보를 반환했습니다.");
        AiServerClient.requireField(response.volatilityLevel(), "AI 서버가 유효하지 않은 환율 정보를 반환했습니다.");
        AiServerClient.requireField(response.message(), "AI 서버가 유효하지 않은 환율 정보를 반환했습니다.");
        return ApiResponse.success(response);
    }
}

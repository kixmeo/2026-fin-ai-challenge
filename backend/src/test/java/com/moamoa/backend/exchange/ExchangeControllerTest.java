package com.moamoa.backend.exchange;

import com.moamoa.backend.common.ai.AiServerClient;
import com.moamoa.backend.common.ai.dto.ExchangeInsightRequest;
import com.moamoa.backend.common.ai.dto.ExchangeInsightResponse;
import com.moamoa.backend.common.ai.dto.RatePoint;
import com.moamoa.backend.config.RestAccessDeniedHandler;
import com.moamoa.backend.config.RestAuthenticationEntryPoint;
import com.moamoa.backend.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class ExchangeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiServerClient aiServerClient;

    @MockitoBean
    private ExchangeRateSnapshotRepository snapshotRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final LocalDate SNAPSHOT_DATE = LocalDate.of(2026, 8, 18);

    private void stubSingleSnapshot(String currency, BigDecimal rate) {
        when(snapshotRepository.findByCurrencyAndRateDateGreaterThanEqualOrderByRateDateAsc(eq(currency), any()))
                .thenReturn(List.of(new ExchangeRateSnapshot(currency, SNAPSHOT_DATE, rate)));
    }

    @Test
    void returnsAiInsightForGivenCurrency() throws Exception {
        stubSingleSnapshot("USD", BigDecimal.valueOf(1385.2));
        when(aiServerClient.exchangeInsight(eq(new ExchangeInsightRequest(
                "USD", 1385.2, List.of(new RatePoint(SNAPSHOT_DATE, 1385.2))))))
                .thenReturn(new ExchangeInsightResponse(
                        "USD", LocalDate.of(2026, 8, 18), 1385.2, 82, 74, "high", 1.8, "최근 30일 중 상위 18%..."));

        mockMvc.perform(get("/api/exchange-rate/insight?currency=USD&window=30")
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.data.current_rate").value(1385.2))
                .andExpect(jsonPath("$.data.percentile_30d").value(82))
                .andExpect(jsonPath("$.data.percentile_90d").value(74))
                .andExpect(jsonPath("$.data.volatility_level").value("high"));
    }

    @Test
    void returnsAiServerErrorWhenVolatilityLevelIsMissing() throws Exception {
        stubSingleSnapshot("USD", BigDecimal.valueOf(1385.2));
        when(aiServerClient.exchangeInsight(eq(new ExchangeInsightRequest(
                "USD", 1385.2, List.of(new RatePoint(SNAPSHOT_DATE, 1385.2))))))
                .thenReturn(new ExchangeInsightResponse(
                        "USD", LocalDate.of(2026, 8, 18), 1385.2, 82, 74, null, 1.8, "최근 30일 중 상위 18%..."));

        mockMvc.perform(get("/api/exchange-rate/insight?currency=USD&window=30")
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value("AI_SERVER_ERROR"));
    }

    @Test
    void rejectsUnsupportedCurrency() throws Exception {
        mockMvc.perform(get("/api/exchange-rate/insight?currency=EUR")
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void returnsExternalApiErrorWhenNoSnapshotsYet() throws Exception {
        when(snapshotRepository.findByCurrencyAndRateDateGreaterThanEqualOrderByRateDateAsc(eq("VND"), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/exchange-rate/insight?currency=VND")
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value("EXTERNAL_API_ERROR"));
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/exchange-rate/insight?currency=USD"))
                .andExpect(status().isUnauthorized());
    }
}

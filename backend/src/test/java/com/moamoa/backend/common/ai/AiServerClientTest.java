package com.moamoa.backend.common.ai;

import com.moamoa.backend.common.ApiException;
import com.moamoa.backend.common.ErrorCode;
import com.moamoa.backend.common.ai.dto.ExchangeInsightRequest;
import com.moamoa.backend.common.ai.dto.ExchangeInsightResponse;
import com.moamoa.backend.common.ai.dto.RatePoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

// AiServerClient의 HTTP 배선(JSON 직렬화 규칙, 타임아웃 설정과 무관한 응답 파싱, 에러 변환)만 얇게 검증.
// 실제 프로덕션 RestClient는 Spring Boot가 자동구성한 SNAKE_CASE ObjectMapper를 쓰므로 여기서도 동일하게 맞춰줌.
class AiServerClientTest {

    private static final String BASE_URL = "http://ai.internal";

    private MockRestServiceServer server;
    private AiServerClient client;

    @BeforeEach
    void setUp() {
        JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter(
                JsonMapper.builder().propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).build());

        RestClient.Builder builder = RestClient.builder()
                .configureMessageConverters(mc -> mc.disableDefaults().withJsonConverter(converter));
        server = MockRestServiceServer.bindTo(builder).build();
        client = new AiServerClient(builder.baseUrl(BASE_URL).build());
    }

    @Test
    void deserializesSnakeCaseResponseFields() {
        server.expect(requestTo(BASE_URL + "/ai/exchange/insight"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "currency":"USD",
                          "date":"2026-08-18",
                          "current_rate":1385.2,
                          "percentile_30d":82,
                          "percentile_90d":74,
                          "volatility_level":"high",
                          "volatility_score":1.8,
                          "message":"최근 30일 중 상위 18%..."
                        }
                        """, MediaType.APPLICATION_JSON));

        ExchangeInsightResponse response = client.exchangeInsight(new ExchangeInsightRequest("USD", 1385.2, List.of(new RatePoint(LocalDate.of(2026, 8, 18), 1385.2))));

        assertThat(response.currentRate()).isEqualTo(1385.2);
        assertThat(response.percentile30d()).isEqualTo(82);
        assertThat(response.percentile90d()).isEqualTo(74);
        assertThat(response.volatilityLevel()).isEqualTo("high");
    }

    @Test
    void translatesAiServerFailureIntoApiException() {
        server.expect(requestTo(BASE_URL + "/ai/exchange/insight"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.exchangeInsight(new ExchangeInsightRequest("USD", 1385.2, List.of(new RatePoint(LocalDate.of(2026, 8, 18), 1385.2)))))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(ErrorCode.AI_SERVER_ERROR);
    }

    @Test
    void translatesEmptyBodyIntoApiExceptionInsteadOfSilentNull() {
        server.expect(requestTo(BASE_URL + "/ai/exchange/insight"))
                .andRespond(withSuccess());

        assertThatThrownBy(() -> client.exchangeInsight(new ExchangeInsightRequest("USD", 1385.2, List.of(new RatePoint(LocalDate.of(2026, 8, 18), 1385.2)))))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(ErrorCode.AI_SERVER_ERROR);
    }
}

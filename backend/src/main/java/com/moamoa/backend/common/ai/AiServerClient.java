package com.moamoa.backend.common.ai;

import com.moamoa.backend.common.ApiException;
import com.moamoa.backend.common.ErrorCode;
import com.moamoa.backend.common.ai.dto.BenefitScoreRequest;
import com.moamoa.backend.common.ai.dto.BenefitScoreResponse;
import com.moamoa.backend.common.ai.dto.CheckInfoRequest;
import com.moamoa.backend.common.ai.dto.CheckInfoResponse;
import com.moamoa.backend.common.ai.dto.ExchangeInsightRequest;
import com.moamoa.backend.common.ai.dto.ExchangeInsightResponse;
import com.moamoa.backend.common.ai.dto.ExplainRequest;
import com.moamoa.backend.common.ai.dto.ExplainResponse;
import com.moamoa.backend.common.ai.dto.SlotExtractRequest;
import com.moamoa.backend.common.ai.dto.SlotExtractResponse;
import com.moamoa.backend.common.ai.dto.WageClassifyRequest;
import com.moamoa.backend.common.ai.dto.WageClassifyResponse;
import com.moamoa.backend.common.ai.dto.WageVerifyRequest;
import com.moamoa.backend.common.ai.dto.WageVerifyResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// AI 서버(FastAPI, 별도 팀원 담당)는 완전히 무상태 - 매 호출마다 필요한 데이터를 전부 담아 보내고
// 판단/계산 결과만 받는다는 명세서의 설계 원칙을 그대로 따름
@Component
public class AiServerClient {

    private final RestClient restClient;

    public AiServerClient(RestClient aiServerRestClient) {
        this.restClient = aiServerRestClient;
    }

    public CheckInfoResponse checkInfo(CheckInfoRequest request) {
        return post("/ai/benefits/check-info", request, CheckInfoResponse.class);
    }

    public SlotExtractResponse slotExtract(SlotExtractRequest request) {
        return post("/ai/slot-extract", request, SlotExtractResponse.class);
    }

    public BenefitScoreResponse score(BenefitScoreRequest request) {
        return post("/ai/benefits/score", request, BenefitScoreResponse.class);
    }

    public ExplainResponse explain(ExplainRequest request) {
        return post("/ai/benefits/explain", request, ExplainResponse.class);
    }

    public WageVerifyResponse wageVerify(WageVerifyRequest request) {
        return post("/ai/wage/verify", request, WageVerifyResponse.class);
    }

    public WageClassifyResponse wageClassify(WageClassifyRequest request) {
        return post("/ai/wage/classify", request, WageClassifyResponse.class);
    }

    public ExchangeInsightResponse exchangeInsight(ExchangeInsightRequest request) {
        return post("/ai/exchange/insight", request, ExchangeInsightResponse.class);
    }

    // AI 응답이 200으로 왔어도 그 안의 특정 필드(중첩 객체/리스트)만 비어있을 수 있음 - post(...)의 빈 body
    // 가드는 이 케이스를 못 잡으므로, 호출부에서 실제로 쓰는 필드마다 이 헬퍼로 확인함
    public static <T> T requireField(T value, String message) {
        if (value == null) {
            throw new ApiException(ErrorCode.AI_SERVER_ERROR, message);
        }
        return value;
    }

    private <Req, Res> Res post(String path, Req request, Class<Res> responseType) {
        Res response;
        try {
            response = restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(responseType);
        } catch (RestClientException e) {
            throw new ApiException(ErrorCode.AI_SERVER_ERROR, "AI 서버 호출에 실패했습니다: " + path, e);
        }
        // AI 서버가 200과 함께 빈 body를 보내는 경우 .body(...)가 예외 없이 null을 반환함 -
        // 그대로 두면 가짜 성공 응답이 나가거나 호출부에서 NPE로 500이 뜸(의도한 502 대신)
        if (response == null) {
            throw new ApiException(ErrorCode.AI_SERVER_ERROR, "AI 서버가 빈 응답을 반환했습니다: " + path);
        }
        return response;
    }
}

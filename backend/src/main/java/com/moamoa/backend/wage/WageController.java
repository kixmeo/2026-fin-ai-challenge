package com.moamoa.backend.wage;

import com.moamoa.backend.common.ApiException;
import com.moamoa.backend.common.ApiResponse;
import com.moamoa.backend.common.ErrorCode;
import com.moamoa.backend.common.JwtUsers;
import com.moamoa.backend.common.ai.AiServerClient;
import com.moamoa.backend.common.ai.dto.DeductionItem;
import com.moamoa.backend.common.ai.dto.WageClassifyRequest;
import com.moamoa.backend.common.ai.dto.WageClassifyResponse;
import com.moamoa.backend.common.ai.dto.WageVerifyRequest;
import com.moamoa.backend.common.ai.dto.WageVerifyResponse;
import com.moamoa.backend.wage.dto.WageCheckRequest;
import com.moamoa.backend.wage.dto.WageCheckResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@RestController
@RequestMapping("/api/wage-check")
public class WageController {

    private static final Logger log = LoggerFactory.getLogger(WageController.class);

    private static final String DISCLAIMER =
            "본 결과는 참고용이며 법적 효력이 없습니다. 확인이 필요하면 고용노동부 상담(1350)을 이용하세요.";

    private final AiServerClient aiServerClient;

    public WageController(AiServerClient aiServerClient) {
        this.aiServerClient = aiServerClient;
    }

    @PostMapping
    public ApiResponse<WageCheckResponse> checkWage(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody WageCheckRequest request
    ) {
        JwtUsers.requireUserId(jwt);

        WageVerifyRequest verifyRequest = new WageVerifyRequest(
                request.baseWage(), request.workHoursPerWeek(), request.overtimeHours(), request.overtimePay());
        WageClassifyRequest classifyRequest = new WageClassifyRequest(
                request.deductions().stream().map(d -> new DeductionItem(d.name(), d.amount())).toList());

        // 최저임금/연장수당 검증(verify)과 공제항목 분류(classify)는 서로 독립적이라 병렬로 호출
        CompletableFuture<WageVerifyResponse> verifyFuture =
                CompletableFuture.supplyAsync(() -> aiServerClient.wageVerify(verifyRequest));
        CompletableFuture<WageClassifyResponse> classifyFuture =
                CompletableFuture.supplyAsync(() -> aiServerClient.wageClassify(classifyRequest));

        // allOf().join()은 둘 다 실패해도 하나의 예외만 드러내므로, 로그에는 두 실패를 모두 남기기 위해
        // 각 future를 개별적으로 join한다 (둘 다 이미 병렬로 실행 중이라 순서대로 join해도 대기 시간은 늘지 않음)
        List<Throwable> failures = new ArrayList<>();
        WageVerifyResponse verify = join(verifyFuture, failures);
        WageClassifyResponse classify = join(classifyFuture, failures);

        if (!failures.isEmpty()) {
            failures.forEach(f -> log.error("임금 진단 AI 호출 실패", f));
            Throwable primary = failures.get(0);
            if (primary instanceof ApiException apiException) {
                throw apiException;
            }
            if (primary instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new ApiException(ErrorCode.AI_SERVER_ERROR, "AI 서버 호출에 실패했습니다.", primary);
        }

        AiServerClient.requireField(verify.minimumWageCheck(), "AI 서버가 유효하지 않은 임금 검증 결과를 반환했습니다.");
        AiServerClient.requireField(verify.overtimeCheck(), "AI 서버가 유효하지 않은 임금 검증 결과를 반환했습니다.");
        AiServerClient.requireField(classify.deductionFlags(), "AI 서버가 유효하지 않은 공제 분류 결과를 반환했습니다.");

        return ApiResponse.success(new WageCheckResponse(
                verify.minimumWageCheck(), verify.overtimeCheck(), classify.deductionFlags(), DISCLAIMER));
    }

    private static <T> T join(CompletableFuture<T> future, List<Throwable> failures) {
        try {
            return future.join();
        } catch (CompletionException e) {
            failures.add(e.getCause() != null ? e.getCause() : e);
            return null;
        }
    }
}

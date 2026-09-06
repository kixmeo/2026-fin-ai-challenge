package com.moamoa.backend.wage;

import com.moamoa.backend.common.ApiException;
import com.moamoa.backend.common.ErrorCode;
import com.moamoa.backend.common.ai.AiServerClient;
import com.moamoa.backend.common.ai.dto.DeductionFlag;
import com.moamoa.backend.common.ai.dto.MinimumWageCheck;
import com.moamoa.backend.common.ai.dto.OvertimeCheck;
import com.moamoa.backend.common.ai.dto.WageClassifyResponse;
import com.moamoa.backend.common.ai.dto.WageVerifyResponse;
import com.moamoa.backend.config.RestAccessDeniedHandler;
import com.moamoa.backend.config.RestAuthenticationEntryPoint;
import com.moamoa.backend.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WageController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class WageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiServerClient aiServerClient;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final String VALID_REQUEST = """
            {
              "base_wage": 2100000,
              "work_hours_per_week": 40,
              "overtime_hours": 8,
              "overtime_pay": 60000,
              "deductions": [
                {"name": "국민연금", "amount": 94500},
                {"name": "숙식비", "amount": 300000}
              ]
            }
            """;

    @Test
    void combinesVerifyAndClassifyResultsIntoOneResponse() throws Exception {
        when(aiServerClient.wageVerify(any())).thenReturn(new WageVerifyResponse(
                new MinimumWageCheck(true, 13125, 10320),
                new OvertimeCheck(false, 78750, 60000)));
        when(aiServerClient.wageClassify(any())).thenReturn(new WageClassifyResponse(List.of(
                new DeductionFlag("국민연금", "정당", "법정 4대보험 공제"),
                new DeductionFlag("숙식비", "주의", "상한 규정 확인 필요"))));

        mockMvc.perform(post("/api/wage-check")
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.minimum_wage_check.pass").value(true))
                .andExpect(jsonPath("$.data.minimum_wage_check.hourly_wage").value(13125))
                .andExpect(jsonPath("$.data.minimum_wage_check.minimum_wage_2026").value(10320))
                .andExpect(jsonPath("$.data.overtime_check.pass").value(false))
                .andExpect(jsonPath("$.data.deduction_flags[0].level").value("정당"))
                .andExpect(jsonPath("$.data.deduction_flags[1].level").value("주의"))
                .andExpect(jsonPath("$.data.disclaimer").exists());
    }

    @Test
    void translatesAiServerFailureIntoAiServerErrorCode() throws Exception {
        when(aiServerClient.wageVerify(any()))
                .thenThrow(new ApiException(ErrorCode.AI_SERVER_ERROR, "AI 서버 호출에 실패했습니다: /ai/wage/verify"));
        when(aiServerClient.wageClassify(any())).thenReturn(new WageClassifyResponse(List.of()));

        mockMvc.perform(post("/api/wage-check")
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value("AI_SERVER_ERROR"));
    }

    @Test
    void stillReturnsAiServerErrorWhenBothCallsFail() throws Exception {
        when(aiServerClient.wageVerify(any()))
                .thenThrow(new ApiException(ErrorCode.AI_SERVER_ERROR, "AI 서버 호출에 실패했습니다: /ai/wage/verify"));
        when(aiServerClient.wageClassify(any()))
                .thenThrow(new ApiException(ErrorCode.AI_SERVER_ERROR, "AI 서버 호출에 실패했습니다: /ai/wage/classify"));

        mockMvc.perform(post("/api/wage-check")
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value("AI_SERVER_ERROR"));
    }

    @Test
    void returnsAiServerErrorWhenVerifyResultIsMissing() throws Exception {
        when(aiServerClient.wageVerify(any())).thenReturn(new WageVerifyResponse(null, null));
        when(aiServerClient.wageClassify(any())).thenReturn(new WageClassifyResponse(List.of()));

        mockMvc.perform(post("/api/wage-check")
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value("AI_SERVER_ERROR"));
    }

    @Test
    void rejectsNonPositiveBaseWage() throws Exception {
        mockMvc.perform(post("/api/wage-check")
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "base_wage": 0,
                                  "work_hours_per_week": 40,
                                  "overtime_hours": 8,
                                  "overtime_pay": 60000,
                                  "deductions": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(post("/api/wage-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isUnauthorized());
    }
}

package com.moamoa.backend.benefits;

import com.moamoa.backend.calendar.CalendarEvent;
import com.moamoa.backend.calendar.CalendarEventRepository;
import com.moamoa.backend.chat.ChatSession;
import com.moamoa.backend.chat.ChatSessionRepository;
import com.moamoa.backend.common.ai.AiServerClient;
import com.moamoa.backend.common.ai.dto.BenefitScoreResponse;
import com.moamoa.backend.common.ai.dto.CheckInfoResponse;
import com.moamoa.backend.common.ai.dto.ExplainResponse;
import com.moamoa.backend.common.ai.dto.ScoredBenefit;
import com.moamoa.backend.config.RestAccessDeniedHandler;
import com.moamoa.backend.config.RestAuthenticationEntryPoint;
import com.moamoa.backend.config.SecurityConfig;
import com.moamoa.backend.profile.Profile;
import com.moamoa.backend.profile.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BenefitsController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class BenefitsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileRepository profileRepository;

    @MockitoBean
    private BenefitCandidateRepository benefitCandidateRepository;

    @MockitoBean
    private ChatSessionRepository chatSessionRepository;

    @MockitoBean
    private CalendarEventRepository calendarEventRepository;

    @MockitoBean
    private AiServerClient aiServerClient;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private Profile profileWithIncomeAndWorkPeriod(UUID userId) {
        Profile profile = new Profile(userId, "E-9", "Nguyen Van A", "안산시");
        profile.applyExtractedInfo(2500000L, 14);
        return profile;
    }

    @Test
    void returnsProfileIncompleteWhenNoBasicProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/benefits").with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("PROFILE_INCOMPLETE"));
    }

    @Test
    void returnsNeedsMoreInfoAndCreatesSessionWhenAiSaysInsufficient() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findById(userId)).thenReturn(Optional.of(new Profile(userId, "E-9", "Nguyen Van A", "안산시")));
        when(aiServerClient.checkInfo(any())).thenReturn(new CheckInfoResponse(true, List.of("income", "work_period")));
        when(chatSessionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(chatSessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(get("/api/benefits").with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.needs_more_info").value(true))
                .andExpect(jsonPath("$.data.session_id").exists())
                .andExpect(jsonPath("$.data.benefits").isEmpty());

        verify(chatSessionRepository).save(any());
    }

    @Test
    void reusesExistingSessionInsteadOfCreatingANewOne() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findById(userId)).thenReturn(Optional.of(new Profile(userId, "E-9", "Nguyen Van A", "안산시")));
        when(aiServerClient.checkInfo(any())).thenReturn(new CheckInfoResponse(true, List.of("income", "work_period")));
        ChatSession existing = new ChatSession("chat_existing", userId, 2500000L, null);
        when(chatSessionRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        mockMvc.perform(get("/api/benefits").with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.session_id").value("chat_existing"));

        verify(chatSessionRepository, never()).save(any());
    }

    @Test
    void returnsSortedEligibleBenefitsWhenAiSaysSufficient() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findById(userId)).thenReturn(Optional.of(profileWithIncomeAndWorkPeriod(userId)));
        when(aiServerClient.checkInfo(any())).thenReturn(new CheckInfoResponse(false, List.of()));

        BenefitCandidate low = new BenefitCandidate("b_009", "산업재해 소액치료비 지원", 300000,
                LocalDate.of(2026, 10, 5), 1, "{}", "설명");
        BenefitCandidate high = new BenefitCandidate("b_017", "외국인근로자 귀국비용보험", 500000,
                LocalDate.of(2026, 9, 30), 2, "{}", "설명");
        when(benefitCandidateRepository.findAll()).thenReturn(List.of(low, high));

        when(aiServerClient.score(any())).thenReturn(new BenefitScoreResponse(List.of(
                new ScoredBenefit("b_009", true, 0.58, List.of("소득 기준 충족")),
                new ScoredBenefit("b_017", true, 0.86, List.of("체류자격 충족")))));

        mockMvc.perform(get("/api/benefits").with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.needs_more_info").value(false))
                .andExpect(jsonPath("$.data.benefits[0].benefit_id").value("b_017"))
                .andExpect(jsonPath("$.data.benefits[0].score").value(0.86))
                .andExpect(jsonPath("$.data.benefits[1].benefit_id").value("b_009"));
    }

    @Test
    void excludesIneligibleBenefits() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findById(userId)).thenReturn(Optional.of(profileWithIncomeAndWorkPeriod(userId)));
        when(aiServerClient.checkInfo(any())).thenReturn(new CheckInfoResponse(false, List.of()));

        BenefitCandidate candidate = new BenefitCandidate("b_021", "외국인근로자 국민연금 반환일시금", 1200000,
                LocalDate.of(2026, 11, 15), 3, "{}", "설명");
        when(benefitCandidateRepository.findAll()).thenReturn(List.of(candidate));
        when(aiServerClient.score(any())).thenReturn(new BenefitScoreResponse(List.of(
                new ScoredBenefit("b_021", false, 0.1, List.of("근속기간 미달")))));

        mockMvc.perform(get("/api/benefits").with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.benefits").isEmpty());
    }

    @Test
    void returnsAiServerErrorWhenAiReturnsUnknownBenefitId() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findById(userId)).thenReturn(Optional.of(profileWithIncomeAndWorkPeriod(userId)));
        when(aiServerClient.checkInfo(any())).thenReturn(new CheckInfoResponse(false, List.of()));

        BenefitCandidate candidate = new BenefitCandidate("b_021", "외국인근로자 국민연금 반환일시금", 1200000,
                LocalDate.of(2026, 11, 15), 3, "{}", "설명");
        when(benefitCandidateRepository.findAll()).thenReturn(List.of(candidate));
        when(aiServerClient.score(any())).thenReturn(new BenefitScoreResponse(List.of(
                new ScoredBenefit("b_999", true, 0.9, List.of()))));

        mockMvc.perform(get("/api/benefits").with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value("AI_SERVER_ERROR"));
    }

    @Test
    void returnsAiServerErrorWhenScoredBenefitsIsMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findById(userId)).thenReturn(Optional.of(profileWithIncomeAndWorkPeriod(userId)));
        when(aiServerClient.checkInfo(any())).thenReturn(new CheckInfoResponse(false, List.of()));
        when(benefitCandidateRepository.findAll()).thenReturn(List.of());
        when(aiServerClient.score(any())).thenReturn(new BenefitScoreResponse(null));

        mockMvc.perform(get("/api/benefits").with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value("AI_SERVER_ERROR"));
    }

    @Test
    void explainReturnsAiAnswerAsIs() throws Exception {
        BenefitCandidate candidate = new BenefitCandidate("b_017", "외국인근로자 귀국비용보험", 500000,
                LocalDate.of(2026, 9, 30), 2, "{}", "설명");
        when(benefitCandidateRepository.findById("b_017")).thenReturn(Optional.of(candidate));
        when(aiServerClient.explain(any())).thenReturn(new ExplainResponse("설명입니다.", List.of("고용노동부 공고 2026-114호")));

        mockMvc.perform(post("/api/benefits/b_017/explain")
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"필요서류가 뭔데"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answer").value("설명입니다."))
                .andExpect(jsonPath("$.data.sources[0]").value("고용노동부 공고 2026-114호"));
    }

    @Test
    void returnsAiServerErrorWhenExplainAnswerIsMissing() throws Exception {
        BenefitCandidate candidate = new BenefitCandidate("b_017", "외국인근로자 귀국비용보험", 500000,
                LocalDate.of(2026, 9, 30), 2, "{}", "설명");
        when(benefitCandidateRepository.findById("b_017")).thenReturn(Optional.of(candidate));
        when(aiServerClient.explain(any())).thenReturn(new ExplainResponse(null, null));

        mockMvc.perform(post("/api/benefits/b_017/explain")
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"필요서류가 뭔데"}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value("AI_SERVER_ERROR"));
    }

    @Test
    void returnsNotFoundWhenExplainingUnknownBenefit() throws Exception {
        when(benefitCandidateRepository.findById("nope")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/benefits/nope/explain")
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"필요서류가 뭔데"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void rejectsBlankExplainQuestion() throws Exception {
        mockMvc.perform(post("/api/benefits/b_017/explain")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void addsBenefitDeadlineToCalendar() throws Exception {
        BenefitCandidate candidate = new BenefitCandidate("b_017", "외국인근로자 귀국비용보험", 500000,
                LocalDate.of(2026, 9, 30), 2, "{}", "설명");
        when(benefitCandidateRepository.findById("b_017")).thenReturn(Optional.of(candidate));
        when(calendarEventRepository.save(any())).thenAnswer(invocation -> {
            CalendarEvent saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            return saved;
        });

        mockMvc.perform(post("/api/benefits/b_017/calendar")
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.calendar_event_id").exists());
    }

    @Test
    void returnsNotFoundWhenBenefitDoesNotExistForCalendar() throws Exception {
        when(benefitCandidateRepository.findById("nope")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/benefits/nope/calendar")
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/benefits"))
                .andExpect(status().isUnauthorized());
    }
}

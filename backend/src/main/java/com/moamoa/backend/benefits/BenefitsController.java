package com.moamoa.backend.benefits;

import com.moamoa.backend.benefits.dto.BenefitExplainRequest;
import com.moamoa.backend.benefits.dto.BenefitResponse;
import com.moamoa.backend.benefits.dto.BenefitsResponse;
import com.moamoa.backend.calendar.CalendarEvent;
import com.moamoa.backend.calendar.CalendarEventRepository;
import com.moamoa.backend.calendar.dto.CalendarEventCreatedResponse;
import com.moamoa.backend.chat.ChatSession;
import com.moamoa.backend.chat.ChatSessionRepository;
import com.moamoa.backend.common.ApiException;
import com.moamoa.backend.common.ApiResponse;
import com.moamoa.backend.common.ErrorCode;
import com.moamoa.backend.common.JwtUsers;
import com.moamoa.backend.common.ai.AiServerClient;
import com.moamoa.backend.common.ai.dto.AiBenefitCandidate;
import com.moamoa.backend.common.ai.dto.AiUserProfile;
import com.moamoa.backend.common.ai.dto.BenefitScoreRequest;
import com.moamoa.backend.common.ai.dto.BenefitScoreResponse;
import com.moamoa.backend.common.ai.dto.CheckInfoResponse;
import com.moamoa.backend.common.ai.dto.ExplainRequest;
import com.moamoa.backend.common.ai.dto.ExplainResponse;
import com.moamoa.backend.common.ai.dto.ScoredBenefit;
import com.moamoa.backend.profile.Profile;
import com.moamoa.backend.profile.ProfileRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/benefits")
public class BenefitsController {

    private final ProfileRepository profileRepository;
    private final BenefitCandidateRepository benefitCandidateRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final AiServerClient aiServerClient;

    public BenefitsController(
            ProfileRepository profileRepository,
            BenefitCandidateRepository benefitCandidateRepository,
            ChatSessionRepository chatSessionRepository,
            CalendarEventRepository calendarEventRepository,
            AiServerClient aiServerClient
    ) {
        this.profileRepository = profileRepository;
        this.benefitCandidateRepository = benefitCandidateRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.calendarEventRepository = calendarEventRepository;
        this.aiServerClient = aiServerClient;
    }

    @GetMapping
    public ApiResponse<BenefitsResponse> getBenefits(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = JwtUsers.requireUserId(jwt);
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROFILE_INCOMPLETE, "기본 프로필이 완성되지 않았습니다."));

        CheckInfoResponse checkInfo = aiServerClient.checkInfo(
                new AiUserProfile(profile.getVisaType(), profile.getIncome(), profile.getWorkPeriod()));

        if (!checkInfo.sufficient()) {
            // 진행 중인 세션이 이미 있으면 재사용 - 매번 새로 만들면 GET /api/benefits를 반복 호출할 때마다 고아 행이 쌓임
            ChatSession session = chatSessionRepository.findByUserId(userId)
                    .orElseGet(() -> chatSessionRepository.save(new ChatSession(
                            "chat_" + UUID.randomUUID(), userId, profile.getIncome(), profile.getWorkPeriod())));
            return ApiResponse.success(new BenefitsResponse(true, session.getSessionId(), List.of()));
        }

        List<BenefitCandidate> candidates = benefitCandidateRepository.findAll();
        BenefitScoreResponse scoreResponse = aiServerClient.score(new BenefitScoreRequest(
                new AiUserProfile(profile.getVisaType(), profile.getIncome(), profile.getWorkPeriod()),
                candidates.stream().map(this::toAiCandidate).toList()));
        AiServerClient.requireField(scoreResponse.scoredBenefits(), "AI 서버가 scored_benefits를 반환하지 않았습니다.");

        Map<String, BenefitCandidate> candidatesById = candidates.stream()
                .collect(Collectors.toMap(BenefitCandidate::getBenefitId, c -> c));

        List<BenefitResponse> benefits = scoreResponse.scoredBenefits().stream()
                .filter(ScoredBenefit::eligible)
                .sorted(Comparator.comparingDouble(ScoredBenefit::score).reversed())
                .map(scored -> toBenefitResponse(scored, candidatesById))
                .toList();

        return ApiResponse.success(new BenefitsResponse(false, null, benefits));
    }

    @PostMapping("/{benefitId}/explain")
    public ApiResponse<ExplainResponse> explain(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String benefitId,
            @Valid @RequestBody BenefitExplainRequest request
    ) {
        JwtUsers.requireUserId(jwt);
        if (!benefitCandidateRepository.existsById(benefitId)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.");
        }
        ExplainResponse response = aiServerClient.explain(new ExplainRequest(benefitId, request.question()));
        AiServerClient.requireField(response.answer(), "AI 서버가 유효하지 않은 설명 응답을 반환했습니다.");
        AiServerClient.requireField(response.sources(), "AI 서버가 유효하지 않은 설명 응답을 반환했습니다.");
        return ApiResponse.success(response);
    }

    @PostMapping("/{benefitId}/calendar")
    public ApiResponse<CalendarEventCreatedResponse> addToCalendar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String benefitId
    ) {
        UUID userId = JwtUsers.requireUserId(jwt);
        BenefitCandidate candidate = benefitCandidateRepository.findById(benefitId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."));

        CalendarEvent saved = calendarEventRepository.save(
                new CalendarEvent(userId, candidate.getTitle(), candidate.getDeadline()));
        return ApiResponse.success(new CalendarEventCreatedResponse(saved.getId().toString()));
    }

    private AiBenefitCandidate toAiCandidate(BenefitCandidate c) {
        return new AiBenefitCandidate(
                c.getBenefitId(), c.getAmount(), c.getDeadline(), c.getRequiredDocsCount(), c.getEligibilityRule());
    }

    private BenefitResponse toBenefitResponse(ScoredBenefit scored, Map<String, BenefitCandidate> candidatesById) {
        BenefitCandidate c = AiServerClient.requireField(
                candidatesById.get(scored.benefitId()), "AI 서버가 알 수 없는 benefit_id를 반환했습니다: " + scored.benefitId());
        return new BenefitResponse(c.getBenefitId(), c.getTitle(), scored.score(), c.getAmount(), c.getDeadline(), c.getRequiredDocsCount());
    }
}

package com.moamoa.backend.chat;

import com.moamoa.backend.chat.dto.ChatMessageRequest;
import com.moamoa.backend.chat.dto.ChatMessageResponse;
import com.moamoa.backend.common.ApiException;
import com.moamoa.backend.common.ApiResponse;
import com.moamoa.backend.common.ErrorCode;
import com.moamoa.backend.common.JwtUsers;
import com.moamoa.backend.common.ai.AiServerClient;
import com.moamoa.backend.common.ai.dto.ExtractedProfile;
import com.moamoa.backend.common.ai.dto.SlotExtractRequest;
import com.moamoa.backend.common.ai.dto.SlotExtractResponse;
import com.moamoa.backend.profile.Profile;
import com.moamoa.backend.profile.ProfileRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatSessionRepository chatSessionRepository;
    private final ProfileRepository profileRepository;
    private final AiServerClient aiServerClient;

    public ChatController(
            ChatSessionRepository chatSessionRepository,
            ProfileRepository profileRepository,
            AiServerClient aiServerClient
    ) {
        this.chatSessionRepository = chatSessionRepository;
        this.profileRepository = profileRepository;
        this.aiServerClient = aiServerClient;
    }

    @PostMapping("/message")
    public ApiResponse<ChatMessageResponse> sendMessage(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChatMessageRequest request
    ) {
        UUID userId = JwtUsers.requireUserId(jwt);
        // 세션이 없거나 다른 사용자 소유면 존재 여부를 흘리지 않기 위해 동일하게 NOT_FOUND (캘린더 삭제와 동일 패턴)
        ChatSession session = chatSessionRepository.findById(request.sessionId())
                .filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."));

        SlotExtractResponse aiResponse = aiServerClient.slotExtract(
                new SlotExtractRequest(request.message(), session.getIncome(), session.getWorkPeriod()));
        AiServerClient.requireField(aiResponse.reply(), "AI 서버가 유효한 응답을 반환하지 않았습니다.");

        ExtractedProfile extracted = aiResponse.extractedProfile();
        if (extracted != null) {
            session.applyExtracted(extracted.income(), extracted.workPeriod());
        }

        if (aiResponse.isComplete()) {
            Profile profile = profileRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(ErrorCode.PROFILE_INCOMPLETE, "기본 프로필이 완성되지 않았습니다."));
            profile.applyExtractedInfo(session.getIncome(), session.getWorkPeriod());
            profileRepository.save(profile);
            // 완료된 세션은 더 이상 필요 없음 - 지워야 GET /api/benefits의 세션 재사용 로직이 다음번에 새 세션을 만들 수 있음
            chatSessionRepository.delete(session);
        } else {
            chatSessionRepository.save(session);
        }

        // extracted(이번 턴 delta)가 아니라 세션 누적값을 돌려줌 - 그래야 프론트가 "지금까지 모은 값"을
        // 표시할 때 이번 턴에 새로 추출된 게 없어도 이전 턴 값이 사라져 보이지 않음
        ExtractedProfile accumulated = new ExtractedProfile(session.getIncome(), session.getWorkPeriod());
        return ApiResponse.success(new ChatMessageResponse(aiResponse.reply(), accumulated, aiResponse.isComplete()));
    }
}

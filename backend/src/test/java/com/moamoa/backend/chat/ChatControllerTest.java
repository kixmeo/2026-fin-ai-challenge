package com.moamoa.backend.chat;

import com.moamoa.backend.common.ai.AiServerClient;
import com.moamoa.backend.common.ai.dto.ExtractedProfile;
import com.moamoa.backend.common.ai.dto.SlotExtractResponse;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatSessionRepository chatSessionRepository;

    @MockitoBean
    private ProfileRepository profileRepository;

    @MockitoBean
    private AiServerClient aiServerClient;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void mergesExtractedIncomeIntoSessionButNotProfileWhenNotComplete() throws Exception {
        UUID userId = UUID.randomUUID();
        ChatSession session = new ChatSession("chat_abc123", userId, null, null);
        when(chatSessionRepository.findById("chat_abc123")).thenReturn(Optional.of(session));
        when(profileRepository.findById(userId)).thenReturn(Optional.of(new Profile(userId, "E-9", "Nguyen Van A", "안산시")));
        when(aiServerClient.slotExtract(any())).thenReturn(new SlotExtractResponse(
                "그럼 지금 근무하신 지는 얼마나 되셨어요?", new ExtractedProfile(2500000L, null), false, 0));

        mockMvc.perform(post("/api/chat/message")
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"session_id":"chat_abc123","message":"월급은 250만원 정도예요"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reply").value("그럼 지금 근무하신 지는 얼마나 되셨어요?"))
                .andExpect(jsonPath("$.data.extracted_profile.income").value(2500000))
                .andExpect(jsonPath("$.data.is_complete").value(false));

        verify(chatSessionRepository).save(session);
        assertThat(session.getIncome()).isEqualTo(2500000L);
        assertThat(session.getWorkPeriod()).isNull();
        verify(profileRepository, never()).save(any());
    }

    @Test
    void handlesTurnWhereAiExtractsNothing() throws Exception {
        UUID userId = UUID.randomUUID();
        ChatSession session = new ChatSession("chat_abc123", userId, 2500000L, null);
        when(chatSessionRepository.findById("chat_abc123")).thenReturn(Optional.of(session));
        when(profileRepository.findById(userId)).thenReturn(Optional.of(new Profile(userId, "E-9", "Nguyen Van A", "안산시")));
        when(aiServerClient.slotExtract(any())).thenReturn(new SlotExtractResponse(
                "죄송해요, 잘 못 알아들었어요. 다시 한 번 말씀해주시겠어요?", null, false, 1));

        mockMvc.perform(post("/api/chat/message")
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"session_id":"chat_abc123","message":"음..."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.is_complete").value(false))
                // 이번 턴엔 아무것도 못 뽑았어도, 세션에 누적된 이전 턴 값(income)은 응답에 그대로 남아있어야 함
                .andExpect(jsonPath("$.data.extracted_profile.income").value(2500000));

        assertThat(session.getIncome()).isEqualTo(2500000L);
        assertThat(session.getWorkPeriod()).isNull();
        verify(chatSessionRepository).save(session);
    }

    @Test
    void returnsAiServerErrorWhenReplyIsMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        ChatSession session = new ChatSession("chat_abc123", userId, null, null);
        when(chatSessionRepository.findById("chat_abc123")).thenReturn(Optional.of(session));
        when(profileRepository.findById(userId)).thenReturn(Optional.of(new Profile(userId, "E-9", "Nguyen Van A", "안산시")));
        when(aiServerClient.slotExtract(any())).thenReturn(new SlotExtractResponse(null, null, false, 0));

        mockMvc.perform(post("/api/chat/message")
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"session_id":"chat_abc123","message":"안녕하세요"}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value("AI_SERVER_ERROR"));
    }

    @Test
    void mergesIntoProfileWhenComplete() throws Exception {
        UUID userId = UUID.randomUUID();
        ChatSession session = new ChatSession("chat_abc123", userId, 2500000L, null);
        Profile profile = new Profile(userId, "E-9", "Nguyen Van A", "안산시");
        when(chatSessionRepository.findById("chat_abc123")).thenReturn(Optional.of(session));
        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(aiServerClient.slotExtract(any())).thenReturn(new SlotExtractResponse(
                "감사해요!", new ExtractedProfile(null, 14), true, 0));

        mockMvc.perform(post("/api/chat/message")
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"session_id":"chat_abc123","message":"14개월이요"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.is_complete").value(true));

        verify(profileRepository).save(profile);
        assertThat(profile.getIncome()).isEqualTo(2500000L);
        assertThat(profile.getWorkPeriod()).isEqualTo(14);
        verify(chatSessionRepository).delete(session);
        verify(chatSessionRepository, never()).save(any());
    }

    @Test
    void returnsNotFoundForUnknownSession() throws Exception {
        when(chatSessionRepository.findById("chat_unknown")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/chat/message")
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"session_id":"chat_unknown","message":"안녕하세요"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void returnsNotFoundForSomeoneElsesSession() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID requester = UUID.randomUUID();
        when(chatSessionRepository.findById("chat_abc123")).thenReturn(Optional.of(new ChatSession("chat_abc123", owner, null, null)));

        mockMvc.perform(post("/api/chat/message")
                        .with(jwt().jwt(j -> j.subject(requester.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"session_id":"chat_abc123","message":"안녕하세요"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void rejectsBlankMessage() throws Exception {
        mockMvc.perform(post("/api/chat/message")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"session_id":"chat_abc123","message":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"session_id":"chat_abc123","message":"안녕하세요"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}

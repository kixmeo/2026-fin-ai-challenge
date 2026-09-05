package com.moamoa.backend.calendar;

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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalendarController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalendarEventRepository calendarEventRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void returnsOnlyCurrentUsersEvents() throws Exception {
        UUID userId = UUID.randomUUID();
        CalendarEvent event = new CalendarEvent(userId, "귀국비용보험 신청", LocalDate.of(2026, 9, 30));
        ReflectionTestUtils.setField(event, "id", UUID.randomUUID());
        when(calendarEventRepository.findByUserIdOrderByDateAsc(userId)).thenReturn(List.of(event));

        mockMvc.perform(get("/api/calendar").with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.events[0].title").value("귀국비용보험 신청"))
                .andExpect(jsonPath("$.data.events[0].date").value("2026-09-30"));
    }

    @Test
    void createsEventAndReturnsId() throws Exception {
        UUID userId = UUID.randomUUID();
        when(calendarEventRepository.save(any())).thenAnswer(invocation -> {
            CalendarEvent saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            return saved;
        });

        mockMvc.perform(post("/api/calendar")
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"귀국비용보험 신청","date":"2026-09-30"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.calendar_event_id").exists());
    }

    @Test
    void rejectsBlankTitle() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/calendar")
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","date":"2026-09-30"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void deletesOwnEvent() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        CalendarEvent event = new CalendarEvent(userId, "삭제할 일정", LocalDate.of(2026, 9, 30));
        when(calendarEventRepository.findByIdAndUserId(eventId, userId)).thenReturn(Optional.of(event));

        mockMvc.perform(delete("/api/calendar/" + eventId)
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(calendarEventRepository).delete(event);
    }

    @Test
    void returnsNotFoundWhenDeletingSomeoneElsesEvent() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        when(calendarEventRepository.findByIdAndUserId(eventId, userId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/calendar/" + eventId)
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void returnsValidationErrorForMalformedEventId() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/calendar/not-a-uuid")
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/calendar"))
                .andExpect(status().isUnauthorized());
    }
}

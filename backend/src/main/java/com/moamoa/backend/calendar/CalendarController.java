package com.moamoa.backend.calendar;

import com.moamoa.backend.calendar.dto.CalendarEventCreatedResponse;
import com.moamoa.backend.calendar.dto.CalendarEventRequest;
import com.moamoa.backend.calendar.dto.CalendarEventResponse;
import com.moamoa.backend.calendar.dto.CalendarEventsResponse;
import com.moamoa.backend.common.ApiException;
import com.moamoa.backend.common.ApiResponse;
import com.moamoa.backend.common.ErrorCode;
import com.moamoa.backend.common.JwtUsers;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarEventRepository calendarEventRepository;

    public CalendarController(CalendarEventRepository calendarEventRepository) {
        this.calendarEventRepository = calendarEventRepository;
    }

    @GetMapping
    public ApiResponse<CalendarEventsResponse> getCalendarEvents(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = JwtUsers.requireUserId(jwt);
        List<CalendarEventResponse> events = calendarEventRepository.findByUserIdOrderByDateAsc(userId).stream()
                .map(event -> new CalendarEventResponse(event.getId().toString(), event.getTitle(), event.getDate()))
                .toList();
        return ApiResponse.success(new CalendarEventsResponse(events));
    }

    @PostMapping
    public ApiResponse<CalendarEventCreatedResponse> createCalendarEvent(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CalendarEventRequest request
    ) {
        UUID userId = JwtUsers.requireUserId(jwt);
        CalendarEvent saved = calendarEventRepository.save(new CalendarEvent(userId, request.title(), request.date()));
        return ApiResponse.success(new CalendarEventCreatedResponse(saved.getId().toString()));
    }

    @DeleteMapping("/{eventId}")
    public ApiResponse<Void> deleteCalendarEvent(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID eventId) {
        UUID userId = JwtUsers.requireUserId(jwt);
        CalendarEvent event = calendarEventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."));
        calendarEventRepository.delete(event);
        return ApiResponse.success(null);
    }
}

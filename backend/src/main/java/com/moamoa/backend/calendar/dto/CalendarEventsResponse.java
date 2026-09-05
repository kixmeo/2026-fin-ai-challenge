package com.moamoa.backend.calendar.dto;

import java.util.List;

public record CalendarEventsResponse(List<CalendarEventResponse> events) {
}

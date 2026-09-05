package com.moamoa.backend.calendar.dto;

import java.time.LocalDate;

public record CalendarEventResponse(String id, String title, LocalDate date) {
}

package com.moamoa.backend.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CalendarEventRequest(
        @NotBlank(message = "title은 필수입니다.") @Size(max = 255, message = "title은 255자를 넘을 수 없습니다.") String title,
        @NotNull(message = "date는 필수입니다.") LocalDate date
) {
}

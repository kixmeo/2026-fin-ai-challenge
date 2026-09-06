package com.moamoa.backend.calendar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID> {

    List<CalendarEvent> findByUserIdOrderByDateAsc(UUID userId);

    Optional<CalendarEvent> findByIdAndUserId(UUID id, UUID userId);
}

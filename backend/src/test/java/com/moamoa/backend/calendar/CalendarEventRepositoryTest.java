package com.moamoa.backend.calendar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// db/migration-postgresql은 Postgres 전용 문법이라 H2 테스트 DB에서는 제외함
@DataJpaTest
@TestPropertySource(properties = "spring.flyway.locations=classpath:db/migration")
class CalendarEventRepositoryTest {

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Test
    void findsOnlyEventsForGivenUser() {
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();
        calendarEventRepository.save(new CalendarEvent(userA, "귀국비용보험 신청", LocalDate.of(2026, 9, 30)));
        calendarEventRepository.save(new CalendarEvent(userB, "다른 사람 일정", LocalDate.of(2026, 10, 1)));

        List<CalendarEvent> events = calendarEventRepository.findByUserIdOrderByDateAsc(userA);

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTitle()).isEqualTo("귀국비용보험 신청");
    }

    @Test
    void findByIdAndUserIdReturnsEmptyForDifferentOwner() {
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        CalendarEvent saved = calendarEventRepository.save(new CalendarEvent(owner, "신청 마감", LocalDate.of(2026, 9, 30)));

        assertThat(calendarEventRepository.findByIdAndUserId(saved.getId(), other)).isEmpty();
        assertThat(calendarEventRepository.findByIdAndUserId(saved.getId(), owner)).isPresent();
    }
}

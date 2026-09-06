package com.moamoa.backend.chat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// db/migration-postgresql은 Postgres 전용 문법이라 H2 테스트 DB에서는 제외함
@DataJpaTest
@TestPropertySource(properties = "spring.flyway.locations=classpath:db/migration")
class ChatSessionRepositoryTest {

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Test
    void savesAndFindsSessionWithAccumulatedSlots() {
        UUID userId = UUID.randomUUID();
        ChatSession session = new ChatSession("chat_abc123", userId, null, null);
        session.applyExtracted(2500000L, null);
        chatSessionRepository.save(session);

        Optional<ChatSession> found = chatSessionRepository.findById("chat_abc123");

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId);
        assertThat(found.get().getIncome()).isEqualTo(2500000L);
        assertThat(found.get().getWorkPeriod()).isNull();
    }
}

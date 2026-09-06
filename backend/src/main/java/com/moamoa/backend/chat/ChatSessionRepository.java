package com.moamoa.backend.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {

    // user_id에 유니크 제약(V13)이 있어 최대 1건만 존재함이 스키마 레벨에서 보장됨 -
    // 혹시라도 깨지면(예: 제약 추가 전 레거시 데이터) Spring Data가 IncorrectResultSizeDataAccessException을 던짐
    Optional<ChatSession> findByUserId(UUID userId);
}

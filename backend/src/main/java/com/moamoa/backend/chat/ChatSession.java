package com.moamoa.backend.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "chat_sessions")
public class ChatSession {

    @Id
    @Column(name = "session_id", length = 64)
    private String sessionId;

    // 사용자당 진행 중인 세션은 최대 1개(V13의 유니크 제약으로 스키마 레벨에서도 강제됨)
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    // AI 서버가 무상태라 턴이 진행될수록 여기 누적됨 - 세션 생성 시점엔 둘 다 비어있음(null)
    @Column
    private Long income;

    @Column(name = "work_period")
    private Integer workPeriod;

    protected ChatSession() {
    }

    public ChatSession(String sessionId, UUID userId, Long income, Integer workPeriod) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId must not be blank");
        }
        this.sessionId = sessionId;
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.income = income;
        this.workPeriod = workPeriod;
    }

    // 채팅 매 턴마다 슬롯이 하나씩만 채워질 수 있으므로, 값이 있는 필드만 갱신함
    public void applyExtracted(Long income, Integer workPeriod) {
        if (income != null) {
            this.income = income;
        }
        if (workPeriod != null) {
            this.workPeriod = workPeriod;
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Long getIncome() {
        return income;
    }

    public Integer getWorkPeriod() {
        return workPeriod;
    }
}

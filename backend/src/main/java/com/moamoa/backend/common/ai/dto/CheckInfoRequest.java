package com.moamoa.backend.common.ai.dto;

// AI 서버는 프로필 필드를 루트에 바로가 아니라 user_profile로 감싸서 받음
public record CheckInfoRequest(AiUserProfile userProfile) {
}

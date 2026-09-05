package com.moamoa.backend.auth.dto;

public record AuthMeResponse(String userId, String email, boolean basicProfileComplete) {
}

package com.moamoa.backend.common;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Objects;
import java.util.UUID;

// Supabase JWT의 sub 클레임은 auth.users.id(UUID)이므로 이 형식을 가정함 (Profile.userId 참고)
public final class JwtUsers {

    private JwtUsers() {
    }

    public static UUID requireUserId(Jwt jwt) {
        Objects.requireNonNull(jwt, "jwt must not be null");
        if (jwt.getSubject() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "인증 토큰의 사용자 식별자 형식이 올바르지 않습니다.");
        }
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "인증 토큰의 사용자 식별자 형식이 올바르지 않습니다.", e);
        }
    }
}

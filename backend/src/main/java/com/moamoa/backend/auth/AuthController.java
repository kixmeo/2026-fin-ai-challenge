package com.moamoa.backend.auth;

import com.moamoa.backend.auth.dto.AuthMeResponse;
import com.moamoa.backend.common.ApiResponse;
import com.moamoa.backend.common.JwtUsers;
import com.moamoa.backend.profile.ProfileRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ProfileRepository profileRepository;

    public AuthController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @GetMapping("/me")
    public ApiResponse<AuthMeResponse> me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = JwtUsers.requireUserId(jwt);
        // email 클레임은 없을 수 있음(이메일 없는 인증 방식 등) - 그 경우 null을 그대로 응답에 실어보냄
        String email = jwt.getClaimAsString("email");
        boolean basicProfileComplete = profileRepository.existsById(userId);
        return ApiResponse.success(new AuthMeResponse(userId.toString(), email, basicProfileComplete));
    }
}

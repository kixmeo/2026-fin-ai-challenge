package com.moamoa.backend.profile;

import com.moamoa.backend.common.ApiResponse;
import com.moamoa.backend.common.JwtUsers;
import com.moamoa.backend.profile.dto.BasicProfileRequest;
import com.moamoa.backend.profile.dto.BasicProfileResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileRepository profileRepository;

    public ProfileController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @PostMapping("/basic")
    public ApiResponse<BasicProfileResponse> saveBasicProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody BasicProfileRequest request
    ) {
        UUID userId = JwtUsers.requireUserId(jwt);
        // 기존 프로필이 있으면 그 위에 갱신 - 새로 만들어 save()하면 merge()가 income/workPeriod까지 null로 덮어씀
        Profile profile = profileRepository.findById(userId)
                .map(existing -> {
                    existing.updateBasicInfo(request.visaType(), request.name(), request.residenceRegion());
                    return existing;
                })
                .orElseGet(() -> new Profile(userId, request.visaType(), request.name(), request.residenceRegion()));
        profileRepository.save(profile);
        return ApiResponse.success(new BasicProfileResponse(true));
    }
}

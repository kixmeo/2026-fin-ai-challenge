package com.moamoa.backend.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BasicProfileRequest(
        @NotBlank(message = "visa_type은 필수입니다.") @Size(max = 255, message = "visa_type은 255자를 넘을 수 없습니다.") String visaType,
        @NotBlank(message = "name은 필수입니다.") @Size(max = 255, message = "name은 255자를 넘을 수 없습니다.") String name,
        @NotBlank(message = "residence_region은 필수입니다.") @Size(max = 255, message = "residence_region은 255자를 넘을 수 없습니다.") String residenceRegion
) {
}

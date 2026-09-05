package com.moamoa.backend.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "profiles")
public class Profile {

    // Supabase auth.users.id(JWT의 sub 클레임)와 동일한 값 - FK 제약은 없고 애플리케이션 레벨에서만 보장됨
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "visa_type", nullable = false, length = 255)
    private String visaType;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "residence_region", nullable = false, length = 255)
    private String residenceRegion;

    protected Profile() {
    }

    public Profile(UUID userId, String visaType, String name, String residenceRegion) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.visaType = requireNonBlank(visaType, "visaType");
        this.name = requireNonBlank(name, "name");
        this.residenceRegion = requireNonBlank(residenceRegion, "residenceRegion");
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getVisaType() {
        return visaType;
    }

    public String getName() {
        return name;
    }

    public String getResidenceRegion() {
        return residenceRegion;
    }
}

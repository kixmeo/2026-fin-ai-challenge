package com.moamoa.backend.profile;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileTest {

    @Test
    void rejectsNullUserId() {
        assertThatThrownBy(() -> new Profile(null, "E-9", "Nguyen Van A", "안산시"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsBlankVisaType() {
        assertThatThrownBy(() -> new Profile(UUID.randomUUID(), " ", "Nguyen Van A", "안산시"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> new Profile(UUID.randomUUID(), "E-9", "", "안산시"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankResidenceRegion() {
        assertThatThrownBy(() -> new Profile(UUID.randomUUID(), "E-9", "Nguyen Van A", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void applyExtractedInfoOnlyUpdatesFieldsThatAreProvided() {
        Profile profile = new Profile(UUID.randomUUID(), "E-9", "Nguyen Van A", "안산시");

        profile.applyExtractedInfo(2500000L, null);
        assertThat(profile.getIncome()).isEqualTo(2500000L);
        assertThat(profile.getWorkPeriod()).isNull();

        profile.applyExtractedInfo(null, 14);
        assertThat(profile.getIncome()).isEqualTo(2500000L);
        assertThat(profile.getWorkPeriod()).isEqualTo(14);
    }
}

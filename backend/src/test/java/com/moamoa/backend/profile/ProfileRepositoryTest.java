package com.moamoa.backend.profile;

import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// db/migration-postgresql은 RLS 등 Postgres 전용 문법이라 H2 테스트 DB에서는 제외함
@DataJpaTest
@TestPropertySource(properties = "spring.flyway.locations=classpath:db/migration")
class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Test
    void savesAndFindsProfileByUserId() {
        UUID userId = UUID.randomUUID();
        profileRepository.save(new Profile(userId, "E-9", "Nguyen Van A", "안산시"));

        assertThat(profileRepository.existsById(userId)).isTrue();
        assertThat(profileRepository.findById(userId)).isPresent();
    }

    @Test
    void existsByIdIsFalseForUnknownUser() {
        assertThat(profileRepository.existsById(UUID.randomUUID())).isFalse();
    }

    @Test
    void savingAgainForSameUserIdUpdatesInPlaceRatherThanDuplicating() {
        UUID userId = UUID.randomUUID();
        profileRepository.save(new Profile(userId, "E-9", "Nguyen Van A", "안산시"));

        profileRepository.save(new Profile(userId, "H-2", "Nguyen Van B", "화성시"));

        assertThat(profileRepository.count()).isEqualTo(1);
        Profile updated = profileRepository.findById(userId).orElseThrow();
        assertThat(updated.getVisaType()).isEqualTo("H-2");
        assertThat(updated.getName()).isEqualTo("Nguyen Van B");
        assertThat(updated.getResidenceRegion()).isEqualTo("화성시");
    }

    @Test
    void updatingBasicInfoInPlacePreservesIncomeAndWorkPeriod() {
        UUID userId = UUID.randomUUID();
        Profile profile = new Profile(userId, "E-9", "Nguyen Van A", "안산시");
        profile.applyExtractedInfo(3000000L, 14);
        profileRepository.save(profile);

        Profile loaded = profileRepository.findById(userId).orElseThrow();
        loaded.updateBasicInfo("H-2", "Nguyen Van B", "화성시");
        profileRepository.save(loaded);

        Profile updated = profileRepository.findById(userId).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Nguyen Van B");
        assertThat(updated.getIncome()).isEqualTo(3000000L);
        assertThat(updated.getWorkPeriod()).isEqualTo(14);
    }
}

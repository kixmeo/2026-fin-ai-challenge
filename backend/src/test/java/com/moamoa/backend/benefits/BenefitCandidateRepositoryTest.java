package com.moamoa.backend.benefits;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// db/migration-postgresql은 Postgres 전용 문법이라 H2 테스트 DB에서는 제외함
@DataJpaTest
@TestPropertySource(properties = "spring.flyway.locations=classpath:db/migration")
class BenefitCandidateRepositoryTest {

    @Autowired
    private BenefitCandidateRepository benefitCandidateRepository;

    @Test
    void seedDataIsLoadedByMigration() {
        List<BenefitCandidate> candidates = benefitCandidateRepository.findAll();

        assertThat(candidates).hasSize(3);
        assertThat(candidates).extracting(BenefitCandidate::getBenefitId)
                .containsExactlyInAnyOrder("b_017", "b_021", "b_009");
    }
}

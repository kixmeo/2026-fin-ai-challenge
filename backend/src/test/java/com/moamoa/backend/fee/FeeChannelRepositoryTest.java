package com.moamoa.backend.fee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// db/migration-postgresql은 Postgres 전용 문법이라 H2 테스트 DB에서는 제외함
@DataJpaTest
@TestPropertySource(properties = "spring.flyway.locations=classpath:db/migration")
class FeeChannelRepositoryTest {

    @Autowired
    private FeeChannelRepository feeChannelRepository;

    @Autowired
    private FeeTierRepository feeTierRepository;

    @Test
    void seedDataLoadsFourInternetChannels() {
        List<FeeChannel> channels = feeChannelRepository.findAll();

        assertThat(channels).hasSize(4);
        assertThat(channels).extracting(FeeChannel::getBankName)
                .containsExactlyInAnyOrder("신한은행", "우리은행", "하나은행", "KB국민은행");
        assertThat(channels).extracting(FeeChannel::getChannelType).containsOnly("인터넷");
    }

    @Test
    void tiersAreOrderedAscendingWithNullLast() {
        FeeChannel shinhanInternet = feeChannelRepository.findAll().stream()
                .filter(c -> c.getBankName().equals("신한은행"))
                .findFirst().orElseThrow();

        List<FeeTier> tiers = feeTierRepository.findByFeeChannelIdInOrderByMaxUsdAmountAscNullsLast(List.of(shinhanInternet.getId()));

        assertThat(tiers).hasSize(5);
        assertThat(tiers.get(0).getMaxUsdAmount()).isEqualTo(500);
        assertThat(tiers.get(0).getFee()).isEqualTo(2500);
        assertThat(tiers.get(4).getMaxUsdAmount()).isNull();
        assertThat(tiers.get(4).getFee()).isEqualTo(12500);
    }
}

package com.moamoa.backend.fee;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class FeeTierTest {

    @Test
    void rejectsNullFeeChannelId() {
        assertThatThrownBy(() -> new FeeTier(null, 500, 5000))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNegativeMaxUsdAmount() {
        assertThatThrownBy(() -> new FeeTier(UUID.randomUUID(), -1, 5000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void allowsNullMaxUsdAmountAsUnboundedTier() {
        assertThatNoException().isThrownBy(() -> new FeeTier(UUID.randomUUID(), null, 10000));
    }

    @Test
    void rejectsNegativeFee() {
        assertThatThrownBy(() -> new FeeTier(UUID.randomUUID(), 500, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

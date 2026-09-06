package com.moamoa.backend.fee;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeeChannelTest {

    @Test
    void rejectsBlankBankName() {
        assertThatThrownBy(() -> new FeeChannel(" ", "인터넷", 8000, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankChannelType() {
        assertThatThrownBy(() -> new FeeChannel("신한은행", "", 8000, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeWireFee() {
        assertThatThrownBy(() -> new FeeChannel("신한은행", "인터넷", -1, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeEtaHours() {
        assertThatThrownBy(() -> new FeeChannel("신한은행", "인터넷", 8000, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

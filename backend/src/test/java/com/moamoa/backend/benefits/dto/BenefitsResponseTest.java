package com.moamoa.backend.benefits.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BenefitsResponseTest {

    @Test
    void rejectsNeedsMoreInfoWithoutSessionId() {
        assertThatThrownBy(() -> new BenefitsResponse(true, null, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsSessionIdWhenInfoIsSufficient() {
        assertThatThrownBy(() -> new BenefitsResponse(false, "chat_abc123", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

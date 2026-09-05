package com.moamoa.backend.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiErrorTest {

    @Test
    void rejectsNullCode() {
        assertThatThrownBy(() -> new ApiError(null, "message"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankCode() {
        assertThatThrownBy(() -> new ApiError(" ", "message"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankMessage() {
        assertThatThrownBy(() -> new ApiError("CODE", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

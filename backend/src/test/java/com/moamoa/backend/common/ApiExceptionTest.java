package com.moamoa.backend.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiExceptionTest {

    @Test
    void rejectsNullErrorCodeInSingleArgConstructor() {
        assertThatThrownBy(() -> new ApiException(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullErrorCodeInTwoArgConstructor() {
        assertThatThrownBy(() -> new ApiException(null, "message"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsBlankMessage() {
        assertThatThrownBy(() -> new ApiException(ErrorCode.UNAUTHORIZED, " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chainsCauseInThreeArgConstructor() {
        RuntimeException cause = new RuntimeException("original");
        ApiException ex = new ApiException(ErrorCode.UNAUTHORIZED, "message", cause);

        org.assertj.core.api.Assertions.assertThat(ex.getCause()).isSameAs(cause);
    }
}

package com.moamoa.backend.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiResponseTest {

    @Test
    void successFactoryProducesSuccessTrueWithNoError() {
        ApiResponse<String> response = ApiResponse.success("hello");

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isEqualTo("hello");
        assertThat(response.error()).isNull();
    }

    @Test
    void failureFactoryProducesSuccessFalseWithNoData() {
        ApiError error = new ApiError("VALIDATION_ERROR", "잘못된 요청입니다.");
        ApiResponse<Void> response = ApiResponse.failure(error);

        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.error()).isEqualTo(error);
    }

    @Test
    void rejectsSuccessTrueWithAnError() {
        assertThatThrownBy(() -> new ApiResponse<>(true, "data", new ApiError("CODE", "message")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsFailureWithData() {
        assertThatThrownBy(() -> new ApiResponse<>(false, "data", new ApiError("CODE", "message")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsFailureWithoutError() {
        assertThatThrownBy(() -> new ApiResponse<>(false, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

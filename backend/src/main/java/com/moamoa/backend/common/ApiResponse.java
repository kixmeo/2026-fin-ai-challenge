package com.moamoa.backend.common;

public record ApiResponse<T>(boolean success, T data, ApiError error) {

    public ApiResponse {
        if (success && error != null) {
            throw new IllegalArgumentException("success response must not carry an error");
        }
        if (!success && data != null) {
            throw new IllegalArgumentException("failure response must not carry data");
        }
        if (!success && error == null) {
            throw new IllegalArgumentException("failure response must carry an error");
        }
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> failure(ApiError error) {
        return new ApiResponse<>(false, null, error);
    }

    // RestAuthenticationEntryPoint/RestAccessDeniedHandler/GlobalExceptionHandler가 각자 만들던
    // "ErrorCode 기본 메시지로 실패 응답 만들기"를 한 곳으로 모음
    public static <T> ApiResponse<T> failure(ErrorCode errorCode) {
        return failure(new ApiError(errorCode.name(), errorCode.getDefaultMessage()));
    }
}

package com.moamoa.backend.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Stream;

// ResponseEntityExceptionHandler를 상속: Spring MVC가 자체적으로 던지는 프레임워크 예외(잘못된 JSON,
// 존재하지 않는 라우트, 지원하지 않는 메서드/미디어타입 등)도 우리 ApiResponse 포맷으로 통일하기 위함.
// 이 handler를 상속하지 않으면 그런 예외들은 Spring 기본 ProblemDetail 포맷으로 응답되고,
// 아래 handleUnexpectedException(범용 catch-all)은 그 예외들이 여기 도달하기 전에
// ResponseEntityExceptionHandler가 먼저 가로채서 createResponseEntity로 흘려보냄.
// (참고: SecurityConfig에 @EnableMethodSecurity가 켜져 있어야 @PreAuthorize 등이 실제로 동작하고,
// 그때 던져지는 AccessDeniedException이 아래 handleAccessDeniedException으로 옴)
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        if (errorCode.getHttpStatus().is5xxServerError()) {
            log.error("ApiException: code={}", errorCode.name(), ex);
        } else {
            log.warn("ApiException: code={}", errorCode.name(), ex);
        }
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(new ApiError(errorCode.name(), ex.getMessage())));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied", ex);
        return ResponseEntity.status(ErrorCode.FORBIDDEN.getHttpStatus())
                .body(ApiResponse.failure(ErrorCode.FORBIDDEN));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ApiResponse.failure(ErrorCode.INTERNAL_ERROR));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = Stream.concat(
                        ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage),
                        ex.getBindingResult().getGlobalErrors().stream().map(ObjectError::getDefaultMessage))
                .reduce((a, b) -> a + ", " + b)
                .orElse(ErrorCode.VALIDATION_ERROR.getDefaultMessage());
        ApiResponse<Void> apiResponse = ApiResponse.failure(new ApiError(ErrorCode.VALIDATION_ERROR.name(), message));
        return createResponseEntity(apiResponse, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        log.warn("Handled MVC exception, status={}", statusCode, ex);
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(
            Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        // handleMethodArgumentNotValid처럼 이미 ApiResponse를 만들어 넘긴 경우는 그대로 쓰고,
        // 그 외(우리가 개별적으로 오버라이드하지 않은 프레임워크 예외)는 상태코드만으로 기본 바디를 생성함
        Object responseBody = body instanceof ApiResponse<?> ? body : ApiResponse.failure(errorCodeFor(statusCode));
        return new ResponseEntity<>(responseBody, headers, statusCode);
    }

    private ErrorCode errorCodeFor(HttpStatusCode statusCode) {
        if (statusCode.value() == HttpStatus.NOT_FOUND.value()) {
            return ErrorCode.NOT_FOUND;
        }
        if (statusCode.value() == HttpStatus.METHOD_NOT_ALLOWED.value()) {
            return ErrorCode.METHOD_NOT_ALLOWED;
        }
        if (statusCode.value() == HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()) {
            return ErrorCode.UNSUPPORTED_MEDIA_TYPE;
        }
        if (statusCode.is5xxServerError()) {
            return ErrorCode.INTERNAL_ERROR;
        }
        return ErrorCode.VALIDATION_ERROR;
    }
}

package com.moamoa.backend.config;

import tools.jackson.databind.ObjectMapper;
import com.moamoa.backend.common.ApiResponse;
import com.moamoa.backend.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 지금은 anyRequest().authenticated()만 있어서 아직 403이 발생하지 않지만,
// 나중에 권한(role) 기반 제약이 추가되면 필요해지므로 미리 대비해둠
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(RestAccessDeniedHandler.class);

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        log.warn("Access denied: path={}", request.getRequestURI(), accessDeniedException);
        response.setStatus(ErrorCode.FORBIDDEN.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.failure(ErrorCode.FORBIDDEN);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}

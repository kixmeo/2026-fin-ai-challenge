package com.moamoa.backend.auth;

import com.moamoa.backend.config.RestAccessDeniedHandler;
import com.moamoa.backend.config.RestAuthenticationEntryPoint;
import com.moamoa.backend.config.SecurityConfig;
import com.moamoa.backend.profile.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileRepository profileRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void returnsBasicProfileCompleteTrueWhenProfileExists() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileRepository.existsById(userId)).thenReturn(true);

        mockMvc.perform(get("/api/auth/me")
                        .with(jwt().jwt(j -> j.subject(userId.toString()).claim("email", "user@gmail.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user_id").value(userId.toString()))
                .andExpect(jsonPath("$.data.email").value("user@gmail.com"))
                .andExpect(jsonPath("$.data.basic_profile_complete").value(true));
    }

    @Test
    void returnsBasicProfileCompleteFalseWhenProfileMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileRepository.existsById(userId)).thenReturn(false);

        mockMvc.perform(get("/api/auth/me")
                        .with(jwt().jwt(j -> j.subject(userId.toString()).claim("email", "user@gmail.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.basic_profile_complete").value(false));
    }

    @Test
    void returnsUnauthorizedInApiResponseShapeWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void returnsUnauthorizedInApiResponseShapeForInvalidOrExpiredToken() throws Exception {
        when(jwtDecoder.decode(any())).thenThrow(new BadJwtException("expired"));

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void returnsUnauthorizedWhenJwtSubjectIsNotAValidUuid() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .with(jwt().jwt(j -> j.subject("not-a-uuid"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void returnsInternalErrorEnvelopeWhenRepositoryThrows() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileRepository.existsById(any())).thenThrow(new RuntimeException("db down"));

        mockMvc.perform(get("/api/auth/me")
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"));
    }

    @Test
    void returnsNullEmailWhenClaimIsAbsent() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileRepository.existsById(userId)).thenReturn(false);

        mockMvc.perform(get("/api/auth/me")
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void returnsMethodNotAllowedEnvelopeForWrongHttpMethod() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/auth/me")
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void returnsNotFoundEnvelopeForUnknownRoute() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/auth/no-such-route")
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }
}

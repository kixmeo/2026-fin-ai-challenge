package com.moamoa.backend.profile;

import com.moamoa.backend.config.RestAccessDeniedHandler;
import com.moamoa.backend.config.RestAuthenticationEntryPoint;
import com.moamoa.backend.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileRepository profileRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void savesBasicProfileWithUserIdFromJwtNotFromRequestBody() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/profile/basic")
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"visa_type":"E-9","name":"Nguyen Van A","residence_region":"안산시"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.basic_profile_complete").value(true));

        ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).save(captor.capture());
        Profile saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getVisaType()).isEqualTo("E-9");
        assertThat(saved.getName()).isEqualTo("Nguyen Van A");
        assertThat(saved.getResidenceRegion()).isEqualTo("안산시");
    }

    @Test
    void rejectsBlankFieldsWithValidationError() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/profile/basic")
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"visa_type":"","name":"Nguyen Van A","residence_region":"안산시"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void reportsAllBlankFieldsNotJustTheFirst() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/profile/basic")
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"visa_type":"","name":"","residence_region":"안산시"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("visa_type")))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("name")));
    }

    @Test
    void rejectsMalformedJsonBodyAsBadRequestNotServerError() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/profile/basic")
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(post("/api/profile/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"visa_type":"E-9","name":"Nguyen Van A","residence_region":"안산시"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void allowsCorsPreflightFromFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/profile/basic")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void doesNotEchoAllowOriginForDisallowedOrigin() throws Exception {
        mockMvc.perform(options("/api/profile/basic")
                        .header("Origin", "http://evil.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void rejectsUnsupportedContentTypeAsBadRequestNotServerError() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/profile/basic")
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text body"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }
}

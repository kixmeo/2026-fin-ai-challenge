package com.moamoa.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// @PreAuthorize 등 메서드 레벨 권한 체크를 실제로 동작시키려면 필요함 (지금은 아무 데도 안 쓰지만,
// 없으면 나중에 @PreAuthorize를 붙여도 아무 효과 없이 조용히 통과되는(fail-open) 함정이 생김)
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http, RestAuthenticationEntryPoint entryPoint, RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
                // 세션/쿠키 없이 Authorization 헤더의 JWT만 사용하므로 CSRF 방어 대상이 아님
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 로그인/회원가입 엔드포인트가 없는 이유: Google 로그인은 프론트가 Supabase SDK로 직접 처리하고,
                // 이 백엔드는 매 요청마다 그 결과로 발급된 JWT만 검증함
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                // 토큰이 아예 없는 경우는 authenticationEntryPoint 위 설정만으로 처리되지만,
                // 토큰은 있는데 유효하지 않은 경우(만료/서명오류 등)는 리소스 서버가 자체 entry point를 쓰므로 여기도 지정해야 함
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(entryPoint)
                        .jwt(jwt -> {}));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // 쿠키/세션을 안 쓰고(allowCredentials=false) Bearer 토큰만 쓰므로 헤더는 다 허용해도 안전함 -
        // 특정 헤더만 허용하면 나중에 프론트가 새 헤더(예: 트레이싱 헤더)를 추가할 때마다 preflight가 막힘
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}

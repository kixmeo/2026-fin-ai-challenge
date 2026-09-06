package com.moamoa.backend.common.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class AiServerConfig {

    @Bean
    public RestClient aiServerRestClient(JsonMapper jsonMapper, @Value("${ai.server.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3_000);
        // RAG 기반 설명 생성(explain)은 느릴 수 있어서 넉넉하게 잡음
        requestFactory.setReadTimeout(20_000);

        // 앱 전역 JsonMapper 빈(spring.jackson.property-naming-strategy=SNAKE_CASE 적용된 것)을 그대로 재사용해서
        // 우리 API 응답과 동일한 snake_case 규칙으로 AI 서버와 주고받게 함
        JacksonJsonHttpMessageConverter jsonConverter = new JacksonJsonHttpMessageConverter(jsonMapper);

        return RestClient.builder()
                .configureMessageConverters(mc -> mc.disableDefaults().withJsonConverter(jsonConverter))
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}

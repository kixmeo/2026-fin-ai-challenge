package com.moamoa.backend.exchange;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ExchangeRateConfig {

    // exchangerate-api.com의 무료 공개 엔드포인트(키 불필요) - 응답 필드명이 이미 소문자 그대로라
    // AI 서버 호출용 RestClient(AiServerConfig)처럼 snake_case 컨버터를 따로 맞출 필요가 없음
    @Bean
    public RestClient exchangeRateRestClient(RestClient.Builder builder) {
        return builder.baseUrl("https://api.exchangerate-api.com").build();
    }
}

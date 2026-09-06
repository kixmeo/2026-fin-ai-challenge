package com.moamoa.backend.exchange;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ExchangeRateConfig {

    // exchangerate-api.com의 무료 공개 엔드포인트(키 불필요) - 응답 필드명이 이미 소문자 그대로라
    // AI 서버 호출용 RestClient(AiServerConfig)처럼 snake_case 컨버터를 따로 맞출 필요가 없음.
    // AiServerConfig와 동일하게 RestClient.Builder 빈이 아니라 정적 팩토리로 직접 생성함 - 이 프로젝트엔
    // 그 빈이 자동구성되어 있지 않음(주입받으면 기동 자체가 실패함)
    @Bean
    public RestClient exchangeRateRestClient() {
        return RestClient.builder().baseUrl("https://api.exchangerate-api.com").build();
    }
}

package com.moamoa.backend.exchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

// exchangerate-api.com(무료, 키 불필요)에서 KRW 기준 환율을 하루 한 번 받아와 그날치 스냅샷을 쌓음.
// AI 서버(/ai/exchange/insight)가 요구하는 90일 시계열은 이 누적치로 채워짐(ExchangeController 참고).
// ponytail: VND는 이 소스로만 커버되고 과거 이력 백필 API가 없어 배포 시점부터 하루씩 쌓이는 중 -
// 90일치가 다 찰 때까지는 표본이 적음. 유료 이력 API를 쓰면 처음부터 해결 가능.
@Component
public class ExchangeRateRefreshJob {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateRefreshJob.class);
    private static final List<String> SUPPORTED_CURRENCIES = List.of("USD", "PHP", "VND", "THB");
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final RestClient restClient;
    private final ExchangeRateSnapshotRepository snapshotRepository;

    public ExchangeRateRefreshJob(RestClient exchangeRateRestClient, ExchangeRateSnapshotRepository snapshotRepository) {
        this.restClient = exchangeRateRestClient;
        this.snapshotRepository = snapshotRepository;
    }

    // 배포 직후에도 바로 오늘치가 있어야 하므로 기동 시 1회 + 이후 매일 자정 5분(KST)에 실행
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void refreshToday() {
        try {
            LatestRatesResponse body = restClient.get()
                    .uri("/v4/latest/KRW")
                    .retrieve()
                    .body(LatestRatesResponse.class);
            if (body == null || body.rates() == null) {
                log.error("환율 API가 빈 응답을 반환했습니다.");
                return;
            }
            LocalDate today = LocalDate.now(KST);
            for (String currency : SUPPORTED_CURRENCIES) {
                Double perKrw = body.rates().get(currency);
                if (perKrw == null || perKrw == 0) {
                    log.warn("환율 API 응답에 {}가 없어 이번 회차는 건너뜁니다.", currency);
                    continue;
                }
                BigDecimal krwPerUnit = BigDecimal.ONE.divide(BigDecimal.valueOf(perKrw), 4, RoundingMode.HALF_UP);
                upsertIfAbsent(currency, today, krwPerUnit);
            }
        } catch (RestClientException e) {
            log.error("환율 갱신에 실패했습니다.", e);
        }
    }

    private void upsertIfAbsent(String currency, LocalDate date, BigDecimal rate) {
        ExchangeRateSnapshot.Id id = new ExchangeRateSnapshot.Id(currency, date);
        if (snapshotRepository.existsById(id)) {
            return;
        }
        snapshotRepository.save(new ExchangeRateSnapshot(currency, date, rate));
    }

    private record LatestRatesResponse(Map<String, Double> rates) {
    }
}

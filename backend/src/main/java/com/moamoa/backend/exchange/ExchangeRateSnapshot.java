package com.moamoa.backend.exchange;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

// AI 서버(/ai/exchange/insight)는 무상태라 매 호출마다 최신 환율 + 90일 시계열을 백엔드가
// 직접 담아 보내야 함 - 이 테이블이 그 시계열 캐시(ExchangeRateRefreshJob이 매일 하루치를 추가)
@Entity
@Table(name = "exchange_rate_snapshots")
@IdClass(ExchangeRateSnapshot.Id.class)
public class ExchangeRateSnapshot {

    @jakarta.persistence.Id
    @Column(length = 3)
    private String currency;

    @jakarta.persistence.Id
    @Column(name = "rate_date")
    private LocalDate rateDate;

    @Column(nullable = false)
    private BigDecimal rate;

    protected ExchangeRateSnapshot() {
    }

    public ExchangeRateSnapshot(String currency, LocalDate rateDate, BigDecimal rate) {
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
        this.rateDate = Objects.requireNonNull(rateDate, "rateDate must not be null");
        this.rate = Objects.requireNonNull(rate, "rate must not be null");
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getRateDate() {
        return rateDate;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public static class Id implements Serializable {
        private String currency;
        private LocalDate rateDate;

        public Id() {
        }

        public Id(String currency, LocalDate rateDate) {
            this.currency = currency;
            this.rateDate = rateDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Id id)) {
                return false;
            }
            return Objects.equals(currency, id.currency) && Objects.equals(rateDate, id.rateDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(currency, rateDate);
        }
    }
}

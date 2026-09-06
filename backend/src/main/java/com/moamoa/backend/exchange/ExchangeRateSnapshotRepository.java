package com.moamoa.backend.exchange;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExchangeRateSnapshotRepository extends JpaRepository<ExchangeRateSnapshot, ExchangeRateSnapshot.Id> {

    List<ExchangeRateSnapshot> findByCurrencyAndRateDateGreaterThanEqualOrderByRateDateAsc(String currency, LocalDate from);
}

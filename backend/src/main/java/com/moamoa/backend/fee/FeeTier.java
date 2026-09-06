package com.moamoa.backend.fee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

// 채널(FeeChannel)마다 "미화 환산액이 얼마 이하면 얼마"라는 구간이 여러 개 있음.
// maxUsdAmount가 null이면 "이 금액 초과" 구간(상한 없음)을 의미함.
@Entity
@Table(name = "fee_tiers")
public class FeeTier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "fee_channel_id", nullable = false)
    private UUID feeChannelId;

    @Column(name = "max_usd_amount")
    private Integer maxUsdAmount;

    @Column(nullable = false)
    private long fee;

    protected FeeTier() {
    }

    public FeeTier(UUID feeChannelId, Integer maxUsdAmount, long fee) {
        this.feeChannelId = Objects.requireNonNull(feeChannelId, "feeChannelId must not be null");
        if (maxUsdAmount != null && maxUsdAmount < 0) {
            throw new IllegalArgumentException("maxUsdAmount must not be negative");
        }
        if (fee < 0) {
            throw new IllegalArgumentException("fee must not be negative");
        }
        this.maxUsdAmount = maxUsdAmount;
        this.fee = fee;
    }

    public UUID getFeeChannelId() {
        return feeChannelId;
    }

    public Integer getMaxUsdAmount() {
        return maxUsdAmount;
    }

    public long getFee() {
        return fee;
    }
}

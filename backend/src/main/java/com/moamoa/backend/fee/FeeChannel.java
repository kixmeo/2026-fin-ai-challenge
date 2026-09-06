package com.moamoa.backend.fee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "fee_channels")
public class FeeChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bank_name", nullable = false, length = 255)
    private String bankName;

    // 창구(은행 방문) 또는 인터넷(온라인)
    @Column(name = "channel_type", nullable = false, length = 50)
    private String channelType;

    // 송금 건당 고정으로 붙는 전신료 (구간 수수료와 별개로 항상 더해짐)
    @Column(name = "wire_fee", nullable = false)
    private long wireFee;

    @Column(name = "eta_hours", nullable = false)
    private int etaHours;

    protected FeeChannel() {
    }

    public FeeChannel(String bankName, String channelType, long wireFee, int etaHours) {
        this.bankName = requireNonBlank(bankName, "bankName");
        this.channelType = requireNonBlank(channelType, "channelType");
        if (wireFee < 0) {
            throw new IllegalArgumentException("wireFee must not be negative");
        }
        if (etaHours < 0) {
            throw new IllegalArgumentException("etaHours must not be negative");
        }
        this.wireFee = wireFee;
        this.etaHours = etaHours;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public UUID getId() {
        return id;
    }

    public String getBankName() {
        return bankName;
    }

    public String getChannelType() {
        return channelType;
    }

    public long getWireFee() {
        return wireFee;
    }

    public int getEtaHours() {
        return etaHours;
    }
}

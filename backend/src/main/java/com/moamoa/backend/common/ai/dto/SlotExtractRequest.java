package com.moamoa.backend.common.ai.dto;

public record SlotExtractRequest(String message, Long knownIncome, Integer knownWorkPeriod) {
}

package com.moamoa.backend.common.ai.dto;

public record WageVerifyRequest(long baseWage, int workHoursPerWeek, int overtimeHours, long overtimePay) {
}

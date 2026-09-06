package com.moamoa.backend.common.ai.dto;

import java.util.List;

public record WageClassifyRequest(List<DeductionItem> deductions) {
}

package com.moamoa.backend.common.ai.dto;

import java.util.List;

public record WageClassifyResponse(List<DeductionFlag> deductionFlags) {
}

package com.moamoa.backend.wage.dto;

import com.moamoa.backend.common.ai.dto.DeductionFlag;
import com.moamoa.backend.common.ai.dto.MinimumWageCheck;
import com.moamoa.backend.common.ai.dto.OvertimeCheck;

import java.util.List;

public record WageCheckResponse(
        MinimumWageCheck minimumWageCheck,
        OvertimeCheck overtimeCheck,
        List<DeductionFlag> deductionFlags,
        String disclaimer
) {
}

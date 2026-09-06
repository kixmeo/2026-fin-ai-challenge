"""F4 (rule half) — minimum-wage and overtime-pay verification.

Monthly standard working hours follow the common 근로기준법 convention: weekly
hours plus one paid weekly-holiday day (주휴시간, calculated as hours/day for
a 5-day week), times 4.345 weeks/month — this is the same math that yields
the familiar "209시간" figure for a 40hr/week job (40 + 8) * 4.345 ≈ 209.

NOTE: the API spec's own example (base_wage=2,100,000 / 40hr week →
hourly_wage=13,125, i.e. monthly_hours=160) does not reconcile with this
formula (209h → ~10,048/hr) or with its own overtime example (실제 배율이
1.5x가 아니라 약 0.75x로 보임). Flagged as an open discrepancy — confirm the
intended formula with the team before treating either version as final.
"""

from __future__ import annotations

from app.config import settings
from app.schemas import MinimumWageCheck, OvertimeCheck, WageVerifyRequest, WageVerifyResponse

OVERTIME_MULTIPLIER = 1.5  # 근로기준법 제56조: 연장근로는 통상임금의 50% 가산
WEEKS_PER_MONTH = 4.345


def _monthly_standard_hours(work_hours_per_week: float) -> float:
    daily_hours = work_hours_per_week / 5
    weekly_hours_with_holiday = work_hours_per_week + daily_hours
    return weekly_hours_with_holiday * WEEKS_PER_MONTH


def verify_wage(req: WageVerifyRequest) -> WageVerifyResponse:
    monthly_hours = _monthly_standard_hours(req.work_hours_per_week)
    hourly_wage = req.base_wage / monthly_hours if monthly_hours else 0.0

    minimum_wage_check = MinimumWageCheck(
        pass_=hourly_wage >= settings.minimum_wage_2026,
        hourly_wage=round(hourly_wage, 1),
        minimum_wage=settings.minimum_wage_2026,
    )

    expected_overtime = req.overtime_hours * hourly_wage * OVERTIME_MULTIPLIER
    # small tolerance for rounding differences between payroll systems
    overtime_check = OvertimeCheck(
        pass_=req.overtime_pay >= expected_overtime - 1,
        expected=round(expected_overtime, 1),
        actual=req.overtime_pay,
    )

    return WageVerifyResponse(minimum_wage_check=minimum_wage_check, overtime_check=overtime_check)

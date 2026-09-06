"""F5 — percentile + volatility statistics (no AI/ML — PRD is explicit that
this is NOT a prediction model). Caching / API-fallback for the raw rate
history is the backend's job (see the "환율 캐시 유효?" node in the FigJam
flowchart) — this module only computes stats over whatever history it's given.
"""

from __future__ import annotations

import statistics
from datetime import datetime, timedelta

from app.schemas import ExchangeInsightRequest, ExchangeInsightResponse, RatePoint

# Coefficient-of-variation thresholds (%) over the last 30 days of closes.
VOLATILITY_LOW_MAX = 0.5
VOLATILITY_MEDIUM_MAX = 1.2


def _percentile_rank(current: float, history: list[float]) -> float:
    """% of days in the window whose rate was at or below the current rate —
    i.e. "current rate is higher than N% of recent days"."""
    if not history:
        return 50.0
    at_or_below = sum(1 for r in history if r <= current)
    return round(at_or_below / len(history) * 100, 1)


def _volatility(history: list[float]) -> tuple[float, str]:
    if len(history) < 2:
        return 0.0, "low"
    mean = statistics.mean(history)
    stdev = statistics.pstdev(history)
    coefficient_of_variation = (stdev / mean * 100) if mean else 0.0
    if coefficient_of_variation < VOLATILITY_LOW_MAX:
        level = "low"
    elif coefficient_of_variation < VOLATILITY_MEDIUM_MAX:
        level = "medium"
    else:
        level = "high"
    return round(coefficient_of_variation, 2), level


def _window(history: list[RatePoint], days: int, as_of: datetime) -> list[float]:
    cutoff = as_of - timedelta(days=days)
    return [p.rate for p in history if datetime.strptime(p.date, "%Y-%m-%d") >= cutoff]


def _build_message(percentile_30d: float, volatility_level: str) -> str:
    position = (
        "상위" if percentile_30d >= 50 else "하위"
    )
    position_pct = percentile_30d if percentile_30d >= 50 else 100 - percentile_30d

    volatility_phrase = {
        "low": "변동성은 평소보다 안정적인 편이에요.",
        "medium": "변동성은 평소와 비슷한 수준이에요.",
        "high": "변동성도 평소보다 큰 편이라 지금 급하게 결정하기보단 며칠 지켜보는 것도 방법이에요.",
    }[volatility_level]

    return f"최근 30일 중 {position} {round(position_pct)}%에 해당하는 환율이에요. {volatility_phrase}"


def compute_insight(req: ExchangeInsightRequest) -> ExchangeInsightResponse:
    sorted_history = sorted(req.rate_history_90d, key=lambda p: p.date)
    as_of = datetime.strptime(sorted_history[-1].date, "%Y-%m-%d") if sorted_history else datetime.now()

    rates_30d = _window(sorted_history, 30, as_of)
    rates_90d = _window(sorted_history, 90, as_of)

    percentile_30d = _percentile_rank(req.current_rate, rates_30d)
    percentile_90d = _percentile_rank(req.current_rate, rates_90d)
    volatility_score, volatility_level = _volatility(rates_30d)

    return ExchangeInsightResponse(
        currency=req.currency,
        current_rate=req.current_rate,
        percentile_30d=percentile_30d,
        percentile_90d=percentile_90d,
        volatility_level=volatility_level,
        volatility_score=volatility_score,
        message=_build_message(percentile_30d, volatility_level),
    )

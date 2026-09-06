"""F2 — rule-based eligibility filter + weighted scoring (PRD: not a learned
model — domain-judgment weights, flagged in the PRD's own risk table as
something that may need defending/re-tuning post-demo).

Weights are a first-pass guess (amount matters most, then how soon the
deadline is, then how easy the application is) — treat as a tunable
constant, not a locked design decision.
"""

from __future__ import annotations

from datetime import date, datetime

from app.schemas import BenefitCandidate, EligibilityRule, ScoredBenefit, UserProfile

AMOUNT_WEIGHT = 0.5
DEADLINE_WEIGHT = 0.3
DIFFICULTY_WEIGHT = 0.2

# Reference amount used to normalize the amount score to [0, 1].
AMOUNT_NORMALIZATION_REF = 1_000_000
# Deadlines further out than this many days stop adding urgency score.
DEADLINE_HORIZON_DAYS = 180


def _check_eligibility(profile: UserProfile, rule: EligibilityRule) -> tuple[bool, list[str]]:
    reasons: list[str] = []

    if rule.visa_type_in is not None:
        if profile.visa_type not in rule.visa_type_in:
            reasons.append(f"체류자격({profile.visa_type})이 대상({rule.visa_type_in})에 해당하지 않음")

    if rule.income_max is not None and profile.income is not None and profile.income > rule.income_max:
        reasons.append(f"소득({profile.income})이 상한({rule.income_max})을 초과함")

    if rule.income_min is not None and profile.income is not None and profile.income < rule.income_min:
        reasons.append(f"소득({profile.income})이 하한({rule.income_min})에 미달함")

    if (
        rule.work_period_min_months is not None
        and profile.work_period is not None
        and profile.work_period < rule.work_period_min_months
    ):
        reasons.append(f"근속기간({profile.work_period}개월)이 최소 조건({rule.work_period_min_months}개월)에 미달함")

    if (
        rule.work_period_max_months is not None
        and profile.work_period is not None
        and profile.work_period > rule.work_period_max_months
    ):
        reasons.append(f"근속기간({profile.work_period}개월)이 최대 조건({rule.work_period_max_months}개월)을 초과함")

    eligible = len(reasons) == 0
    if eligible:
        reasons.append("모든 자격 조건 충족")
    return eligible, reasons


def _amount_score(amount: int) -> float:
    return min(amount / AMOUNT_NORMALIZATION_REF, 1.0)


def _deadline_score(deadline_str: str, today: date) -> tuple[float, bool]:
    """Returns (score, is_expired). Sooner deadlines score higher — the point
    is to surface benefits the user is at risk of missing, not to bury them."""
    try:
        deadline = datetime.strptime(deadline_str, "%Y-%m-%d").date()
    except ValueError:
        return 0.0, False  # unparseable deadline — treat as no urgency signal, not fatal

    days_left = (deadline - today).days
    if days_left < 0:
        return 0.0, True
    score = max(0.0, 1.0 - days_left / DEADLINE_HORIZON_DAYS)
    return score, False


def _difficulty_score(required_docs_count: int) -> float:
    return 1.0 / (1.0 + max(required_docs_count, 0))


def score_benefits(
    profile: UserProfile,
    candidates: list[BenefitCandidate],
    today: date | None = None,
) -> list[ScoredBenefit]:
    today = today or date.today()
    results: list[ScoredBenefit] = []

    for candidate in candidates:
        eligible, reasons = _check_eligibility(profile, candidate.eligibility_rule)
        deadline_score, expired = _deadline_score(candidate.deadline, today)

        if expired:
            eligible = False
            reasons.append("신청 마감일이 지남")

        if not eligible:
            results.append(ScoredBenefit(benefit_id=candidate.benefit_id, eligible=False, score=0.0, reasons=reasons))
            continue

        score = (
            AMOUNT_WEIGHT * _amount_score(candidate.amount)
            + DEADLINE_WEIGHT * deadline_score
            + DIFFICULTY_WEIGHT * _difficulty_score(candidate.required_docs_count)
        )
        results.append(
            ScoredBenefit(benefit_id=candidate.benefit_id, eligible=True, score=round(score, 4), reasons=reasons)
        )

    results.sort(key=lambda r: r.score, reverse=True)
    return results

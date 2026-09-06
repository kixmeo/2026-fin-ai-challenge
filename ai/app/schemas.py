from __future__ import annotations

from typing import Literal, Optional

from pydantic import BaseModel, Field

# ---------------------------------------------------------------------------
# Shared
# ---------------------------------------------------------------------------


class UserProfile(BaseModel):
    """Fields the AI server cares about. visa_type is collected at signup
    (POST /api/profile/basic) and passed through here for eligibility rules;
    income/work_period are the two fields F1 collects via chat."""

    visa_type: Optional[str] = None
    income: Optional[int] = None  # monthly, KRW
    work_period: Optional[int] = None  # months


# ---------------------------------------------------------------------------
# POST /ai/benefits/check-info
# ---------------------------------------------------------------------------

REQUIRED_SCORING_FIELDS = ["income", "work_period"]


class CheckInfoRequest(BaseModel):
    user_profile: UserProfile


class CheckInfoResponse(BaseModel):
    needs_more_info: bool
    missing_fields: list[str]


# ---------------------------------------------------------------------------
# POST /ai/slot-extract
# ---------------------------------------------------------------------------


class ChatTurn(BaseModel):
    role: Literal["user", "assistant"]
    content: str


class SlotExtractRequest(BaseModel):
    message: str
    current_profile: UserProfile = Field(default_factory=UserProfile)
    conversation_history: list[ChatTurn] = Field(default_factory=list)
    reask_count: int = 0


class SlotExtractionLLMOutput(BaseModel):
    """What we ask Claude to produce. Kept separate from SlotExtractResponse
    because reask_count is server-computed, not something the model decides."""

    reply: str
    extracted_profile: UserProfile
    is_complete: bool


class SlotExtractResponse(BaseModel):
    reply: str
    extracted_profile: UserProfile
    is_complete: bool
    reask_count: int


# ---------------------------------------------------------------------------
# POST /ai/benefits/score
# ---------------------------------------------------------------------------


class EligibilityRule(BaseModel):
    """Sparse rule — an unset field means "no constraint on this axis"."""

    visa_type_in: Optional[list[str]] = None
    income_max: Optional[int] = None
    income_min: Optional[int] = None
    work_period_min_months: Optional[int] = None
    work_period_max_months: Optional[int] = None


class BenefitCandidate(BaseModel):
    benefit_id: str
    title: str
    amount: int
    deadline: str  # YYYY-MM-DD
    required_docs_count: int
    eligibility_rule: EligibilityRule = Field(default_factory=EligibilityRule)


class BenefitScoreRequest(BaseModel):
    user_profile: UserProfile
    benefit_candidates: list[BenefitCandidate]
    today: Optional[str] = None  # YYYY-MM-DD; defaults to server date, override in tests


class ScoredBenefit(BaseModel):
    benefit_id: str
    eligible: bool
    score: float
    reasons: list[str]  # why eligible/ineligible, for debugging / (Front) tooltips


class BenefitScoreResponse(BaseModel):
    scored_benefits: list[ScoredBenefit]


# ---------------------------------------------------------------------------
# POST /ai/benefits/explain
# ---------------------------------------------------------------------------


class SourceDocument(BaseModel):
    doc_id: str
    title: str
    content: str


class BenefitExplainRequest(BaseModel):
    question: str
    benefit_title: str
    source_documents: list[SourceDocument]


class ExplainLLMOutput(BaseModel):
    answer: str
    sources: list[str]  # doc_ids actually used to ground the answer
    grounded: bool  # false if the documents don't actually answer the question


class BenefitExplainResponse(BaseModel):
    answer: str
    sources: list[str]


# ---------------------------------------------------------------------------
# POST /ai/wage/verify
# ---------------------------------------------------------------------------


class WageVerifyRequest(BaseModel):
    base_wage: int
    work_hours_per_week: float
    overtime_hours: float = 0
    overtime_pay: int = 0


class MinimumWageCheck(BaseModel):
    pass_: bool = Field(alias="pass")
    hourly_wage: float
    minimum_wage: int

    model_config = {"populate_by_name": True}


class OvertimeCheck(BaseModel):
    pass_: bool = Field(alias="pass")
    expected: float
    actual: float

    model_config = {"populate_by_name": True}


class WageVerifyResponse(BaseModel):
    minimum_wage_check: MinimumWageCheck
    overtime_check: OvertimeCheck


# ---------------------------------------------------------------------------
# POST /ai/wage/classify
# ---------------------------------------------------------------------------


class Deduction(BaseModel):
    name: str
    amount: int


class WageClassifyRequest(BaseModel):
    deductions: list[Deduction]


DeductionLevel = Literal["정당", "주의", "의심"]


class DeductionFlag(BaseModel):
    name: str
    level: DeductionLevel
    reason: str


class WageClassifyResponse(BaseModel):
    deduction_flags: list[DeductionFlag]


# ---------------------------------------------------------------------------
# POST /ai/exchange/insight
# ---------------------------------------------------------------------------


class RatePoint(BaseModel):
    date: str  # YYYY-MM-DD
    rate: float


class ExchangeInsightRequest(BaseModel):
    """AI server is stateless (see PRD §5 boundary principle): the backend owns
    the exchange-rate cache/API-fallback logic and sends the raw history here —
    this endpoint only computes statistics, never fetches or caches anything."""

    currency: str
    current_rate: float
    rate_history_90d: list[RatePoint]  # >=90 days of daily closes, most recent last


class ExchangeInsightResponse(BaseModel):
    currency: str
    current_rate: float
    percentile_30d: float
    percentile_90d: float
    volatility_level: Literal["low", "medium", "high"]
    volatility_score: float
    message: str

from datetime import datetime

from fastapi import APIRouter

from app import llm_client
from app.benefit_scoring import score_benefits
from app.schemas import (
    REQUIRED_SCORING_FIELDS,
    BenefitExplainRequest,
    BenefitExplainResponse,
    BenefitScoreRequest,
    BenefitScoreResponse,
    CheckInfoRequest,
    CheckInfoResponse,
)

router = APIRouter(prefix="/ai/benefits", tags=["benefits"])


@router.post("/check-info", response_model=CheckInfoResponse)
def check_info(req: CheckInfoRequest) -> CheckInfoResponse:
    profile_dict = req.user_profile.model_dump()
    missing = [field for field in REQUIRED_SCORING_FIELDS if profile_dict.get(field) is None]
    return CheckInfoResponse(needs_more_info=len(missing) > 0, missing_fields=missing)


@router.post("/score", response_model=BenefitScoreResponse)
def score(req: BenefitScoreRequest) -> BenefitScoreResponse:
    today = datetime.strptime(req.today, "%Y-%m-%d").date() if req.today else None
    scored = score_benefits(req.user_profile, req.benefit_candidates, today=today)
    return BenefitScoreResponse(scored_benefits=scored)


@router.post("/explain", response_model=BenefitExplainResponse)
def explain(req: BenefitExplainRequest) -> BenefitExplainResponse:
    result = llm_client.explain_benefit(req.question, req.benefit_title, req.source_documents)
    if not result.grounded:
        return BenefitExplainResponse(
            answer=(
                "죄송해요, 제공된 자료만으로는 정확히 답변드리기 어려워요. "
                "고용노동부 상담(1350) 또는 해당 공고 원문을 확인해주세요."
            ),
            sources=[],
        )
    return BenefitExplainResponse(answer=result.answer, sources=result.sources)

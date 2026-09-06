from fastapi import APIRouter

from app.exchange_insight import compute_insight
from app.schemas import ExchangeInsightRequest, ExchangeInsightResponse

router = APIRouter(prefix="/ai/exchange", tags=["exchange"])


@router.post("/insight", response_model=ExchangeInsightResponse)
def insight(req: ExchangeInsightRequest) -> ExchangeInsightResponse:
    return compute_insight(req)

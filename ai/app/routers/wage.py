from fastapi import APIRouter

from app.schemas import WageClassifyRequest, WageClassifyResponse, WageVerifyRequest, WageVerifyResponse
from app.wage_classify import classify_deductions
from app.wage_verify import verify_wage

router = APIRouter(prefix="/ai/wage", tags=["wage"])


@router.post("/verify", response_model=WageVerifyResponse)
def verify(req: WageVerifyRequest) -> WageVerifyResponse:
    return verify_wage(req)


@router.post("/classify", response_model=WageClassifyResponse)
def classify(req: WageClassifyRequest) -> WageClassifyResponse:
    return WageClassifyResponse(deduction_flags=classify_deductions(req.deductions))

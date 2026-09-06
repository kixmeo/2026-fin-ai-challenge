"""F4 (classify half) — deduction-item tiering.

PRD target design is a Sentence-BERT embedding + trained classifier
(법정/일반/의심 3단계). No labeled training data exists yet, so this is a
keyword-rule MVP standin with the same function signature
(`classify_deductions(list[Deduction]) -> list[DeductionFlag]`) — swapping in
the real embedding classifier later should not require touching the router.
"""

from __future__ import annotations

from app.schemas import Deduction, DeductionFlag, DeductionLevel

LEGAL_KEYWORDS = ["국민연금", "건강보험", "고용보험", "산재보험", "소득세", "지방소득세", "장기요양보험"]
CAUTION_KEYWORDS = ["숙식비", "기숙사비", "식비", "교통비", "관리비", "제복비", "작업복"]
SUSPICIOUS_KEYWORDS = ["명목", "위약금", "손해배상", "벌금", "교육비", "적립금", "보증금", "훈련비"]


def _classify_one(name: str) -> tuple[DeductionLevel, str]:
    if any(k in name for k in LEGAL_KEYWORDS):
        return "정당", "법정 4대보험/세금 공제 항목입니다."
    if any(k in name for k in SUSPICIOUS_KEYWORDS):
        return "의심", "근로기준법상 임의 공제가 제한되는 항목과 유사한 패턴입니다. 고용노동부 상담(1350)에 문의해보세요."
    if any(k in name for k in CAUTION_KEYWORDS):
        return "주의", "법정 공제는 아니지만 실비 상한 규정 준수 여부 확인이 필요합니다."
    return "주의", "일반적이지 않은 공제 항목입니다. 공제 사유와 근거(근로계약서/취업규칙)를 확인해보세요."


def classify_deductions(deductions: list[Deduction]) -> list[DeductionFlag]:
    flags = []
    for deduction in deductions:
        level, reason = _classify_one(deduction.name)
        flags.append(DeductionFlag(name=deduction.name, level=level, reason=reason))
    return flags

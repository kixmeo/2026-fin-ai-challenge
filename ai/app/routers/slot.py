from fastapi import APIRouter

from app import llm_client
from app.config import settings
from app.schemas import SlotExtractRequest, SlotExtractResponse

router = APIRouter(prefix="/ai", tags=["slot"])


@router.post("/slot-extract", response_model=SlotExtractResponse)
def slot_extract(req: SlotExtractRequest) -> SlotExtractResponse:
    result = llm_client.extract_slots(req.message, req.current_profile, req.conversation_history)

    reask_count = req.reask_count
    if not result.is_complete:
        reask_count += 1
        if reask_count > settings.max_reask_count:
            # PRD F1: 2회 초과 시 "나중에 이어서" 옵션 제공 — backend/front decide the UI,
            # we just signal it via the reply so the contract stays a single string field.
            return SlotExtractResponse(
                reply="괜찮아요, 나중에 다시 이어서 답해주셔도 돼요. 지금까지 입력하신 내용은 저장해둘게요.",
                extracted_profile=result.extracted_profile,
                is_complete=False,
                reask_count=reask_count,
            )

    return SlotExtractResponse(
        reply=result.reply,
        extracted_profile=result.extracted_profile,
        is_complete=result.is_complete,
        reask_count=reask_count,
    )

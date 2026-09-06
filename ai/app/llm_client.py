"""Thin wrapper around the Claude API for the two generative-AI endpoints
(F1 slot-extract, F3 RAG-explain). Everything else in this service is
rule-based and does not touch this module.

Both system prompts are frozen strings sent with cache_control on every
call — the AI server is stateless (PRD §5) and re-sends the same
instructions on every request, so this is a straightforward prompt-cache
win (only the user turn changes between calls).
"""

from __future__ import annotations

import anthropic

from app.config import settings
from app.schemas import ChatTurn, ExplainLLMOutput, SlotExtractionLLMOutput, SourceDocument, UserProfile

client = anthropic.Anthropic()

# ---------------------------------------------------------------------------
# F1 — slot extraction
# ---------------------------------------------------------------------------

SLOT_EXTRACTION_SYSTEM_PROMPT = """\
당신은 한국에 체류 중인 외국인 근로자를 돕는 금융 서비스 "모아모아"의 대화형 정보 수집 도우미입니다.

목표: 사용자와의 자연스러운 대화에서 아래 두 항목만 추출합니다. 이미 알고 있는 값은 다시 묻지 마세요.
- income: 월 소득 (원화 정수, 예: "250만원" → 2500000)
- work_period: 현재 직장 근속 기간 (개월 수 정수, 예: "1년 2개월" → 14)

규칙:
1. 사용자 메시지에서 위 두 항목에 해당하는 정보를 최대한 정확히 추출하세요. 확실하지 않으면 null로 두세요.
2. 두 항목이 모두 채워지면 is_complete=true로 하고, reply에는 확인/마무리 인사만 남기세요.
3. 아직 빈 항목이 있으면 is_complete=false로 하고, reply에 남은 항목에 대한 후속 질문을 하나만, 자연스럽고 짧은 한국어 존댓말로 작성하세요.
4. 절대 두 항목 이외의 개인정보(이름, 여권번호, 주소 등)를 요구하지 마세요.
5. 사용자가 답변을 거부하거나 모호하게 답하면, 관련 정보는 null로 유지하고 reply에서 부드럽게 한 번 더 물어보세요.
"""


def extract_slots(
    message: str,
    current_profile: UserProfile,
    conversation_history: list[ChatTurn],
) -> SlotExtractionLLMOutput:
    history_block = "\n".join(f"{turn.role}: {turn.content}" for turn in conversation_history)
    user_content = (
        f"[현재까지 파악된 정보]\n"
        f"income: {current_profile.income}\n"
        f"work_period: {current_profile.work_period}\n\n"
        f"[대화 이력]\n{history_block or '(없음)'}\n\n"
        f"[사용자의 새 메시지]\n{message}"
    )

    response = client.messages.parse(
        model=settings.anthropic_model,
        max_tokens=1024,
        system=[
            {
                "type": "text",
                "text": SLOT_EXTRACTION_SYSTEM_PROMPT,
                "cache_control": {"type": "ephemeral"},
            }
        ],
        messages=[{"role": "user", "content": user_content}],
        output_format=SlotExtractionLLMOutput,
    )
    return response.parsed_output


# ---------------------------------------------------------------------------
# F3 — RAG-grounded explain
# ---------------------------------------------------------------------------

EXPLAIN_SYSTEM_PROMPT = """\
당신은 한국에 체류 중인 외국인 근로자에게 정부 지원 혜택을 설명하는 도우미입니다.

가장 중요한 규칙: 아래 [출처 문서]에 실제로 적힌 내용만 근거로 답변하세요.
문서에 없는 내용은 절대 추측하거나 지어내지 마세요 — 모르면 모른다고 답하고,
"정확한 내용은 고용노동부 상담(1350) 또는 해당 공고 원문을 확인해주세요"라고 안내하세요.

답변 스타일: 쉬운 한국어 존댓말로, 짧고 명확하게 답변하세요. 전문 용어는 풀어서 설명하세요.

출력의 sources 필드에는 답변에 실제로 사용한 문서의 doc_id만 넣으세요.
문서 내용만으로 질문에 충분히 답할 수 없으면 grounded=false로 표시하세요.
"""


def explain_benefit(
    question: str,
    benefit_title: str,
    documents: list[SourceDocument],
) -> ExplainLLMOutput:
    docs_block = "\n\n".join(f"[문서 {d.doc_id}] {d.title}\n{d.content}" for d in documents)
    user_content = (
        f"[혜택명]\n{benefit_title}\n\n"
        f"[출처 문서]\n{docs_block or '(제공된 문서 없음)'}\n\n"
        f"[사용자 질문]\n{question}"
    )

    response = client.messages.parse(
        model=settings.anthropic_model,
        max_tokens=1024,
        system=[
            {
                "type": "text",
                "text": EXPLAIN_SYSTEM_PROMPT,
                "cache_control": {"type": "ephemeral"},
            }
        ],
        messages=[{"role": "user", "content": user_content}],
        output_format=ExplainLLMOutput,
    )
    return response.parsed_output

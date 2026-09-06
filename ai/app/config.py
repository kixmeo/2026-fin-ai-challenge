import os

from dotenv import load_dotenv

load_dotenv()


class Settings:
    anthropic_model: str = os.getenv("MOAMOA_LLM_MODEL", "claude-opus-5")
    # TODO: confirm against the official 고용노동부 최저임금 고시 for 2026 before demo/launch.
    minimum_wage_2026: int = int(os.getenv("MOAMOA_MIN_WAGE_2026", "10320"))
    max_reask_count: int = 2  # PRD F1: 모호한 답변 시 최대 2회 재질문


settings = Settings()

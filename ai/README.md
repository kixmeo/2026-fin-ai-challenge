# moamoa AI server

Stateless FastAPI service implementing the 7 internal AI endpoints from the API spec §8. Called only by the Spring Boot backend — never exposed externally.

## Run

```bash
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env   # then fill in ANTHROPIC_API_KEY
uvicorn app.main:app --reload --port 8001
```

Docs at `http://localhost:8001/docs` once running.

## What's real vs. placeholder

| Endpoint | Logic | Status |
|---|---|---|
| `POST /ai/benefits/check-info` | rule-based (required-field check) | done |
| `POST /ai/slot-extract` | Claude API, structured output | done, needs `ANTHROPIC_API_KEY` |
| `POST /ai/benefits/score` | rule-based eligibility + weighted scoring | done, **weights are a first guess** (`app/benefit_scoring.py` top) — PRD's own risk table flags these as defensible-but-arbitrary |
| `POST /ai/benefits/explain` | Claude API, RAG-grounded, structured output | done, needs `ANTHROPIC_API_KEY`. Caller must pass in the already-retrieved source documents — this service does no vector search itself |
| `POST /ai/wage/verify` | rule-based, 근로기준법 formula | done — **see the discrepancy note at the top of `app/wage_verify.py`**: the API spec's own worked example doesn't reconcile with either the minimum-wage or the overtime formula. Confirm the intended math with the team before demo |
| `POST /ai/wage/classify` | keyword-rule MVP standin for the PRD's Sentence-BERT classifier | placeholder — same function signature so the real model can drop in later, see `app/wage_classify.py` |
| `POST /ai/exchange/insight` | pure statistics (percentile + coefficient of variation) | done. Caller (backend) supplies the rate history — this service has no cache/external-API logic by design, per the PRD's stateless-AI-server boundary |

All 7 routes + `/health` verified registered (`app.openapi()`); the rule-based modules (`benefit_scoring`, `wage_verify`, `wage_classify`, `exchange_insight`) were smoke-tested locally against the request/response shapes. `llm_client.py` (the two Claude-backed endpoints) was not run end-to-end — no API key was available in the sandbox this was written in.

## Open items to resolve with the team

1. **Wage formula discrepancy** — see above.
2. **2026 minimum wage constant** (`MOAMOA_MIN_WAGE_2026=10320` in `.env.example`) — taken directly from the API spec's example, not independently verified against the official 고용노동부 고시.
3. **Benefit scoring weights** — amount 0.5 / deadline-urgency 0.3 / doc-difficulty 0.2, all in `app/benefit_scoring.py`. Arbitrary starting point, not tuned on real data.
4. **F1/F2 guest-mode question from the flowchart-vs-userflow discussion is still open** — this server doesn't care either way (it's stateless and takes whatever profile the backend sends), but it affects what the backend can send on an unauthenticated call.

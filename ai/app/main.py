from fastapi import FastAPI

from app.routers import benefits, exchange, slot, wage

app = FastAPI(
    title="moamoa AI server",
    description="Stateless internal AI service — see API spec §8 (AI 서버 내부 API). Called only by the backend, never exposed externally.",
)

app.include_router(benefits.router)
app.include_router(slot.router)
app.include_router(wage.router)
app.include_router(exchange.router)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}

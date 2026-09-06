import { getAccessToken } from "../lib/supabase.js";
import * as mock from "./mock.js";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8000";

async function request(path, options = {}) {
  const token = await getAccessToken();
  const res = await fetch(BASE_URL + path, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });
  const json = await res.json().catch(() => null);
  if (!res.ok || !json || json.success === false) {
    const message = (json && json.error && json.error.message) || `요청 실패 (${res.status})`;
    throw new Error(message);
  }
  return json.data;
}

// 실제 호출이 실패하면(백엔드가 아직 없거나, 아직 안 켜져 있거나) 자동으로 mock 데이터로 대체해요.
// 브라우저 콘솔에 경고가 남으니, 지금 실제 응답인지 mock인지는 거기서 확인할 수 있어요.
function withFallback(realFn, mockFn, label) {
  return async (...args) => {
    try {
      return await realFn(...args);
    } catch (err) {
      console.warn(`[api] ${label} 실패 → mock으로 대체:`, err.message);
      return mockFn(...args);
    }
  };
}

export const api = {
  // GET /api/auth/me
  getMe: withFallback(() => request("/api/auth/me"), mock.getMe, "GET /api/auth/me"),

  // POST /api/profile/basic
  postBasicProfile: withFallback(
    (payload) => request("/api/profile/basic", { method: "POST", body: JSON.stringify(payload) }),
    mock.postBasicProfile,
    "POST /api/profile/basic"
  ),

  // GET /api/benefits (profile은 real 호출에는 안 쓰이고, mock 대체용으로만 씀)
  getBenefits: withFallback(
    () => request("/api/benefits"),
    mock.getBenefits,
    "GET /api/benefits"
  ),

  // POST /api/chat/message
  postChatMessage: withFallback(
    (sessionId, message) => request("/api/chat/message", { method: "POST", body: JSON.stringify({ session_id: sessionId, message }) }),
    mock.postChatMessage,
    "POST /api/chat/message"
  ),

  // POST /api/benefits/{id}/explain
  explainBenefit: withFallback(
    (benefitId, question) => request(`/api/benefits/${benefitId}/explain`, { method: "POST", body: JSON.stringify({ question }) }),
    mock.explainBenefit,
    "POST /api/benefits/:id/explain"
  ),

  // POST /api/benefits/{id}/calendar
  addBenefitToCalendar: withFallback(
    (benefitId) => request(`/api/benefits/${benefitId}/calendar`, { method: "POST", body: JSON.stringify({}) }),
    mock.addBenefitToCalendar,
    "POST /api/benefits/:id/calendar"
  ),

  // POST /api/wage-check
  postWageCheck: withFallback(
    (form) =>
      request("/api/wage-check", {
        method: "POST",
        body: JSON.stringify({
          base_wage: Number(form.base_wage),
          work_hours_per_week: Number(form.work_hours_per_week),
          overtime_hours: Number(form.overtime_hours),
          overtime_pay: Number(form.overtime_pay),
          deductions: form.deductions
            .filter((d) => d.name.trim())
            .map((d) => ({ name: d.name, amount: Number(d.amount) })),
        }),
      }),
    mock.postWageCheck,
    "POST /api/wage-check"
  ),

  // GET /api/exchange-rate/insight
  getExchangeInsight: withFallback(
    (currency) => request(`/api/exchange-rate/insight?currency=${currency}&window=30`),
    mock.getExchangeInsight,
    "GET /api/exchange-rate/insight"
  ),

  // GET /api/fees — mock 대체 없음: 실제 돈 관련 수치라 백엔드 에러를 가짜 데이터로 가리면 안 됨
  getFees: (amount) => request(`/api/fees?amount=${amount}`),

  // GET /api/calendar
  getCalendar: withFallback(() => request("/api/calendar"), mock.getCalendar, "GET /api/calendar"),

  // POST /api/calendar — mock 대체 없음: 실패를 성공으로 가리면 일정이 저장 안 된 채 저장됐다고 표시됨
  postCalendarEvent: (title, date) => request("/api/calendar", { method: "POST", body: JSON.stringify({ title, date }) }),

  // DELETE /api/calendar/{event_id} — mock 대체 없음: 실패를 성공으로 가리면 실제로는 안 지워졌는데 지워졌다고 표시됨
  deleteCalendarEvent: (id) => request(`/api/calendar/${id}`, { method: "DELETE" }),
};

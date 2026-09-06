// 목업 데이터 — 실제 백엔드가 아직 없거나 응답하지 않을 때 client.js가 자동으로 이걸 대신 써요.
// 실제 엔드포인트가 다 붙으면 이 파일은 그냥 안 쓰이게 됩니다 (지우지 않아도 무방해요).
const wait = (ms = 400) => new Promise((res) => setTimeout(res, ms));

let mockProfileDetail = { income: null, work_period: null };
const chatSessions = new Map();

export async function getMe() {
  await wait(300);
  return { user_id: "mock_u1", email: "mock@example.com", basic_profile_complete: false };
}

export async function postBasicProfile(payload) {
  await wait(400);
  return { basic_profile_complete: true, ...payload };
}

export async function getBenefits(profile) {
  await wait(500);
  const hasDetail =
    (profile && profile.income != null && profile.work_period != null) ||
    (mockProfileDetail.income != null && mockProfileDetail.work_period != null);
  if (!hasDetail) {
    const sessionId = "mock_chat_" + Math.random().toString(36).slice(2, 8);
    chatSessions.set(sessionId, 0);
    return { needs_more_info: true, session_id: sessionId, benefits: [] };
  }
  return {
    needs_more_info: false,
    benefits: [
      { benefit_id: "b_017", title: "외국인근로자 귀국비용보험", score: 0.86, amount: 500000, deadline: "2026-09-30", required_docs_count: 2 },
      { benefit_id: "b_021", title: "외국인근로자 국민연금 반환일시금", score: 0.74, amount: 1200000, deadline: "2026-11-15", required_docs_count: 3 },
      { benefit_id: "b_009", title: "산업재해 소액치료비 지원", score: 0.58, amount: 300000, deadline: "2026-10-05", required_docs_count: 1 },
    ],
  };
}

export async function postChatMessage(sessionId, message) {
  await wait(600);
  const step = chatSessions.get(sessionId) ?? 0;
  if (step === 0) {
    const digits = message.replace(/[^0-9]/g, "");
    let income = digits ? Number(digits) : 2500000;
    if (message.includes("만원") && digits.length <= 4) income = income * 10000;
    mockProfileDetail.income = income;
    chatSessions.set(sessionId, 1);
    return { reply: "그럼 지금 근무하신 지는 얼마나 되셨어요?", extracted_profile: { income, work_period: null }, is_complete: false };
  }
  const digits = message.replace(/[^0-9]/g, "");
  const work_period = digits ? Number(digits) : 14;
  mockProfileDetail.work_period = work_period;
  return { reply: "감사해요! 입력해주신 정보로 받으실 수 있는 혜택을 찾아볼게요.", extracted_profile: { work_period }, is_complete: true };
}

export async function explainBenefit(benefitId, question) {
  await wait(700);
  return {
    answer: "귀국 준비 시 드는 항공권·이사 비용 일부를 지원하는 제도예요. 필요서류는 여권 사본과 재직증명서예요. 신청은 출국 30일 전부터 가능해요.",
    sources: ["고용노동부 공고 2026-114호"],
  };
}

export async function addBenefitToCalendar(benefitId) {
  await wait(500);
  return { calendar_event_id: "mock_evt_" + Math.floor(Math.random() * 1000) };
}

export async function postWageCheck(form) {
  await wait(900);
  const monthlyHours = Number(form.work_hours_per_week) * 4;
  const hourly_wage = Math.round(Number(form.base_wage) / monthlyHours);
  const minimum_wage_2026 = 10320;
  const minWagePass = hourly_wage >= minimum_wage_2026;
  const expected = Math.round(hourly_wage * 1.5 * Number(form.overtime_hours || 0));
  const actual = Number(form.overtime_pay || 0);
  const overtimePass = actual >= expected * 0.95;
  const positiveKeywords = ["국민연금", "건강보험", "고용보험", "산재보험", "소득세", "지방소득세"];
  const cautionKeywords = ["숙식비", "기숙사비", "식비", "관리비", "교통비"];
  const deduction_flags = form.deductions
    .filter((d) => d.name.trim())
    .map((d) => {
      const name = d.name.trim();
      if (positiveKeywords.some((k) => name.includes(k))) return { name, amount: Number(d.amount), level: "정당", reason: "법정 4대보험/세금 공제 항목이에요." };
      if (cautionKeywords.some((k) => name.includes(k))) return { name, amount: Number(d.amount), level: "주의", reason: "상한 규정이 있는 항목이에요. 금액이 과도하지 않은지 확인이 필요해요." };
      return { name, amount: Number(d.amount), level: "의심", reason: "근로기준법상 임의 공제가 제한되는 항목과 패턴이 비슷해요." };
    });
  const has_suspicious = !minWagePass || !overtimePass || deduction_flags.some((d) => d.level === "의심");
  return {
    minimum_wage_check: {
      pass: minWagePass,
      hourly_wage,
      minimum_wage_2026,
      reason: minWagePass ? "2026년 최저시급 기준을 충족하고 있어요." : "계산된 시급이 2026년 최저시급보다 낮아요. 최저임금법 위반 소지가 있어요.",
    },
    overtime_check: {
      pass: overtimePass,
      expected,
      actual,
      reason: overtimePass ? "근로기준법상 가산수당(1.5배) 기준을 충족하고 있어요." : "연장근로 가산수당(통상임금의 1.5배)보다 실지급액이 적어요.",
    },
    deduction_flags,
    has_suspicious,
    disclaimer: "본 진단 결과는 참고용이며 법적 효력이 없습니다.",
    consultation_notice: "의심되는 항목이 있어요. 정확한 판단은 고용노동부 상담센터(국번없이 1350)에서 받아보실 수 있어요.",
  };
}

export async function getExchangeInsight(currency) {
  await wait(500);
  const table = {
    USD: { current_rate: 1385.2, percentile_30d: 82, percentile_90d: 74, volatility_level: "high", volatility_score: 1.8 },
    PHP: { current_rate: 24.35, percentile_30d: 41, percentile_90d: 55, volatility_level: "medium", volatility_score: 1.1 },
    VND: { current_rate: 0.0552, percentile_30d: 18, percentile_90d: 22, volatility_level: "low", volatility_score: 0.4 },
    THB: { current_rate: 39.8, percentile_30d: 63, percentile_90d: 58, volatility_level: "medium", volatility_score: 0.9 },
  };
  const d = table[currency] || table.USD;
  const messages = {
    high: "최근 30일 중 상위 " + (100 - d.percentile_30d) + "%에 해당하는 높은 환율이에요. 변동성도 평소보다 큰 편이라 지금 급하게 결정하기보단 며칠 지켜보는 것도 방법이에요.",
    medium: "최근 30일 평균과 비슷한 수준이에요. 급하지 않다면 조금 더 지켜봐도 괜찮아요.",
    low: "최근 30일 중 낮은 편에 속하는 환율이에요. 송금을 계획 중이라면 지금이 나쁘지 않은 타이밍이에요.",
  };
  return { currency, date: "2026-08-26", ...d, message: messages[d.volatility_level] };
}

export async function getCalendar() {
  await wait(200);
  return { events: [] };
}


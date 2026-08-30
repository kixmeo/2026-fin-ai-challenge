import { useState, useEffect } from "react";
import PageWrap from "./components/PageWrap.jsx";
import Sidebar from "./components/Sidebar.jsx";
import TopHeader from "./components/TopHeader.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import OnboardingPage from "./pages/OnboardingPage.jsx";
import HomePage from "./pages/HomePage.jsx";
import BenefitsPage from "./pages/BenefitsPage.jsx";
import ChatPanel from "./pages/ChatPanel.jsx";
import BenefitDetailPage from "./pages/BenefitDetailPage.jsx";
import WagePage from "./pages/WagePage.jsx";
import ExchangePage from "./pages/ExchangePage.jsx";
import FeesPage from "./pages/FeesPage.jsx";
import CalendarPage from "./pages/CalendarPage.jsx";
import { api } from "./api/client.js";
import { supabase } from "./lib/supabase.js";

export default function App() {
  const [stack, setStack] = useState(["login"]);
  const screen = stack[stack.length - 1];
  const push = (s) => setStack((prev) => [...prev, s]);
  const pop = () => setStack((prev) => (prev.length > 1 ? prev.slice(0, -1) : prev));
  const goTab = (s) => setStack([s]);

  const [profile, setProfile] = useState({ visa_type: "", name: "", residence_region: "", income: null, work_period: null });
  const [loading, setLoading] = useState(false);
  const [selectedBenefit, setSelectedBenefit] = useState(null);
  const [chatSessionId, setChatSessionId] = useState(null);
  const [events, setEvents] = useState([]);
  const [toast, setToast] = useState(null);
  const showToast = (msg) => { setToast(msg); setTimeout(() => setToast(null), 2200); };

  const loadCalendar = async () => {
    try {
      const res = await api.getCalendar();
      setEvents(res.events || []);
    } catch {
      // 조용히 무시 — 초기 목록이 비어 있어도 화면엔 빈 상태가 뜰 뿐이라 문제 없음
    }
  };

  // Supabase가 설정돼 있으면(.env에 URL/KEY가 있으면) 실제 구글 로그인 리다이렉트 이후
  // 세션이 감지될 때 자동으로 로그인 처리를 이어가요. 설정 전이면 이 effect는 아무 일도 안 해요.
  useEffect(() => {
    if (!supabase) return;
    const { data: sub } = supabase.auth.onAuthStateChange(async (_event, session) => {
      if (session) {
        setLoading(true);
        const me = await api.getMe();
        setLoading(false);
        setProfile((p) => ({ ...p, email: session.user.email }));
        if (me.basic_profile_complete) { await loadCalendar(); push("home"); }
        else push("onboarding");
      }
    });
    return () => sub.subscription.unsubscribe();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleLogin = async () => {
    if (supabase) {
      // 실제 구글 로그인: 리다이렉트되고, 이후 처리는 위 onAuthStateChange가 이어받음
      setLoading(true);
      await supabase.auth.signInWithOAuth({ provider: "google", options: { redirectTo: window.location.origin } });
      return;
    }
    // Supabase .env가 아직 없으면(초기 개발 단계) mock으로 바로 로그인
    setLoading(true);
    const me = await api.getMe();
    setLoading(false);
    const alreadyOnboarded = Boolean(profile.name) || me.basic_profile_complete;
    if (alreadyOnboarded) await loadCalendar();
    push(alreadyOnboarded ? "home" : "onboarding");
  };

  const handleBasicProfile = async (payload) => {
    setLoading(true);
    await api.postBasicProfile(payload);
    setLoading(false);
    setProfile((p) => ({ ...p, ...payload }));
    await loadCalendar();
    goTab("home");
  };

  const handleChatComplete = (extracted) => {
    setProfile((p) => ({ ...p, ...extracted }));
    setStack(["benefits"]);
  };

  const handleAddBenefitToCalendar = (benefit) => {
    setEvents((prev) => [...prev, { id: "evt_" + Date.now(), title: benefit.title, date: benefit.deadline }]);
    showToast("캘린더에 마감일을 등록했어요");
  };

  const handleManualAddEvent = async (title, date) => {
    await api.postCalendarEvent(title, date);
    setEvents((prev) => [...prev, { id: "evt_" + Date.now(), title, date }]);
    showToast("일정을 추가했어요");
  };

  const handleDeleteEvent = async (id) => {
    await api.deleteCalendarEvent(id);
    setEvents((prev) => prev.filter((e) => e.id !== id));
  };

  const handleLogout = async () => {
    if (supabase) await supabase.auth.signOut();
    setStack(["login"]);
  };

  if (screen === "login") return <PageWrap><LoginPage onLogin={handleLogin} loading={loading} /></PageWrap>;
  if (screen === "onboarding") return <PageWrap><OnboardingPage onSubmit={handleBasicProfile} loading={loading} /></PageWrap>;

  const headers = {
    home: { title: `안녕하세요, ${profile.name || "회원"}님`, subtitle: "오늘도 모아모아가 도와드릴게요" },
    benefits: { title: "혜택 찾기", subtitle: "나에게 맞는 지원금을 찾아드려요" },
    chat: { title: "혜택 찾기", subtitle: "몇 가지만 답하면 맞춤 혜택을 찾아드려요" },
    benefitDetail: { title: selectedBenefit ? selectedBenefit.title : "혜택 상세", subtitle: "자세한 내용을 확인하고 궁금한 점을 물어보세요" },
    wage: { title: "임금 진단", subtitle: "급여명세서를 기반으로 체불·부당공제를 확인해요" },
    exchange: { title: "환율 인사이트", subtitle: "지금이 송금하기 좋은 타이밍인지 알려드려요" },
    fees: { title: "송금 수수료 비교", subtitle: "채널별 수수료와 소요 시간을 비교해요" },
    calendar: { title: "캘린더", subtitle: "신청 마감일을 놓치지 않도록 관리해요" },
  };
  const sectionFor = { chat: "benefits", benefitDetail: "benefits" };
  const activeSection = sectionFor[screen] || screen;

  let page = null;
  if (screen === "home") page = <HomePage profile={profile} onOpen={goTab} />;
  else if (screen === "benefits")
    page = (
      <BenefitsPage
        profile={profile}
        onNeedsChat={(sessionId) => { setChatSessionId(sessionId); push("chat"); }}
        onOpenDetail={(b) => { setSelectedBenefit(b); push("benefitDetail"); }}
      />
    );
  else if (screen === "chat") page = <ChatPanel sessionId={chatSessionId} onComplete={handleChatComplete} />;
  else if (screen === "benefitDetail") page = <BenefitDetailPage benefit={selectedBenefit} onBack={pop} onAdded={handleAddBenefitToCalendar} />;
  else if (screen === "wage") page = <WagePage />;
  else if (screen === "exchange") page = <ExchangePage />;
  else if (screen === "fees") page = <FeesPage />;
  else if (screen === "calendar") page = <CalendarPage events={events} onAdd={handleManualAddEvent} onDelete={handleDeleteEvent} />;

  const h = headers[screen] || {};

  return (
    <PageWrap>
      <div style={{ display: "flex", minHeight: "100vh" }}>
        <Sidebar active={activeSection} onNavigate={goTab} profile={profile} onLogout={handleLogout} />
        <div key={screen} className="page-enter" style={{ flex: 1, display: "flex", flexDirection: "column", minWidth: 0 }}>
          <TopHeader title={h.title} subtitle={h.subtitle} />
          <div style={{ flex: 1, overflowY: "auto" }}>
            <div style={{ maxWidth: 1120, margin: "0 auto", padding: "36px 44px" }}>{page}</div>
          </div>
        </div>
      </div>
      {toast && (
        <div style={{ position: "fixed", bottom: 28, left: "50%", transform: "translateX(-50%)", background: "rgba(11,12,15,0.92)", color: "#fff", padding: "13px 24px", borderRadius: 26, fontSize: 14.5, fontWeight: 700, whiteSpace: "nowrap", animation: "toastIn .25s ease", zIndex: 50 }}>
          {toast}
        </div>
      )}
    </PageWrap>
  );
}

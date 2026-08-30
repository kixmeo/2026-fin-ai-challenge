import { useState, useEffect } from "react";
import { Clock } from "lucide-react";
import { C } from "../lib/theme.js";
import { Card } from "../components/ui/Primitives.jsx";
import { won } from "../lib/format.js";
import { useCountUp } from "../lib/useCountUp.js";
import { api } from "../api/client.js";

function BenefitsPage({ profile, onNeedsChat, onOpenDetail }) {
  const [state, setState] = useState({ loading: true, data: null });

  const load = async () => {
    setState({ loading: true, data: null });
    const res = await api.getBenefits(profile);
    setState({ loading: false, data: res });
    if (res.needs_more_info) onNeedsChat(res.session_id);
  };
  useEffect(() => { load(); /* eslint-disable-next-line */ }, [profile.income, profile.work_period]);

  const benefits = state.data && !state.data.needs_more_info ? state.data.benefits : [];
  const totalAmount = benefits.reduce((sum, b) => sum + b.amount, 0);
  const nearestDeadline = benefits.length ? benefits.reduce((a, b) => (a < b.deadline ? a : b.deadline), benefits[0].deadline) : null;
  const nearestDays = nearestDeadline ? Math.max(0, Math.ceil((new Date(nearestDeadline) - new Date()) / 86400000)) : 0;
  const countAnim = useCountUp(benefits.length, 600);
  const amountAnim = useCountUp(totalAmount, 800);
  const daysAnim = useCountUp(nearestDays, 600);

  if (state.loading || !state.data || state.data.needs_more_info) {
    return (
      <div style={{ display: "flex", alignItems: "center", justifyContent: "center", flexDirection: "column", gap: 12, padding: "120px 0" }}>
        <div style={{ width: 30, height: 30, border: `3px solid ${C.border}`, borderTopColor: C.primary, borderRadius: "50%", animation: "spin 0.8s linear infinite" }} />
        <p style={{ color: C.textMute, fontSize: 14.5 }}>맞춤 혜택을 찾고 있어요</p>
      </div>
    );
  }

  return (
    <div>
      <div className="pop-in" style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 1, background: C.border, borderRadius: 20, overflow: "hidden", marginBottom: 24, border: `1px solid ${C.border}` }}>
        <div style={{ background: "#fff", padding: "22px 24px" }}>
          <p style={{ fontSize: 13, color: C.textMute, margin: "0 0 8px" }}>매칭된 혜택</p>
          <p style={{ fontSize: 28, fontWeight: 800, color: C.text, margin: 0, letterSpacing: "-0.02em" }}>{Math.round(countAnim)}건</p>
        </div>
        <div style={{ background: "#fff", padding: "22px 24px" }}>
          <p style={{ fontSize: 13, color: C.textMute, margin: "0 0 8px" }}>예상 총 수령액</p>
          <p style={{ fontSize: 28, fontWeight: 800, color: C.primary, margin: 0, letterSpacing: "-0.02em" }}>{won(Math.round(amountAnim))}</p>
        </div>
        <div style={{ background: "#fff", padding: "22px 24px" }}>
          <p style={{ fontSize: 13, color: C.textMute, margin: "0 0 8px" }}>가장 빠른 마감</p>
          <p style={{ fontSize: 28, fontWeight: 800, color: C.danger, margin: 0, letterSpacing: "-0.02em" }}>D-{Math.round(daysAnim)}</p>
        </div>
      </div>

      <p style={{ color: C.textMute, fontSize: 15, margin: "0 0 20px" }}>매칭 스코어가 높은 순서로 정렬했어요 · 총 {benefits.length}건</p>
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))", gap: 16 }}>
        {benefits.map((b, i) => (
          <BenefitCard key={b.benefit_id} b={b} index={i} onClick={() => onOpenDetail(b)} />
        ))}
      </div>
    </div>
  );
}

function BenefitCard({ b, onClick, index }) {
  const amount = useCountUp(b.amount, 850);
  return (
    <Card onClick={onClick} className="liftable pop-in" style={{ padding: 24, animationDelay: `${index * 0.08}s` }}>
      <span style={{ fontSize: 12.5, fontWeight: 800, color: C.primary, background: C.primaryLight, padding: "5px 10px", borderRadius: 9 }}>매칭 {Math.round(b.score * 100)}%</span>
      <p style={{ margin: "14px 0 0", fontWeight: 800, fontSize: 17.5, color: C.text }}>{b.title}</p>
      <p style={{ margin: "12px 0 0", fontSize: 28, fontWeight: 800, color: C.text, letterSpacing: "-0.02em" }}>{won(Math.round(amount))}</p>
      <div style={{ display: "flex", gap: 16, marginTop: 14, fontSize: 13.5, color: C.textMute, borderTop: `1px solid ${C.border}`, paddingTop: 14 }}>
        <span style={{ display: "flex", alignItems: "center", gap: 5 }}><Clock size={13} /> ~{b.deadline}</span>
        <span>서류 {b.required_docs_count}개</span>
      </div>
    </Card>
  );
}

export default BenefitsPage;
export { BenefitCard };

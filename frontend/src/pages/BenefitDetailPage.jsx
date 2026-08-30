import { useState, useRef, useEffect } from "react";
import { ArrowLeft, MessageCircle, Send, Clock, ClipboardList, Sparkles } from "lucide-react";
import { C } from "../lib/theme.js";
import { Btn, Row } from "../components/ui/Primitives.jsx";
import { won } from "../lib/format.js";
import { useCountUp } from "../lib/useCountUp.js";
import { api } from "../api/client.js";

function BenefitDetailPage({ benefit, onBack, onAdded }) {
  const [qa, setQa] = useState([]);
  const [question, setQuestion] = useState("");
  const [asking, setAsking] = useState(false);
  const [added, setAdded] = useState(false);
  const bottomRef = useRef(null);
  const amount = useCountUp(benefit.amount, 850);
  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: "smooth" }); }, [qa]);

  const ask = async () => {
    if (!question.trim()) return;
    const q = question.trim();
    setQuestion("");
    setAsking(true);
    const res = await api.explainBenefit(benefit.benefit_id, q);
    setQa((prev) => [...prev, { q, ...res }]);
    setAsking(false);
  };
  const addToCalendar = async () => {
    await api.addBenefitToCalendar(benefit.benefit_id);
    setAdded(true);
    onAdded(benefit);
  };

  return (
    <div>
      <button onClick={onBack} style={{ display: "flex", alignItems: "center", gap: 7, background: "none", border: "none", color: C.textSub, fontSize: 14.5, fontWeight: 700, cursor: "pointer", padding: 0, marginBottom: 22 }}>
        <ArrowLeft size={16} /> 혜택 목록으로
      </button>
      <div className="grid-detail">
        <div className="pop-in" style={{ background: "#fff", border: `1px solid ${C.border}`, borderRadius: 24, overflow: "hidden", display: "flex", flexDirection: "column", height: 540 }}>
          <div style={{ padding: "20px 22px", borderBottom: `1px solid ${C.border}`, display: "flex", alignItems: "center", gap: 9 }}>
            <MessageCircle size={17} color={C.primary} />
            <span style={{ fontWeight: 800, fontSize: 15 }}>궁금한 점을 물어보세요</span>
          </div>
          <div style={{ flex: 1, overflowY: "auto", padding: 22 }}>
            {qa.length === 0 && <p style={{ color: C.textMute, fontSize: 14 }}>예: "이거 무슨 말이야?", "필요서류가 뭔데?"</p>}
            {qa.map((item, i) => (
              <div key={i} style={{ marginBottom: 18 }}>
                <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 9 }}>
                  <div style={{ background: C.primary, color: "#fff", padding: "11px 15px", borderRadius: 15, borderBottomRightRadius: 4, fontSize: 15, maxWidth: "80%" }}>{item.q}</div>
                </div>
                <div style={{ background: C.surfaceDeep, padding: "13px 15px", borderRadius: 15, borderBottomLeftRadius: 4, fontSize: 15, color: C.text, lineHeight: 1.65, maxWidth: "88%" }}>
                  {item.answer}
                  <div style={{ marginTop: 9, fontSize: 12, color: C.textMute }}>출처: {item.sources.join(", ")}</div>
                </div>
              </div>
            ))}
            {asking && <p style={{ fontSize: 14, color: C.textMute }}>답변을 찾고 있어요...</p>}
            <div ref={bottomRef} />
          </div>
          <div style={{ display: "flex", gap: 9, padding: 16, borderTop: `1px solid ${C.border}` }}>
            <input value={question} onChange={(e) => setQuestion(e.target.value)} onKeyDown={(e) => e.key === "Enter" && ask()} placeholder="예: 필요서류가 뭔데?" style={{ flex: 1, padding: "13px 15px", borderRadius: 13, border: `1px solid ${C.border}`, outline: "none", fontSize: 15 }} />
            <button onClick={ask} className="btn-pop" style={{ width: 46, height: 46, borderRadius: 13, background: C.primary, border: "none", display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer", flexShrink: 0 }}>
              <Send size={19} color="#fff" />
            </button>
          </div>
        </div>

        <div className="pop-in" style={{ background: "#fff", border: `1px solid ${C.border}`, borderRadius: 24, padding: 24, animationDelay: "0.08s" }}>
          <p style={{ fontSize: 18, fontWeight: 800, color: C.text, margin: "0 0 6px" }}>{benefit.title}</p>
          <p style={{ fontSize: 36, fontWeight: 800, color: C.primary, margin: "12px 0 20px", letterSpacing: "-0.02em" }}>{won(Math.round(amount))}</p>
          <Row icon={Clock} label="신청 마감일" value={benefit.deadline} />
          <Row icon={ClipboardList} label="필요 서류" value={`${benefit.required_docs_count}개`} />
          <Row icon={Sparkles} label="매칭 점수" value={`${Math.round(benefit.score * 100)}%`} last />
          <div style={{ marginTop: 20 }}>
            <Btn variant={added ? "ghost" : "secondary"} onClick={added ? undefined : addToCalendar} disabled={added}>
              {added ? "✓ 캘린더에 등록됨" : "캘린더에 마감일 등록하기"}
            </Btn>
          </div>
        </div>
      </div>
    </div>
  );
}

export default BenefitDetailPage;

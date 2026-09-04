import { Sparkles, Gift, ClipboardList, TrendingUp, ArrowRightLeft, Calendar } from "lucide-react";
import { C } from "../lib/theme.js";
import { Card } from "../components/ui/Primitives.jsx";

function HomePage({ profile, onOpen }) {
  const menu = [
    { key: "benefits", title: "혜택 찾기", desc: "나에게 맞는 지원금을 확인해요", icon: Gift, color: C.primary },
    { key: "wage", title: "임금 진단", desc: "체불·부당공제를 확인해요", icon: ClipboardList, color: C.violet },
    { key: "exchange", title: "환율 인사이트", desc: "지금 보낼 타이밍인지 알려드려요", icon: TrendingUp, color: C.cyan },
    { key: "fees", title: "송금 수수료 비교", desc: "가장 저렴한 채널을 찾아요", icon: ArrowRightLeft, color: "#FF7A45" },
    { key: "calendar", title: "캘린더", desc: "신청 마감일을 관리해요", icon: Calendar, color: "#4E5968" },
  ];
  return (
    <div>
      <div className="pop-in" style={{ position: "relative", background: C.primary, borderRadius: 32, padding: "54px 50px", color: "#fff", marginBottom: 34, minHeight: 210 }}>
        <div style={{ position: "relative", zIndex: 1, display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <div>
            <div style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 14, opacity: 0.9, marginBottom: 16 }}>
              <Sparkles size={16} /> AI 추천
            </div>
            <p style={{ fontSize: 34, fontWeight: 800, margin: "0 0 12px", letterSpacing: "-0.02em" }}>받을 수 있는 혜택이 있어요</p>
            <p style={{ fontSize: 16, opacity: 0.88, margin: "0 0 26px" }}>몇 가지만 답하면 바로 확인할 수 있어요</p>
            <button onClick={() => onOpen("benefits")} className="btn-pop" style={{ background: "#fff", color: C.primaryDark, border: "none", borderRadius: 13, padding: "14px 26px", fontWeight: 800, fontSize: 15.5, cursor: "pointer" }}>
              혜택 찾아보기
            </button>
          </div>
          <div className="floaty" style={{ width: 100, height: 100, borderRadius: 28, background: "rgba(255,255,255,0.16)", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
            <Gift size={48} />
          </div>
        </div>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(240px, 1fr))", gap: 16 }}>
        {menu.map((m, i) => {
          const Icon = m.icon;
          return (
            <Card key={m.key} onClick={() => onOpen(m.key)} className="liftable pop-in" style={{ padding: 26, animationDelay: `${i * 0.07}s` }}>
              <div style={{ width: 48, height: 48, borderRadius: 14, background: m.color + "1A", display: "flex", alignItems: "center", justifyContent: "center", marginBottom: 18 }}>
                <Icon size={24} color={m.color} />
              </div>
              <p style={{ margin: 0, fontWeight: 800, fontSize: 18, color: C.text }}>{m.title}</p>
              <p style={{ margin: "7px 0 0", fontSize: 14.5, color: C.textMute }}>{m.desc}</p>
            </Card>
          );
        })}
      </div>
    </div>
  );
}

export default HomePage;

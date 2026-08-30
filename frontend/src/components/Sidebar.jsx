import {
  Home, Gift, ClipboardList, TrendingUp, Calendar, LogOut, ArrowRightLeft, Sparkles,
} from "lucide-react";
import { C } from "../lib/theme.js";

function Sidebar({ active, onNavigate, profile, onLogout }) {
  const items = [
    { key: "home", label: "홈", icon: Home },
    { key: "benefits", label: "혜택 찾기", icon: Gift },
    { key: "wage", label: "임금 진단", icon: ClipboardList },
    { key: "exchange", label: "환율 인사이트", icon: TrendingUp },
    { key: "fees", label: "수수료 비교", icon: ArrowRightLeft },
    { key: "calendar", label: "캘린더", icon: Calendar },
  ];
  const activeIndex = Math.max(0, items.findIndex((it) => it.key === active));

  return (
    <div className="sidebar" style={{ width: 256, flexShrink: 0, background: "#fff", borderRight: `1px solid ${C.border}`, display: "flex", flexDirection: "column", padding: "26px 14px" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 10, padding: "0 8px", marginBottom: 32 }}>
        <div style={{ width: 36, height: 36, borderRadius: 11, background: C.primary, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
          <Sparkles size={19} color="#fff" />
        </div>
        <span style={{ fontSize: 22, fontWeight: 800, color: C.text, letterSpacing: "-0.02em" }}>모아모아</span>
      </div>

      <div className="sidebar-nav" style={{ position: "relative", display: "flex", flexDirection: "column", gap: 6 }}>
        <div className="sidebar-pill" style={{ transform: `translateY(${activeIndex * 54}px)` }} />
        {items.map((it) => {
          const Icon = it.icon;
          const isActive = active === it.key;
          return (
            <button key={it.key} className={`nav-item ${isActive ? "active" : ""}`} onClick={() => onNavigate(it.key)}>
              <Icon size={19} strokeWidth={isActive ? 2.4 : 2} />
              {it.label}
            </button>
          );
        })}
      </div>

      <div className="sidebar-foot" style={{ marginTop: "auto", paddingTop: 18, borderTop: `1px solid ${C.border}` }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10, padding: "8px", marginBottom: 4 }}>
          <div style={{ width: 36, height: 36, borderRadius: "50%", background: C.primaryLight, color: C.primaryDark, display: "flex", alignItems: "center", justifyContent: "center", fontWeight: 800, fontSize: 15, flexShrink: 0 }}>
            {(profile.name || "?").slice(0, 1)}
          </div>
          <div style={{ minWidth: 0 }}>
            <p style={{ margin: 0, fontSize: 14, fontWeight: 700, color: C.text, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{profile.name || "회원"}</p>
            <p style={{ margin: 0, fontSize: 12, color: C.textMute }}>{profile.residence_region || "-"} · {profile.visa_type || "-"}</p>
          </div>
        </div>
        <button className="nav-item" onClick={onLogout} style={{ color: C.textMute, height: 42 }}>
          <LogOut size={17} /> 로그아웃
        </button>
      </div>
    </div>
  );
}

export default Sidebar;

import {
  Home, Gift, ClipboardList, TrendingUp, Calendar, LogOut, ArrowRightLeft, Bell, ChevronLeft, ChevronRight,
} from "lucide-react";
import { C } from "../lib/theme.js";
import Logo from "./Logo.jsx";

function Sidebar({ active, onNavigate, profile, onLogout, hasNotifications, collapsed, onToggleCollapse }) {
  const items = [
    { key: "notifications", label: "알림", icon: Bell },
    { key: "home", label: "홈", icon: Home },
    { key: "benefits", label: "혜택 찾기", icon: Gift },
    { key: "wage", label: "임금 진단", icon: ClipboardList },
    { key: "exchange", label: "환율 인사이트", icon: TrendingUp },
    { key: "fees", label: "수수료 비교", icon: ArrowRightLeft },
    { key: "calendar", label: "캘린더", icon: Calendar },
  ];
  const activeIndex = Math.max(0, items.findIndex((it) => it.key === active));

  return (
    <div className="sidebar" style={{ position: "relative", width: collapsed ? 76 : 256, flexShrink: 0, background: "#fff", borderRight: `1px solid ${C.border}`, display: "flex", flexDirection: "column", padding: collapsed ? "26px 12px" : "26px 14px", transition: "width .25s ease, padding .25s ease" }}>
      <button
        onClick={onToggleCollapse}
        style={{ position: "absolute", top: 26, right: -12, width: 24, height: 24, borderRadius: "50%", background: "#fff", border: `1px solid ${C.border}`, display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer", zIndex: 5, color: C.textMute }}
      >
        {collapsed ? <ChevronRight size={13} /> : <ChevronLeft size={13} />}
      </button>

      <button
        onClick={() => onNavigate("home")}
        style={{ display: "flex", alignItems: "center", justifyContent: collapsed ? "center" : "flex-start", gap: 10, padding: "0 8px", marginBottom: 32, background: "none", border: "none", cursor: "pointer", textAlign: "left" }}
      >
        <Logo size={36} style={{ flexShrink: 0 }} />
        {!collapsed && <span style={{ fontSize: 22, fontWeight: 800, color: C.text, letterSpacing: "-0.02em", whiteSpace: "nowrap" }}>MOAMOA</span>}
      </button>

      <div className="sidebar-nav" style={{ position: "relative", display: "flex", flexDirection: "column", gap: 6 }}>
        <div className="sidebar-pill" style={{ transform: `translateY(${activeIndex * 54}px)` }} />
        {items.map((it) => {
          const Icon = it.icon;
          const isActive = active === it.key;
          return (
            <button
              key={it.key}
              className={`nav-item ${isActive ? "active" : ""}`}
              onClick={() => onNavigate(it.key)}
              style={collapsed ? { justifyContent: "center", padding: 0 } : undefined}
              title={collapsed ? it.label : undefined}
            >
              <span style={{ position: "relative", display: "inline-flex" }}>
                <Icon size={19} strokeWidth={isActive ? 2.4 : 2} />
                {it.key === "notifications" && hasNotifications && (
                  <span style={{ position: "absolute", top: -2, right: -2, width: 8, height: 8, borderRadius: "50%", background: C.danger, border: "1.5px solid #fff" }} />
                )}
              </span>
              {!collapsed && it.label}
            </button>
          );
        })}
      </div>

      <div className="sidebar-foot" style={{ marginTop: "auto", paddingTop: 18, borderTop: `1px solid ${C.border}` }}>
        <button
          onClick={() => onNavigate("profile")}
          title={collapsed ? profile.name || "프로필" : undefined}
          style={{ display: "flex", alignItems: "center", justifyContent: collapsed ? "center" : "flex-start", gap: 10, padding: "8px", marginBottom: 4, width: "100%", background: "none", border: "none", cursor: "pointer", textAlign: "left", borderRadius: 12 }}
        >
          <div style={{ width: 36, height: 36, borderRadius: "50%", background: C.primaryLight, color: C.primaryDark, display: "flex", alignItems: "center", justifyContent: "center", fontWeight: 800, fontSize: 15, flexShrink: 0 }}>
            {(profile.name || "?").slice(0, 1)}
          </div>
          {!collapsed && (
            <div style={{ minWidth: 0 }}>
              <p style={{ margin: 0, fontSize: 14, fontWeight: 700, color: C.text, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{profile.name || "회원"}</p>
              <p style={{ margin: 0, fontSize: 12, color: C.textMute }}>{profile.residence_region || "-"} · {profile.visa_type || "-"}</p>
            </div>
          )}
        </button>
        <button className="nav-item" onClick={onLogout} style={{ color: C.textMute, height: 42, ...(collapsed ? { justifyContent: "center", padding: 0 } : {}) }} title={collapsed ? "로그아웃" : undefined}>
          <LogOut size={17} /> {!collapsed && "로그아웃"}
        </button>
      </div>
    </div>
  );
}

export default Sidebar;

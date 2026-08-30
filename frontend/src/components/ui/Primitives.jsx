import { CheckCircle2, AlertTriangle, XCircle } from "lucide-react";
import { C, labelStyle } from "../../lib/theme.js";

function Btn({ children, onClick, variant = "primary", disabled, style }) {
  const base = { width: "100%", padding: "16px 22px", borderRadius: 14, border: "none", fontSize: 16, fontWeight: 800, cursor: disabled ? "not-allowed" : "pointer", opacity: disabled ? 0.45 : 1 };
  const variants = {
    primary: { background: C.primary, color: "#fff" },
    secondary: { background: C.primaryLight, color: C.primaryDark },
    ghost: { background: "transparent", color: C.textSub },
  };
  return (
    <button disabled={disabled} onClick={onClick} className={disabled ? "" : "btn-pop"} style={{ ...base, ...variants[variant], ...style }}>
      {children}
    </button>
  );
}

function Card({ children, onClick, style, className = "" }) {
  return (
    <div onClick={onClick} className={className} style={{ background: "#fff", border: `1px solid ${C.border}`, borderRadius: 20, padding: 20, cursor: onClick ? "pointer" : "default", ...style }}>
      {children}
    </div>
  );
}

function Badge({ level }) {
  const map = {
    정당: { bg: C.positiveBg, fg: C.positive, icon: CheckCircle2 },
    주의: { bg: C.warningBg, fg: C.warning, icon: AlertTriangle },
    의심: { bg: C.dangerBg, fg: C.danger, icon: XCircle },
  };
  const m = map[level];
  const Icon = m.icon;
  return (
    <span style={{ display: "inline-flex", alignItems: "center", gap: 4, background: m.bg, color: m.fg, fontSize: 12.5, fontWeight: 800, padding: "5px 9px", borderRadius: 9 }}>
      <Icon size={13} /> {level}
    </span>
  );
}

function Row({ icon: Icon, label, value, last }) {
  return (
    <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "12px 0", borderBottom: last ? "none" : `1px solid ${C.border}` }}>
      <span style={{ display: "flex", alignItems: "center", gap: 9, fontSize: 14.5, color: C.textSub }}><Icon size={16} /> {label}</span>
      <span style={{ fontSize: 15, fontWeight: 800, color: C.text }}>{value}</span>
    </div>
  );
}

function Field({ label, children }) {
  return (
    <div style={{ marginBottom: 15 }}>
      <label style={labelStyle}>{label}</label>
      {children}
    </div>
  );
}

export { Btn, Card, Badge, Row, Field };

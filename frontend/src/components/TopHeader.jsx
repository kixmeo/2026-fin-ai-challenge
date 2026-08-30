import { C } from "../lib/theme.js";

function TopHeader({ title, subtitle }) {
  return (
    <div style={{ padding: "36px 44px 22px", borderBottom: `1px solid ${C.border}`, flexShrink: 0, background: "#fff", position: "sticky", top: 0, zIndex: 10 }}>
      <div style={{ maxWidth: 1120, margin: "0 auto" }}>
        <h1 style={{ fontSize: 36, fontWeight: 800, color: C.text, margin: 0, letterSpacing: "-0.02em" }}>{title}</h1>
        {subtitle && <p style={{ fontSize: 15.5, color: C.textMute, margin: "8px 0 0" }}>{subtitle}</p>}
      </div>
    </div>
  );
}

export default TopHeader;

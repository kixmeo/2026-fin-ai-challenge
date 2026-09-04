import { useState } from "react";
import { C, labelStyle } from "../lib/theme.js";
import Logo from "../components/Logo.jsx";
import { Btn } from "../components/ui/Primitives.jsx";

function OnboardingPage({ onSubmit, loading }) {
  const [visaType, setVisaType] = useState("E-9");
  const [name, setName] = useState("");
  const [region, setRegion] = useState("");
  const visaOptions = ["E-9", "E-7", "H-2", "D-4", "F-4"];
  const inputStyle = { width: "100%", padding: "14px 15px", borderRadius: 12, border: `1px solid ${C.border}`, fontSize: 15.5, color: C.text, outline: "none", boxSizing: "border-box" };
  return (
    <div style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", padding: 24, background: C.surfaceDeep }}>
      <div className="pop-in" style={{ width: "100%", maxWidth: 480, background: "#fff", borderRadius: 26, border: `1px solid ${C.border}`, padding: 44 }}>
        <Logo size={48} style={{ marginBottom: 20 }} />
        <h2 style={{ fontSize: 26, fontWeight: 800, color: C.text, margin: "0 0 7px", letterSpacing: "-0.02em" }}>기본 정보 입력</h2>
        <p style={{ color: C.textMute, fontSize: 14.5, margin: "0 0 30px" }}>가입 후 딱 한 번만 입력해요. 혜택 매칭에 사용돼요.</p>

        <div style={{ marginBottom: 22 }}>
          <label style={labelStyle}>체류 자격 (비자 종류)</label>
          <div style={{ display: "flex", gap: 9, flexWrap: "wrap" }}>
            {visaOptions.map((v) => (
              <button key={v} onClick={() => setVisaType(v)} className="btn-pop" style={{ padding: "10px 16px", borderRadius: 11, border: `1.5px solid ${visaType === v ? C.primary : C.border}`, background: visaType === v ? C.primaryLight : "#fff", color: visaType === v ? C.primaryDark : C.textSub, fontWeight: 800, fontSize: 14.5, cursor: "pointer" }}>{v}</button>
            ))}
          </div>
        </div>
        <div style={{ marginBottom: 22 }}>
          <label style={labelStyle}>이름</label>
          <input style={inputStyle} value={name} onChange={(e) => setName(e.target.value)} placeholder="여권상 이름" />
        </div>
        <div style={{ marginBottom: 30 }}>
          <label style={labelStyle}>거주 지역</label>
          <input style={inputStyle} value={region} onChange={(e) => setRegion(e.target.value)} placeholder="예: 안산시" />
        </div>
        <Btn disabled={!name || !region || loading} onClick={() => onSubmit({ visa_type: visaType, name, residence_region: region })}>{loading ? "저장 중..." : "다음"}</Btn>
      </div>
    </div>
  );
}

export default OnboardingPage;

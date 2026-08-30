import { CheckCircle2, Sparkles, ChevronRight } from "lucide-react";
import { C } from "../lib/theme.js";

function LoginPage({ onLogin, loading }) {
  const features = ["혜택 매칭부터 임금 진단까지 한 곳에서", "AI가 어려운 서류·용어를 대신 정리", "마감일은 캘린더가 알아서 챙겨드려요"];
  return (
    <div style={{ height: "100vh", overflowY: "auto", scrollSnapType: "y mandatory" }}>
      <div style={{ height: "100vh", scrollSnapAlign: "start", position: "relative", overflow: "hidden", background: `linear-gradient(155deg, ${C.primary}, ${C.primaryDark})`, color: "#fff", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: 24, textAlign: "center" }}>
        <div className="blob" style={{ width: 320, height: 320, background: C.violet, opacity: 0.5, top: -100, right: -60, animation: "blobFloat 16s ease-in-out infinite" }} />
        <div className="blob" style={{ width: 240, height: 240, background: C.cyan, opacity: 0.4, bottom: -60, left: -40, animation: "blobFloat 20s ease-in-out infinite reverse" }} />

        <div className="pop-in" style={{ position: "relative", zIndex: 1, width: "100%", maxWidth: 380, display: "flex", flexDirection: "column", alignItems: "center" }}>
          <div style={{ width: 56, height: 56, borderRadius: 16, background: "rgba(255,255,255,0.18)", display: "flex", alignItems: "center", justifyContent: "center", marginBottom: 22 }}>
            <Sparkles size={28} />
          </div>
          <h1 style={{ fontSize: 56, fontWeight: 800, letterSpacing: "-0.03em", margin: "0 0 16px", lineHeight: 1 }}>모아모아</h1>
          <p style={{ fontSize: 19, fontWeight: 700, opacity: 0.95, margin: "0 0 10px", lineHeight: 1.4 }}>한국살이, 모아모아가 챙겨드릴게요</p>
          <p style={{ fontSize: 14.5, opacity: 0.78, margin: 0 }}>외국인 근로자를 위한 금융 비서</p>
        </div>

        <div className="floaty" style={{ position: "absolute", bottom: 32, left: "50%", transform: "translateX(-50%)", opacity: 0.7 }}>
          <ChevronRight size={22} style={{ transform: "rotate(90deg)" }} />
        </div>
      </div>

      <div style={{ height: "100vh", scrollSnapAlign: "start", background: "#fff", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: 24 }}>
        <div style={{ width: "100%", maxWidth: 420, display: "flex", flexDirection: "column", gap: 24, marginBottom: 44 }}>
          {features.map((f, i) => (
            <div key={i} style={{ display: "flex", alignItems: "center", gap: 16, fontSize: 17, fontWeight: 700, color: C.text }}>
              <div style={{ width: 44, height: 44, borderRadius: 13, background: C.primaryLight, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
                <CheckCircle2 size={20} color={C.primary} />
              </div>
              {f}
            </div>
          ))}
        </div>

        <div style={{ width: "100%", maxWidth: 360 }}>
          <button
            onClick={onLogin}
            disabled={loading}
            className="btn-pop"
            style={{
              width: "100%",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: 10,
              padding: "14px 20px",
              borderRadius: 13,
              border: `1px solid ${C.border}`,
              background: "#fff",
              color: C.text,
              fontSize: 15,
              fontWeight: 700,
              cursor: loading ? "not-allowed" : "pointer",
              opacity: loading ? 0.6 : 1,
              boxShadow: "0 1px 2px rgba(20,24,32,0.04)",
            }}
          >
            <GoogleIcon />
            {loading ? "로그인 중..." : "Google로 로그인"}
          </button>
          <p style={{ textAlign: "center", fontSize: 12, color: C.textMute, marginTop: 18, lineHeight: 1.5 }}>로그인하면 이용약관과 개인정보 처리방침에 동의하는 것으로 간주합니다</p>
        </div>
      </div>
    </div>
  );
}

function GoogleIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" style={{ flexShrink: 0 }}>
      <path fill="#4285F4" d="M17.64 9.2c0-.64-.06-1.25-.16-1.84H9v3.48h4.84a4.14 4.14 0 0 1-1.8 2.71v2.26h2.9c1.7-1.57 2.7-3.88 2.7-6.61z" />
      <path fill="#34A853" d="M9 18c2.43 0 4.47-.8 5.96-2.18l-2.9-2.26c-.8.54-1.84.86-3.06.86-2.35 0-4.34-1.59-5.05-3.72H.98v2.33A9 9 0 0 0 9 18z" />
      <path fill="#FBBC05" d="M3.95 10.7A5.4 5.4 0 0 1 3.67 9c0-.59.1-1.17.28-1.7V4.97H.98A9 9 0 0 0 0 9c0 1.45.35 2.83.98 4.03z" />
      <path fill="#EA4335" d="M9 3.58c1.32 0 2.5.46 3.44 1.35l2.58-2.58C13.46.89 11.43 0 9 0A9 9 0 0 0 .98 4.97l2.97 2.33C4.66 5.17 6.65 3.58 9 3.58z" />
    </svg>
  );
}

export default LoginPage;

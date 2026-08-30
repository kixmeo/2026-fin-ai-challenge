import { useState, useEffect } from "react";
import { C } from "../lib/theme.js";
import { Card } from "../components/ui/Primitives.jsx";
import { useCountUp } from "../lib/useCountUp.js";
import { api } from "../api/client.js";

function ExchangePage() {
  const currencies = [{ code: "USD", label: "미국 달러" }, { code: "PHP", label: "필리핀 페소" }, { code: "VND", label: "베트남 동" }, { code: "THB", label: "태국 바트" }];
  const [currency, setCurrency] = useState("USD");
  const [data, setData] = useState(null);
  useEffect(() => { let active = true; setData(null); api.getExchangeInsight(currency).then((res) => active && setData(res)); return () => { active = false; }; }, [currency]);

  const decimals = currency === "VND" ? 4 : 2;
  const rate = useCountUp(data ? data.current_rate : 0, 800);
  const levelColor = { high: C.danger, medium: C.warning, low: C.positive };
  const levelLabel = { high: "높음", medium: "보통", low: "낮음" };

  return (
    <div>
      <div style={{ display: "flex", gap: 9, marginBottom: 22, flexWrap: "wrap" }}>
        {currencies.map((c) => (
          <button key={c.code} onClick={() => setCurrency(c.code)} className="btn-pop" style={{ padding: "10px 18px", borderRadius: 12, border: `1.5px solid ${currency === c.code ? C.primary : C.border}`, background: currency === c.code ? C.primaryLight : "#fff", color: currency === c.code ? C.primaryDark : C.textSub, fontWeight: 800, fontSize: 14.5, cursor: "pointer" }}>{c.code} · {c.label}</button>
        ))}
      </div>

      {!data ? (
        <p style={{ color: C.textMute, fontSize: 14 }}>불러오는 중...</p>
      ) : (
        <div key={currency} className="pop-in" style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 18 }}>
          <Card className="liftable" style={{ padding: 26 }}>
            <p style={{ fontSize: 13.5, color: C.textMute, margin: 0 }}>{data.date} 기준</p>
            <p style={{ fontSize: 44, fontWeight: 800, color: C.text, margin: "10px 0 4px", letterSpacing: "-0.02em" }}>
              {rate.toLocaleString("ko-KR", { minimumFractionDigits: decimals, maximumFractionDigits: decimals })}원
            </p>
            <p style={{ fontSize: 14, color: C.textSub, margin: 0 }}>1 {data.currency} 당</p>
          </Card>
          <Card className="liftable" style={{ padding: 26 }}>
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 12 }}>
              <span style={{ fontSize: 14, color: C.textSub }}>30일 백분위</span>
              <span style={{ fontSize: 14, fontWeight: 800 }}>상위 {100 - data.percentile_30d}%</span>
            </div>
            <div style={{ height: 9, background: C.surface, borderRadius: 6, overflow: "hidden" }}>
              <div style={{ width: `${data.percentile_30d}%`, height: "100%", background: levelColor[data.volatility_level], transition: "width .8s cubic-bezier(0.16,1,0.3,1)" }} />
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: 18 }}>
              <span style={{ fontSize: 14, color: C.textSub }}>변동성</span>
              <span style={{ fontSize: 14, fontWeight: 800, color: levelColor[data.volatility_level] }}>{levelLabel[data.volatility_level]} ({data.volatility_score})</span>
            </div>
          </Card>
          <Card style={{ background: C.primaryLight, border: "none", gridColumn: "1 / -1" }}>
            <p style={{ fontSize: 15, color: C.primaryDark, lineHeight: 1.7, margin: 0 }}>{data.message}</p>
          </Card>
        </div>
      )}
    </div>
  );
}

export default ExchangePage;

import { useState, useEffect } from "react";
import { C, thStyle, tdStyle } from "../lib/theme.js";
import { Btn } from "../components/ui/Primitives.jsx";
import { won } from "../lib/format.js";
import { useCountUp } from "../lib/useCountUp.js";
import { api } from "../api/client.js";

function FeesPage() {
  const [amount, setAmount] = useState("1000000");
  const [currency, setCurrency] = useState("PHP");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const search = async () => { setLoading(true); const res = await api.getFees(Number(amount), currency); setResult(res); setLoading(false); };
  useEffect(() => { search(); /* eslint-disable-next-line */ }, []);

  return (
    <div>
      <div style={{ display: "flex", gap: 10, marginBottom: 22, flexWrap: "wrap" }}>
        <input value={amount} onChange={(e) => setAmount(e.target.value)} style={{ width: 210, padding: "12px 15px", borderRadius: 11, border: `1px solid ${C.border}`, fontSize: 15, outline: "none" }} />
        <select value={currency} onChange={(e) => setCurrency(e.target.value)} style={{ padding: "12px 13px", borderRadius: 11, border: `1px solid ${C.border}`, fontSize: 15, outline: "none", background: "#fff" }}>
          <option value="PHP">PHP</option><option value="VND">VND</option><option value="USD">USD</option><option value="THB">THB</option>
        </select>
        <Btn onClick={search} style={{ width: "auto", padding: "12px 24px" }} disabled={loading}>{loading ? "비교 중..." : "비교하기"}</Btn>
      </div>

      {result && (
        <div key={currency + amount} className="pop-in" style={{ background: "#fff", border: `1px solid ${C.border}`, borderRadius: 20, overflowX: "auto" }}>
          <table style={{ width: "100%", borderCollapse: "collapse", minWidth: 500 }}>
            <thead>
              <tr style={{ background: C.surfaceDeep }}>
                <th style={thStyle}>채널</th><th style={thStyle}>예상 수수료</th><th style={thStyle}>소요 시간</th><th style={thStyle}></th>
              </tr>
            </thead>
            <tbody>
              {result.channels.map((ch, i) => (
                <FeeRow key={i} ch={ch} isTop={i === 0} />
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function FeeRow({ ch, isTop }) {
  const fee = useCountUp(ch.fee, 700);
  return (
    <tr className="fee-row" style={{ borderTop: `1px solid ${C.border}` }}>
      <td style={tdStyle}><span style={{ fontWeight: 800, fontSize: 16 }}>{ch.name}</span></td>
      <td style={tdStyle}><span style={{ fontWeight: 800, fontSize: 19 }}>{won(Math.round(fee))}</span></td>
      <td style={{ ...tdStyle, color: C.textMute, fontSize: 14 }}>약 {ch.eta_hours}시간</td>
      <td style={tdStyle}>{isTop && <span style={{ fontSize: 12, fontWeight: 800, color: C.primary, background: C.primaryLight, padding: "4px 9px", borderRadius: 8 }}>최저 수수료</span>}</td>
    </tr>
  );
}

export default FeesPage;
export { FeeRow };

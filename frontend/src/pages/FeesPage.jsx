import { useState, useEffect } from "react";
import { C, thStyle, tdStyle } from "../lib/theme.js";
import { Btn } from "../components/ui/Primitives.jsx";
import { won } from "../lib/format.js";
import { useCountUp } from "../lib/useCountUp.js";
import { api } from "../api/client.js";

// 실시간 환율(F5) 붙기 전까지 쓰는 고정 환율 추정치 — 백엔드 FeesController.USD_TO_KRW_RATE와 동일하게 맞춤
const USD_TO_KRW_RATE = 1400;
const REPORTING_THRESHOLD_USD = 5000;

function FeesPage() {
  const [amount, setAmount] = useState("1000000");
  const [result, setResult] = useState(null);
  const [notice, setNotice] = useState(null);
  const [loading, setLoading] = useState(false);
  const search = async () => {
    const n = Number(amount);
    if (!Number.isInteger(n) || n <= 0) {
      setResult(null);
      setNotice("보낼 금액을 올바르게 입력해 주세요.");
      return;
    }
    const usdEquivalent = n / USD_TO_KRW_RATE;
    if (usdEquivalent > REPORTING_THRESHOLD_USD) {
      setResult(null);
      setNotice("송금 합계가 미화 5,000불을 초과하면 외국환거래규정에 따라 신고 또는 보고 의무가 발생할 수 있어요. 이 경우 수수료 비교 대신 은행 창구에서 별도로 확인해 주세요.");
      return;
    }
    setLoading(true);
    try {
      const res = await api.getFees(n);
      if (res.channels.length === 0) {
        setResult(null);
        setNotice("입력하신 금액으로는 수수료(전신료 포함)가 보내는 금액보다 커서 비교할 채널이 없어요. 더 큰 금액을 입력해 주세요.");
      } else {
        setNotice(null);
        setResult(res);
      }
    } catch (err) {
      console.error("[FeesPage] getFees failed:", err);
      setResult(null);
      setNotice("수수료 비교 중 오류가 발생했어요. 잠시 후 다시 시도해 주세요.");
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => { search(); /* eslint-disable-next-line */ }, []);

  return (
    <div>
      <p style={{ color: C.textMute, fontSize: 13, marginTop: -10, marginBottom: 18 }}>
        생활비 송금 기준 비교예요. 고액 송금은 은행별 별도 절차가 있을 수 있어요.
      </p>
      <div style={{ display: "flex", gap: 10, marginBottom: 22, flexWrap: "wrap" }}>
        <input type="number" min="1" value={amount} onChange={(e) => setAmount(e.target.value)} placeholder="보낼 금액 (원)" style={{ width: 210, padding: "12px 15px", borderRadius: 11, border: `1px solid ${C.border}`, fontSize: 15, outline: "none" }} />
        <Btn onClick={search} style={{ width: "auto", padding: "12px 24px" }} disabled={loading}>{loading ? "비교 중..." : "비교하기"}</Btn>
      </div>

      {notice && (
        <div style={{ background: C.warningBg, border: `1px solid ${C.warning}`, borderRadius: 16, padding: "16px 18px", color: C.text, fontSize: 14, lineHeight: 1.6 }}>
          {notice}
        </div>
      )}

      {result && (
        <div key={amount} className="pop-in" style={{ background: "#fff", border: `1px solid ${C.border}`, borderRadius: 20, overflowX: "auto" }}>
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

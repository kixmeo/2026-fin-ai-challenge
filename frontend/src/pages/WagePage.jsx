import { useState } from "react";
import { ClipboardList, Trash2, Plus, AlertTriangle } from "lucide-react";
import { C } from "../lib/theme.js";
import { Card, Field, Btn, Badge } from "../components/ui/Primitives.jsx";
import { won } from "../lib/format.js";
import { useCountUp } from "../lib/useCountUp.js";
import { api } from "../api/client.js";

function WagePage() {
  const [form, setForm] = useState({
    base_wage: "2100000",
    work_hours_per_week: "40",
    overtime_hours: "8",
    overtime_pay: "60000",
    deductions: [{ name: "국민연금", amount: "94500" }, { name: "숙식비", amount: "300000" }],
  });
  const [result, setResult] = useState(null);
  const [resultKey, setResultKey] = useState(0);
  const [loading, setLoading] = useState(false);
  const inputStyle = { width: "100%", padding: "12px 14px", borderRadius: 11, border: `1px solid ${C.border}`, fontSize: 15, outline: "none", boxSizing: "border-box" };

  const hourly = useCountUp(result ? result.minimum_wage_check.hourly_wage : 0, 800);
  const expected = useCountUp(result ? result.overtime_check.expected : 0, 800);
  const actual = useCountUp(result ? result.overtime_check.actual : 0, 800);

  const setDeduction = (i, key, val) => { const next = [...form.deductions]; next[i] = { ...next[i], [key]: val }; setForm({ ...form, deductions: next }); };
  const addDeduction = () => setForm({ ...form, deductions: [...form.deductions, { name: "", amount: "" }] });
  const removeDeduction = (i) => setForm({ ...form, deductions: form.deductions.filter((_, idx) => idx !== i) });
  const submit = async () => {
    setLoading(true);
    try {
      const res = await api.postWageCheck(form);
      setResult(res);
      setResultKey((k) => k + 1);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="grid-wage">
      <Card style={{ padding: 24 }}>
        <p style={{ fontWeight: 800, fontSize: 17, margin: "0 0 5px" }}>급여 정보 입력</p>
        <p style={{ fontSize: 13.5, color: C.textMute, margin: "0 0 20px" }}>급여명세서를 보고 그대로 입력해보세요</p>
        <Field label="기본급 (월, 원)"><input style={inputStyle} value={form.base_wage} onChange={(e) => setForm({ ...form, base_wage: e.target.value })} /></Field>
        <Field label="주당 근무시간"><input style={inputStyle} value={form.work_hours_per_week} onChange={(e) => setForm({ ...form, work_hours_per_week: e.target.value })} /></Field>
        <Field label="월 연장근로시간"><input style={inputStyle} value={form.overtime_hours} onChange={(e) => setForm({ ...form, overtime_hours: e.target.value })} /></Field>
        <Field label="연장근로수당 (실지급, 원)"><input style={inputStyle} value={form.overtime_pay} onChange={(e) => setForm({ ...form, overtime_pay: e.target.value })} /></Field>

        <p style={{ fontSize: 14, fontWeight: 800, color: C.text, margin: "18px 0 12px" }}>공제 내역</p>
        {form.deductions.map((d, i) => (
          <div key={i} style={{ display: "flex", gap: 8, marginBottom: 9, alignItems: "center" }}>
            <input style={{ ...inputStyle, flex: 1.2 }} placeholder="항목명" value={d.name} onChange={(e) => setDeduction(i, "name", e.target.value)} />
            <input style={{ ...inputStyle, flex: 1 }} placeholder="금액" value={d.amount} onChange={(e) => setDeduction(i, "amount", e.target.value)} />
            <button onClick={() => removeDeduction(i)} style={{ background: "none", border: "none", cursor: "pointer", padding: 4 }}><Trash2 size={16} color={C.textMute} /></button>
          </div>
        ))}
        <button onClick={addDeduction} style={{ display: "flex", alignItems: "center", gap: 6, background: "none", border: "none", color: C.primary, fontSize: 14, fontWeight: 800, cursor: "pointer", padding: "6px 0 20px" }}>
          <Plus size={15} /> 공제 항목 추가
        </button>
        <Btn onClick={submit} disabled={loading}>{loading ? "진단 중..." : "진단하기"}</Btn>
      </Card>

      {!result ? (
        <div style={{ display: "flex", alignItems: "center", justifyContent: "center", minHeight: 440, border: `1.5px dashed ${C.border}`, borderRadius: 24, color: C.textMute, flexDirection: "column", gap: 12 }}>
          <ClipboardList size={32} style={{ opacity: 0.4 }} />
          <p style={{ fontSize: 14.5 }}>왼쪽 정보를 입력하고 진단하기를 눌러주세요</p>
        </div>
      ) : (
        <div key={resultKey} className="pop-in">
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14, marginBottom: 14 }}>
            <Card className="liftable">
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <span style={{ fontSize: 14.5, fontWeight: 800 }}>최저임금 기준</span>
                <Badge level={result.minimum_wage_check.pass ? "정당" : "의심"} />
              </div>
              <p style={{ fontSize: 13.5, color: C.textSub, marginTop: 12, lineHeight: 1.7 }}>시급 {won(Math.round(hourly))}<br />2026년 최저시급 {won(result.minimum_wage_check.minimum_wage_2026)}</p>
              <p style={{ fontSize: 12.5, color: result.minimum_wage_check.pass ? C.textMute : C.danger, marginTop: 8, lineHeight: 1.6 }}>{result.minimum_wage_check.reason}</p>
            </Card>
            <Card className="liftable">
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <span style={{ fontSize: 14.5, fontWeight: 800 }}>연장수당</span>
                <Badge level={result.overtime_check.pass ? "정당" : "의심"} />
              </div>
              <p style={{ fontSize: 13.5, color: C.textSub, marginTop: 12, lineHeight: 1.7 }}>예상 {won(Math.round(expected))}<br />실지급 {won(Math.round(actual))}</p>
              <p style={{ fontSize: 12.5, color: result.overtime_check.pass ? C.textMute : C.danger, marginTop: 8, lineHeight: 1.6 }}>{result.overtime_check.reason}</p>
            </Card>
          </div>

          <p style={{ fontSize: 15, fontWeight: 800, color: C.text, margin: "20px 0 12px" }}>공제 항목 분석</p>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(240px, 1fr))", gap: 12 }}>
            {result.deduction_flags.map((d, i) => (
              <Card key={i} className="liftable">
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 7 }}>
                  <span style={{ fontSize: 15, fontWeight: 800 }}>{d.name}</span>
                  <Badge level={d.level} />
                </div>
                <p style={{ fontSize: 14, color: C.textSub, margin: "0 0 5px" }}>{won(d.amount)}</p>
                <p style={{ fontSize: 12.5, color: C.textMute, margin: 0 }}>{d.reason}</p>
              </Card>
            ))}
          </div>

          {result.has_suspicious && (
            <div style={{ display: "flex", gap: 12, alignItems: "flex-start", background: C.dangerBg, border: `1px solid ${C.danger}33`, borderRadius: 14, padding: 16, marginTop: 18 }}>
              <AlertTriangle size={20} color={C.danger} style={{ flexShrink: 0, marginTop: 1 }} />
              <p style={{ fontSize: 13.5, color: C.danger, lineHeight: 1.7, margin: 0, fontWeight: 600 }}>{result.consultation_notice}</p>
            </div>
          )}
          <div style={{ background: C.surface, borderRadius: 14, padding: 16, marginTop: 12, fontSize: 12.5, color: C.textMute, lineHeight: 1.7 }}>{result.disclaimer}</div>
          <div style={{ marginTop: 16 }}>
            <Btn variant="secondary" onClick={() => setResult(null)} style={{ width: "auto", padding: "12px 22px" }}>다시 진단하기</Btn>
          </div>
        </div>
      )}
    </div>
  );
}

export default WagePage;

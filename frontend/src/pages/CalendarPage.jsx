import { useState } from "react";
import { ChevronLeft, ChevronRight, Calendar, X } from "lucide-react";
import { C } from "../lib/theme.js";
import { Card, Btn } from "../components/ui/Primitives.jsx";

function getMonthMatrix(year, month) {
  const first = new Date(year, month, 1);
  const startDay = first.getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const cells = [];
  for (let i = 0; i < startDay; i++) cells.push(null);
  for (let d = 1; d <= daysInMonth; d++) cells.push(d);
  while (cells.length % 7 !== 0) cells.push(null);
  const weeks = [];
  for (let i = 0; i < cells.length; i += 7) weeks.push(cells.slice(i, i + 7));
  return weeks;
}

function CalendarPage({ events, onAdd, onDelete }) {
  const today = new Date();
  const [viewYear, setViewYear] = useState(today.getFullYear());
  const [viewMonth, setViewMonth] = useState(today.getMonth());
  const [newTitle, setNewTitle] = useState("");
  const [newDate, setNewDate] = useState("");
  const pad = (n) => String(n).padStart(2, "0");
  const dateKey = (d) => `${viewYear}-${pad(viewMonth + 1)}-${pad(d)}`;
  const weeks = getMonthMatrix(viewYear, viewMonth);
  const eventsByDate = {};
  events.forEach((ev) => { (eventsByDate[ev.date] = eventsByDate[ev.date] || []).push(ev); });
  const isToday = (d) => d && viewYear === today.getFullYear() && viewMonth === today.getMonth() && d === today.getDate();
  const changeMonth = (delta) => {
    let m = viewMonth + delta, y = viewYear;
    if (m < 0) { m = 11; y -= 1; } else if (m > 11) { m = 0; y += 1; }
    setViewMonth(m); setViewYear(y);
  };
  const upcoming = [...events].sort((a, b) => a.date.localeCompare(b.date));
  const submitAdd = () => { if (!newTitle.trim() || !newDate) return; onAdd(newTitle.trim(), newDate); setNewTitle(""); setNewDate(""); };
  const smallInput = { width: "100%", padding: "11px 13px", borderRadius: 11, border: `1px solid ${C.border}`, fontSize: 14.5, outline: "none", boxSizing: "border-box" };

  return (
    <div>
      <div className="grid-calendar">
        <Card key={`${viewYear}-${viewMonth}`} className="pop-in" style={{ padding: 24 }}>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 18 }}>
            <button className="icon-btn" onClick={() => changeMonth(-1)}><ChevronLeft size={18} /></button>
            <p style={{ fontWeight: 800, fontSize: 18, color: C.text, margin: 0 }}>{viewYear}년 {viewMonth + 1}월</p>
            <button className="icon-btn" onClick={() => changeMonth(1)}><ChevronRight size={18} /></button>
          </div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(7,1fr)", textAlign: "center", fontSize: 12.5, color: C.textMute, marginBottom: 8 }}>
            {["일", "월", "화", "수", "목", "금", "토"].map((d) => <div key={d}>{d}</div>)}
          </div>
          {weeks.map((week, wi) => (
            <div key={wi} style={{ display: "grid", gridTemplateColumns: "repeat(7,1fr)", gap: 6, marginBottom: 6 }}>
              {week.map((d, di) => {
                const key = d ? dateKey(d) : null;
                const dayEvents = key ? eventsByDate[key] || [] : [];
                return (
                  <div key={di} style={{ minHeight: 76, borderRadius: 12, padding: 7, background: d ? C.surfaceDeep : "transparent", border: isToday(d) ? `1.5px solid ${C.primary}` : "1px solid transparent" }}>
                    {d && <span style={{ fontSize: 13, fontWeight: isToday(d) ? 800 : 600, color: isToday(d) ? C.primary : C.text }}>{d}</span>}
                    {dayEvents.slice(0, 2).map((ev) => (
                      <div key={ev.id} style={{ marginTop: 4, fontSize: 10.5, background: C.primaryLight, color: C.primaryDark, borderRadius: 6, padding: "2px 5px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{ev.title}</div>
                    ))}
                  </div>
                );
              })}
            </div>
          ))}
        </Card>

        <Card>
          <p style={{ fontWeight: 800, fontSize: 14.5, margin: "0 0 14px" }}>일정 직접 추가</p>
          <input placeholder="일정 제목" value={newTitle} onChange={(e) => setNewTitle(e.target.value)} style={smallInput} />
          <input type="date" value={newDate} onChange={(e) => setNewDate(e.target.value)} style={{ ...smallInput, marginTop: 9 }} />
          <div style={{ marginTop: 11 }}><Btn onClick={submitAdd}>추가하기</Btn></div>
        </Card>
      </div>

      <p style={{ fontWeight: 800, fontSize: 18, color: C.text, margin: "30px 0 14px" }}>다가오는 일정</p>
      {upcoming.length === 0 ? (
        <div style={{ textAlign: "center", padding: "54px 20px", color: C.textMute, border: `1.5px dashed ${C.border}`, borderRadius: 18 }}>
          <Calendar size={30} style={{ marginBottom: 12, opacity: 0.5 }} />
          <p style={{ fontSize: 14.5 }}>등록된 일정이 없어요. 혜택 상세에서 마감일을 등록하거나 직접 추가해보세요.</p>
        </div>
      ) : (
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(260px, 1fr))", gap: 12 }}>
          {upcoming.map((ev) => (
            <Card key={ev.id} className="liftable" style={{ display: "flex", alignItems: "center", gap: 13 }}>
              <div style={{ width: 42, height: 42, borderRadius: 11, background: C.primaryLight, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
                <Calendar size={19} color={C.primary} />
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <p style={{ margin: 0, fontWeight: 800, fontSize: 15, color: C.text, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{ev.title}</p>
                <p style={{ margin: "3px 0 0", fontSize: 12.5, color: C.textMute }}>마감 {ev.date}</p>
              </div>
              <button onClick={() => onDelete(ev.id)} style={{ background: "none", border: "none", cursor: "pointer", padding: 4, flexShrink: 0 }}>
                <X size={17} color={C.textMute} />
              </button>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

export default CalendarPage;

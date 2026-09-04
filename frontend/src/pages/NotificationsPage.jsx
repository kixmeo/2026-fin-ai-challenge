import { Bell } from "lucide-react";
import { C } from "../lib/theme.js";
import { Card } from "../components/ui/Primitives.jsx";

function NotificationsPage({ events }) {
  const items = [...events]
    .map((ev) => ({ ...ev, days: Math.ceil((new Date(ev.date) - new Date()) / 86400000) }))
    .sort((a, b) => a.days - b.days);

  return (
    <div>
      {items.length === 0 ? (
        <div style={{ textAlign: "center", padding: "80px 20px", color: C.textMute, border: `1.5px dashed ${C.border}`, borderRadius: 18 }}>
          <Bell size={30} style={{ marginBottom: 12, opacity: 0.5 }} />
          <p style={{ fontSize: 14.5 }}>아직 알림이 없어요. 캘린더에 일정을 등록해두면<br />마감일이 다가올 때 여기서 알려드려요.</p>
        </div>
      ) : (
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          {items.map((item) => {
            const urgent = item.days <= 7;
            return (
              <Card key={item.id} className="liftable" style={{ display: "flex", alignItems: "center", gap: 14 }}>
                <div style={{ width: 44, height: 44, borderRadius: 12, background: urgent ? C.dangerBg : C.primaryLight, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
                  <Bell size={20} color={urgent ? C.danger : C.primary} />
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <p style={{ margin: 0, fontWeight: 800, fontSize: 15, color: C.text }}>{item.title} 마감이 {item.days >= 0 ? `D-${item.days}` : "지났어요"}</p>
                  <p style={{ margin: "3px 0 0", fontSize: 12.5, color: C.textMute }}>{item.date}까지</p>
                </div>
                {urgent && <span style={{ fontSize: 11, fontWeight: 800, color: C.danger, background: C.dangerBg, padding: "4px 9px", borderRadius: 8, flexShrink: 0 }}>곧 마감</span>}
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default NotificationsPage;

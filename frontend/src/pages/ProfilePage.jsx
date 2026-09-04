import { ClipboardList, Home, MessageCircle } from "lucide-react";
import { C } from "../lib/theme.js";
import { Card, Row } from "../components/ui/Primitives.jsx";

function ProfilePage({ profile }) {
  const rows = [
    { icon: ClipboardList, label: "체류 자격", value: profile.visa_type || "-" },
    { icon: Home, label: "거주 지역", value: profile.residence_region || "-" },
  ];
  if (profile.email) rows.push({ icon: MessageCircle, label: "이메일", value: profile.email });

  return (
    <div style={{ maxWidth: 480 }}>
      <Card className="pop-in" style={{ padding: 28 }}>
        <div style={{ width: 64, height: 64, borderRadius: "50%", background: C.primaryLight, color: C.primaryDark, display: "flex", alignItems: "center", justifyContent: "center", fontWeight: 800, fontSize: 26, marginBottom: 18 }}>
          {(profile.name || "?").slice(0, 1)}
        </div>
        <p style={{ fontSize: 20, fontWeight: 800, color: C.text, margin: "0 0 22px" }}>{profile.name || "회원"}</p>
        {rows.map((r, i) => (
          <Row key={r.label} icon={r.icon} label={r.label} value={r.value} last={i === rows.length - 1} />
        ))}
      </Card>
    </div>
  );
}

export default ProfilePage;

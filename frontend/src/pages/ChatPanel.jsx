import { useState, useRef, useEffect } from "react";
import { Send } from "lucide-react";
import { C } from "../lib/theme.js";
import { api } from "../api/client.js";

function ChatPanel({ onComplete, sessionId }) {
  const [messages, setMessages] = useState([{ role: "ai", text: "안녕하세요! 딱 2가지만 더 여쭤보고 맞춤 혜택을 찾아드릴게요.\n먼저, 월급은 얼마 정도 받으세요?" }]);
  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);
  const bottomRef = useRef(null);
  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: "smooth" }); }, [messages]);

  // 진행 단계는 로컬 상태로 따로 안 두고, 지금까지 보낸 메시지 개수로 유도해요.
  // (실제 대화 상태는 서버가 session_id로 관리하므로, 여기선 표시용으로만 씀)
  const stepDisplay = Math.min(messages.filter((m) => m.role === "user").length + 1, 2);

  const send = async () => {
    if (!input.trim() || sending) return;
    const userMsg = input.trim();
    setMessages((m) => [...m, { role: "user", text: userMsg }]);
    setInput("");
    setSending(true);
    const res = await api.postChatMessage(sessionId, userMsg);
    setMessages((m) => [...m, { role: "ai", text: res.reply }]);
    setSending(false);
    if (res.is_complete) setTimeout(() => onComplete(res.extracted_profile), 500);
  };

  return (
    <div className="pop-in" style={{ maxWidth: 660, margin: "0 auto" }}>
      <div style={{ background: "#fff", border: `1px solid ${C.border}`, borderRadius: 24, overflow: "hidden", display: "flex", flexDirection: "column", height: 580 }}>
        <div style={{ padding: "16px 22px", borderBottom: `1px solid ${C.border}`, display: "flex", alignItems: "center", gap: 12 }}>
          <span style={{ fontSize: 12.5, fontWeight: 800, color: C.primary }}>{stepDisplay}/2단계</span>
          <div style={{ flex: 1, height: 5, background: C.surface, borderRadius: 3, overflow: "hidden" }}>
            <div style={{ width: `${(stepDisplay / 2) * 100}%`, height: "100%", background: C.primary, borderRadius: 3, transition: "width .4s cubic-bezier(0.16,1,0.3,1)" }} />
          </div>
        </div>
        <div style={{ flex: 1, overflowY: "auto", padding: 22 }}>
          {messages.map((m, i) => (
            <div key={i} style={{ display: "flex", justifyContent: m.role === "user" ? "flex-end" : "flex-start", marginBottom: 13 }}>
              <div style={{ maxWidth: "76%", padding: "13px 17px", borderRadius: 17, borderBottomRightRadius: m.role === "user" ? 4 : 17, borderBottomLeftRadius: m.role === "ai" ? 4 : 17, background: m.role === "user" ? C.primary : C.surfaceDeep, color: m.role === "user" ? "#fff" : C.text, fontSize: 15.5, lineHeight: 1.6, whiteSpace: "pre-line" }}>
                {m.text}
              </div>
            </div>
          ))}
          {sending && <p style={{ color: C.textMute, fontSize: 14 }}>답변을 정리하고 있어요...</p>}
          <div ref={bottomRef} />
        </div>
        <div style={{ display: "flex", gap: 9, padding: 17, borderTop: `1px solid ${C.border}` }}>
          <input value={input} onChange={(e) => setInput(e.target.value)} onKeyDown={(e) => e.key === "Enter" && send()} placeholder="메시지를 입력하세요" style={{ flex: 1, padding: "13px 15px", borderRadius: 13, border: `1px solid ${C.border}`, outline: "none", fontSize: 15.5 }} />
          <button onClick={send} className="btn-pop" style={{ width: 46, height: 46, borderRadius: 13, background: C.primary, border: "none", display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer", flexShrink: 0 }}>
            <Send size={19} color="#fff" />
          </button>
        </div>
      </div>
    </div>
  );
}

export default ChatPanel;

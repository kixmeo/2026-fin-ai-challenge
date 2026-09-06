-- chat_sessions는 사용자별 개인 데이터(소득 등)라 profiles(V2)/calendar_events(V5)와 같은 이유로 보호함.
ALTER TABLE chat_sessions ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON chat_sessions FROM anon, authenticated;

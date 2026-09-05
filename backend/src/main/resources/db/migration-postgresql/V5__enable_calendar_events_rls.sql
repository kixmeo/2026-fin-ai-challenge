-- calendar_events는 사용자별 개인 일정(신청 마감일 등)이라 profiles(V2)와 같은 이유로 보호함.
-- 모든 쓰기는 이 백엔드(postgres 역할, RLS를 우회함)를 통해서만 이루어지므로 정책 없이 RLS만 켜서 전면 차단함.
ALTER TABLE calendar_events ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON calendar_events FROM anon, authenticated;

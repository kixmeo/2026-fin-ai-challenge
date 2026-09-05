-- profiles는 Supabase가 public 스키마에 anon/authenticated 권한을 자동으로 부여하는 테이블이라,
-- RLS 없이는 프론트가 이미 갖고 있는 anon key로 누구나 PostgREST를 통해 다른 사용자의 프로필을 읽고 쓸 수 있음.
-- 모든 쓰기는 이 백엔드(postgres 역할, RLS를 우회함)를 통해서만 이루어지므로 정책 없이 RLS만 켜서 전면 차단함.
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON profiles FROM anon, authenticated;

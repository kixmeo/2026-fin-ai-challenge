-- fee_channels/fee_tiers는 쓰기가 노출되면 안 되는 데이터(수수료 조작 가능)라 profiles(V2)/calendar_events(V5)와 같은 이유로 보호함.
-- 모든 쓰기는 이 백엔드(postgres 역할, RLS를 우회함)를 통해서만 이루어지므로 정책 없이 RLS만 켜서 전면 차단함.
ALTER TABLE fee_channels ENABLE ROW LEVEL SECURITY;
ALTER TABLE fee_tiers ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON fee_channels, fee_tiers FROM anon, authenticated;

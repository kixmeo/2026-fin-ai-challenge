-- fee_channels(V7)와 같은 이유: 사용자별 데이터가 아니고 쓰기가 노출되면 안 되는 공유 참조 데이터.
-- 모든 쓰기는 이 백엔드(postgres 역할, RLS를 우회함)를 통해서만 이루어지므로 정책 없이 RLS만 켜서 전면 차단함.
ALTER TABLE exchange_rate_snapshots ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON exchange_rate_snapshots FROM anon, authenticated;

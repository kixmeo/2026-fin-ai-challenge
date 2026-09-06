-- F2/F3 혜택 매칭 후보 데이터. eligibility_rule은 백엔드가 내부를 조회하지 않고
-- AI 서버(/ai/benefits/score)에 그대로 실어보내기만 하는 불투명한 JSON 문자열이라 VARCHAR로 저장함.
CREATE TABLE benefit_candidates (
    benefit_id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    amount BIGINT NOT NULL,
    deadline DATE NOT NULL,
    required_docs_count INT NOT NULL,
    eligibility_rule VARCHAR(2000) NOT NULL
);

INSERT INTO benefit_candidates (benefit_id, title, amount, deadline, required_docs_count, eligibility_rule) VALUES
('b_017', '외국인근로자 귀국비용보험', 500000, '2026-09-30', 2, '{"visa_types":["E-9","H-2"],"min_work_period_months":0}'),
('b_021', '외국인근로자 국민연금 반환일시금', 1200000, '2026-11-15', 3, '{"visa_types":["E-9","H-2","D-4","F-4"],"min_work_period_months":12}'),
('b_009', '산업재해 소액치료비 지원', 300000, '2026-10-05', 1, '{"visa_types":["E-9","H-2","D-4","F-4"],"min_work_period_months":0}');

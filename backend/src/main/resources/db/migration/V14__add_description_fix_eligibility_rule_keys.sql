-- F3(설명) 기능이 AI 서버(/ai/benefits/explain)에 source_documents로 보낼 본문.
-- 지금은 실제 공고 원문이 없어 자리표시 텍스트만 채움 - 콘텐츠 담당자가 실제 문서로 교체해야 함.
ALTER TABLE benefit_candidates ADD COLUMN description VARCHAR(2000);

UPDATE benefit_candidates
SET description = title || '에 대한 상세 안내 자료가 아직 등록되지 않았습니다.'
WHERE description IS NULL;

-- eligibility_rule의 필드명을 AI 서버 실제 스키마(EligibilityRule: visa_type_in, work_period_min_months)에 맞게 수정.
-- 기존 값은 AI가 인식하지 못하는 필드명(visa_types, min_work_period_months)으로 저장되어 있었음.
UPDATE benefit_candidates
SET eligibility_rule = '{"visa_type_in":["E-9","H-2"],"work_period_min_months":0}'
WHERE benefit_id = 'b_017';

UPDATE benefit_candidates
SET eligibility_rule = '{"visa_type_in":["E-9","H-2","D-4","F-4"],"work_period_min_months":12}'
WHERE benefit_id = 'b_021';

UPDATE benefit_candidates
SET eligibility_rule = '{"visa_type_in":["E-9","H-2","D-4","F-4"],"work_period_min_months":0}'
WHERE benefit_id = 'b_009';

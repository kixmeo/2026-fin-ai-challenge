-- "사용자당 진행 중인 세션은 최대 1개"가 findFirstByUserId라는 이름 뒤에 숨어서 애플리케이션 코드로만 지켜지고 있었음
-- (동시 요청 시 중복 행 생성 가능) - 스키마 레벨에서 강제하도록 유니크 제약으로 교체
DROP INDEX idx_chat_sessions_user_id;
ALTER TABLE chat_sessions ADD CONSTRAINT uq_chat_sessions_user_id UNIQUE (user_id);

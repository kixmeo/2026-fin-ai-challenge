-- F1 대화형 정보수집의 턴 간 누적 상태. AI 서버가 무상태라 이 값들을 백엔드가 들고 있어야 함.
CREATE TABLE chat_sessions (
    session_id VARCHAR(64) PRIMARY KEY,
    user_id UUID NOT NULL,
    income BIGINT,
    work_period INT
);

CREATE INDEX idx_chat_sessions_user_id ON chat_sessions (user_id);

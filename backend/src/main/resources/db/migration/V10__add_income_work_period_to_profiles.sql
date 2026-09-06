-- F1(대화형 정보수집) 완료 시 채워지는 값들 - 가입 직후엔 비어있다가 나중에 채워지므로 nullable
ALTER TABLE profiles ADD COLUMN income BIGINT;
ALTER TABLE profiles ADD COLUMN work_period INT;

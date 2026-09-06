-- V3의 "통화별 고정 비율" 구조를 실제 은행 수수료 체계(미화 환산액 구간별 정액 수수료 + 건당 전신료)로 교체함.
-- MVP 범위: 인터넷(온라인) 채널만 반영, 창구(방문) 채널은 제외.
-- 은행마다 구간 개수/기준점이 달라서 구간을 별도 테이블로 분리함.
DROP TABLE fee_channels;

CREATE TABLE fee_channels (
    id UUID PRIMARY KEY,
    bank_name VARCHAR(255) NOT NULL,
    channel_type VARCHAR(50) NOT NULL,
    wire_fee BIGINT NOT NULL,
    eta_hours INT NOT NULL
);

CREATE TABLE fee_tiers (
    id UUID PRIMARY KEY,
    fee_channel_id UUID NOT NULL REFERENCES fee_channels(id),
    max_usd_amount INT,
    fee BIGINT NOT NULL
);

CREATE INDEX idx_fee_tiers_fee_channel_id ON fee_tiers(fee_channel_id);

-- 신한은행 인터넷
INSERT INTO fee_channels (id, bank_name, channel_type, wire_fee, eta_hours) VALUES
    ('b1000000-0000-0000-0000-000000000002', '신한은행', '인터넷', 8000, 2);
INSERT INTO fee_tiers (id, fee_channel_id, max_usd_amount, fee) VALUES
    ('c1000000-0000-0000-0000-000000000006', 'b1000000-0000-0000-0000-000000000002', 500, 2500),
    ('c1000000-0000-0000-0000-000000000007', 'b1000000-0000-0000-0000-000000000002', 2000, 5000),
    ('c1000000-0000-0000-0000-000000000008', 'b1000000-0000-0000-0000-000000000002', 5000, 7500),
    ('c1000000-0000-0000-0000-000000000009', 'b1000000-0000-0000-0000-000000000002', 20000, 10000),
    ('c1000000-0000-0000-0000-000000000010', 'b1000000-0000-0000-0000-000000000002', NULL, 12500);

-- 우리은행 인터넷 (이미 개인고객 우대율 반영된 최종 수수료, 전신료는 별도 통지 시까지 면제라 0으로 반영)
INSERT INTO fee_channels (id, bank_name, channel_type, wire_fee, eta_hours) VALUES
    ('b1000000-0000-0000-0000-000000000004', '우리은행', '인터넷', 0, 2);
INSERT INTO fee_tiers (id, fee_channel_id, max_usd_amount, fee) VALUES
    ('c1000000-0000-0000-0000-000000000016', 'b1000000-0000-0000-0000-000000000004', 500, 2500),
    ('c1000000-0000-0000-0000-000000000017', 'b1000000-0000-0000-0000-000000000004', 3000, 5000),
    ('c1000000-0000-0000-0000-000000000018', 'b1000000-0000-0000-0000-000000000004', 5000, 7500),
    ('c1000000-0000-0000-0000-000000000019', 'b1000000-0000-0000-0000-000000000004', NULL, 10000);

-- 하나은행 인터넷
INSERT INTO fee_channels (id, bank_name, channel_type, wire_fee, eta_hours) VALUES
    ('b1000000-0000-0000-0000-000000000006', '하나은행', '인터넷', 5000, 2);
INSERT INTO fee_tiers (id, fee_channel_id, max_usd_amount, fee) VALUES
    ('c1000000-0000-0000-0000-000000000025', 'b1000000-0000-0000-0000-000000000006', 5000, 3000),
    ('c1000000-0000-0000-0000-000000000026', 'b1000000-0000-0000-0000-000000000006', NULL, 5000);

-- KB국민은행 인터넷 (ATM 해외송금 면제 등 별도 채널은 미모델링)
INSERT INTO fee_channels (id, bank_name, channel_type, wire_fee, eta_hours) VALUES
    ('b1000000-0000-0000-0000-000000000008', 'KB국민은행', '인터넷', 5000, 2);
INSERT INTO fee_tiers (id, fee_channel_id, max_usd_amount, fee) VALUES
    ('c1000000-0000-0000-0000-000000000032', 'b1000000-0000-0000-0000-000000000008', 5000, 3000),
    ('c1000000-0000-0000-0000-000000000033', 'b1000000-0000-0000-0000-000000000008', NULL, 5000);

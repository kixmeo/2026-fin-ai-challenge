CREATE TABLE fee_channels (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    fee_rate DECIMAL(6, 4) NOT NULL,
    eta_hours INT NOT NULL
);

INSERT INTO fee_channels (id, name, currency, fee_rate, eta_hours) VALUES
    ('a1a1a1a1-0001-0001-0001-000000000001', 'A은행 전신송금', 'PHP', 0.0150, 24),
    ('a1a1a1a1-0001-0001-0001-000000000002', 'B 핀테크 송금', 'PHP', 0.0045, 2),
    ('a1a1a1a1-0001-0001-0001-000000000003', 'A은행 전신송금', 'VND', 0.0150, 24),
    ('a1a1a1a1-0001-0001-0001-000000000004', 'B 핀테크 송금', 'VND', 0.0045, 2),
    ('a1a1a1a1-0001-0001-0001-000000000005', 'A은행 전신송금', 'USD', 0.0120, 24),
    ('a1a1a1a1-0001-0001-0001-000000000006', 'B 핀테크 송금', 'USD', 0.0035, 2);

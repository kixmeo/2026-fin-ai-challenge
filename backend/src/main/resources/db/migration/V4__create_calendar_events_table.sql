CREATE TABLE calendar_events (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    event_date DATE NOT NULL
);

CREATE INDEX idx_calendar_events_user_id ON calendar_events(user_id);

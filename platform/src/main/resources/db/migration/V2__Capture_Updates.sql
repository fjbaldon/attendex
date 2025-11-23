ALTER TABLE capture_entry
    ADD COLUMN scan_uuid VARCHAR(36);

CREATE UNIQUE INDEX idx_capture_entry_scan_uuid ON capture_entry (scan_uuid);

ALTER TABLE capture_entry
    ALTER COLUMN session_id DROP NOT NULL;

ALTER TABLE capture_entry
    ADD COLUMN event_id BIGINT;

CREATE INDEX idx_capture_entry_event_timestamp ON capture_entry (event_id, scan_timestamp DESC);

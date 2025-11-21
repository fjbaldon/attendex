-- 1. Add scan_uuid column and make it unique to prevent duplicate syncs
ALTER TABLE capture_entry
    ADD COLUMN scan_uuid VARCHAR(36);

CREATE UNIQUE INDEX idx_capture_entry_scan_uuid ON capture_entry (scan_uuid);

-- 2. Make session_id nullable (Thin Client doesn't send it)
ALTER TABLE capture_entry
    ALTER COLUMN session_id DROP NOT NULL;

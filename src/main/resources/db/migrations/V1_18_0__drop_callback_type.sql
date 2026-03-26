ALTER TABLE virus_scan_callback
DROP
COLUMN type;

ALTER TABLE virus_scan_callback
ALTER COLUMN encoded_callback_data
TYPE VARCHAR(1000);

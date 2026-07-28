ALTER TABLE ownership_link
    ALTER COLUMN created_date TYPE TIMESTAMPTZ(6)
        USING created_date AT TIME ZONE 'UTC',
    ALTER COLUMN last_modified_date TYPE TIMESTAMPTZ(6)
        USING last_modified_date AT TIME ZONE 'UTC';

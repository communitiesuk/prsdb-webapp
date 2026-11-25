ALTER TABLE letting_agent_access
    ADD COLUMN password_set_date TIMESTAMPTZ(6);

UPDATE letting_agent_access
SET password_set_date = COALESCE(last_modified_date, created_date)
WHERE encoded_password IS NOT NULL;

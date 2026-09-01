ALTER TABLE letting_agent_access
    DROP CONSTRAINT letting_agent_access_property_ownership_id_fkey;

ALTER TABLE letting_agent_access
    ADD CONSTRAINT letting_agent_access_property_ownership_id_fkey
        FOREIGN KEY (property_ownership_id) REFERENCES property_ownership (id) ON DELETE CASCADE;

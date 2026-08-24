-- The letting_agent_access row is a child of property_ownership, but the entity layer no longer maps the
-- relationship from the parent side, so JPA cannot clean it up when a property is deregistered. Cascade the
-- delete in the database instead.
ALTER TABLE letting_agent_access
    DROP CONSTRAINT letting_agent_access_property_ownership_id_fkey;

ALTER TABLE letting_agent_access
    ADD CONSTRAINT letting_agent_access_property_ownership_id_fkey
        FOREIGN KEY (property_ownership_id) REFERENCES property_ownership (id) ON DELETE CASCADE;

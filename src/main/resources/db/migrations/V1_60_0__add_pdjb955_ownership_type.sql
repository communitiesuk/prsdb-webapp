-- PDJB-1956: Introduce the new OwnershipType alongside the existing one.
-- The old enum becomes OwnershipTypeBeforePdjb955 and its column is made nullable, so rows
-- created once pdjb-955-associated-parties is on need not populate it.
-- TODO PDJB-2019: drop ownership_type_before_pdjb955 once PDJB-2016 has removed the flag.

ALTER TABLE property_ownership RENAME COLUMN ownership_type TO ownership_type_before_pdjb955;

ALTER TABLE property_ownership ALTER COLUMN ownership_type_before_pdjb955 DROP NOT NULL;

ALTER TABLE property_ownership ADD COLUMN ownership_type SMALLINT;

UPDATE property_ownership
SET ownership_type = CASE ownership_type_before_pdjb955
                       WHEN 0 THEN 2 -- FREEHOLD          -> SOLE_FREEHOLD
                       WHEN 1 THEN 1 -- LEASEHOLD         -> LEASEHOLD_ONLY
                       WHEN 2 THEN 3 -- SHARE_OF_FREEHOLD -> JOINT_TENANTS
                       WHEN 3 THEN 5 -- COMMONHOLD        -> COMMONHOLD
                     END;

ALTER TABLE property_ownership ALTER COLUMN ownership_type SET NOT NULL;

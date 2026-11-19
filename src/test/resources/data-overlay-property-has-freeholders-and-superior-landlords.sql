-- Overlay script: gives property ownership 34 (a sole freehold for the mock user) a share of joint tenants ownership
-- type with a named freeholder and a named superior landlord, so the ownership update journey has existing parties
-- to show and clear. Apply on top of data-local.sql, e.g.
--   listOf("data-local.sql", "data-overlay-property-has-freeholders-and-superior-landlords.sql")
UPDATE property_ownership
SET ownership_type = 7, -- SHARE_OF_JOINT_TENANTS
    ownership_type_before_pdjb955 = 2 -- SHARE_OF_FREEHOLD
WHERE id = 34;

INSERT INTO freeholder (name, email, property_ownership_id)
VALUES ('Fiona Freeholder', 'fiona.freeholder@example.com', 34);

INSERT INTO superior_landlord (name, email, property_ownership_id)
VALUES ('Simon Superior', 'simon.superior@example.com', 34);

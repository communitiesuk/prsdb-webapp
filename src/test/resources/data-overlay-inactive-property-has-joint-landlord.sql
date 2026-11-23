-- Adds landlord 2 as a joint landlord on the mock user's inactive property 6 (which has no details to provide).
-- Apply on top of data-local.sql.

INSERT INTO ownership_link (landlord_id, landlordship_id, created_date)
VALUES (2, 6, '2025-01-15');

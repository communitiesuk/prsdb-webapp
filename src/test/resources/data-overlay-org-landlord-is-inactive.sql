-- Deactivates the organisation landlord (id 1) with no deactivated or incomplete properties.
-- Apply on top of data-mockuser-org-landlord-trust.sql.

UPDATE landlord
SET is_active = false, last_deactivated_date = '01/01/26'
WHERE id = 1;

-- Deactivates the joint property (id 2) and its co-owner (landlord id 2), who has no other property.
-- Apply on top of data-mockuser-landlord-with-sole-and-joint-properties.sql.

UPDATE property_ownership
SET is_active = false, last_deactivated_date = '01/01/26'
WHERE id = 2;

UPDATE landlord
SET is_active = false, last_deactivated_date = '01/01/26'
WHERE id = 2;

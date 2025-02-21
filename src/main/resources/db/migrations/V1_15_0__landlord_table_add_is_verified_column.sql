ALTER TABLE landlord
    ADD is_verified BOOLEAN;

UPDATE landlord SET is_verified = false;

ALTER TABLE landlord
    ALTER COLUMN is_verified SET NOT NULL;
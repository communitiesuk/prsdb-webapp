ALTER TABLE landlord
    ADD has_accepted_privacy_notice BOOLEAN;

UPDATE landlord SET has_accepted_privacy_notice = false;

ALTER TABLE landlord
    ALTER COLUMN has_accepted_privacy_notice SET NOT NULL;
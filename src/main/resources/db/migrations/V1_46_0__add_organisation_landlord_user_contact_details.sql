ALTER TABLE organisation_landlord_user
    ADD COLUMN name VARCHAR(255),
    ADD COLUMN email VARCHAR(255);

UPDATE organisation_landlord_user AS organisation_user
SET name = landlord.organisation_registrant_name,
    email = landlord.organisation_registrant_email
FROM landlord
WHERE landlord.id = organisation_user.organisation_landlord_id;

ALTER TABLE organisation_landlord_user
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN email SET NOT NULL;

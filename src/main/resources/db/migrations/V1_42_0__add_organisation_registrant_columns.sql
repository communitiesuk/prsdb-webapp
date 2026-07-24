ALTER TABLE landlord
    ADD COLUMN organisation_registrant_name VARCHAR(255),
    ADD COLUMN organisation_registrant_date_of_birth DATE,
    ADD COLUMN organisation_registrant_email VARCHAR(255),
    ADD COLUMN organisation_registrant_phone_number VARCHAR(255);

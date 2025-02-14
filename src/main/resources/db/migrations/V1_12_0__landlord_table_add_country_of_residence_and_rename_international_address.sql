ALTER TABLE landlord
    ADD country_of_residence VARCHAR(255);

ALTER TABLE landlord
    ALTER COLUMN country_of_residence SET NOT NULL;

ALTER TABLE landlord
    RENAME COLUMN international_address TO non_england_or_wales_address;
ALTER TABLE landlord
    ADD country_of_residence VARCHAR(255);

UPDATE landlord SET country_of_residence = 'England or Wales';

ALTER TABLE landlord
    ALTER COLUMN country_of_residence SET NOT NULL;

ALTER TABLE landlord
    RENAME COLUMN international_address TO non_england_or_wales_address;
ALTER TABLE property_ownership
    ADD num_bedrooms INTEGER,
    ADD list_bills_included VARCHAR(255),
    ADD custom_bills_included VARCHAR(255),
    ADD furnished_status SMALLINT,
    ADD rent_frequency SMALLINT,
    ADD custom_rent_frequency VARCHAR(255),
    ADD rent_amount DECIMAL(9, 2);

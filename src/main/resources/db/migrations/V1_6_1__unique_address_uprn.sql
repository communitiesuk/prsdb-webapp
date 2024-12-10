ALTER TABLE address
    ADD CONSTRAINT uc_address_uprn UNIQUE (uprn);
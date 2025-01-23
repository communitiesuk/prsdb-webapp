ALTER TABLE local_authority
    ADD custodian_code VARCHAR(255);

ALTER TABLE local_authority
    ALTER COLUMN custodian_code SET NOT NULL;

ALTER TABLE local_authority
    ADD CONSTRAINT uc_localauthority_custodiancode UNIQUE (custodian_code);

ALTER TABLE address
    ADD local_authority_id INTEGER;

ALTER TABLE address
    ADD CONSTRAINT FK_ADDRESS_LA FOREIGN KEY (local_authority_id) REFERENCES local_authority (id);

ALTER TABLE address
    DROP COLUMN custodian_code;
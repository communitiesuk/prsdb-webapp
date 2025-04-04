CREATE TABLE address
(
    id                  BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    last_modified_date  TIMESTAMPTZ(6),
    created_date        TIMESTAMPTZ(6),
    uprn                BIGINT,
    single_line_address VARCHAR(472)                            NOT NULL,
    organisation        VARCHAR(255),
    sub_building        VARCHAR(255),
    building_name       VARCHAR(255),
    building_number     VARCHAR(255),
    street_name         VARCHAR(255),
    locality            VARCHAR(255),
    town_name           VARCHAR(255),
    postcode            VARCHAR(255),
    custodian_code      VARCHAR(255),
    CONSTRAINT pk_address PRIMARY KEY (id)
);

ALTER TABLE landlord
    ADD address_id BIGINT;

ALTER TABLE landlord
    ADD created_date TIMESTAMPTZ(6);

ALTER TABLE landlord
    ADD date_of_birth TIMESTAMP(6);

ALTER TABLE landlord
    ADD email VARCHAR(255);

ALTER TABLE landlord
    ADD international_address VARCHAR(1000);

ALTER TABLE landlord
    ADD is_active BOOLEAN;

ALTER TABLE landlord
    ADD last_modified_date TIMESTAMPTZ(6);

ALTER TABLE landlord
    ADD name VARCHAR(255);

ALTER TABLE landlord
    ADD phone_number VARCHAR(255);

ALTER TABLE landlord
    ADD subject_identifier VARCHAR(255);

ALTER TABLE landlord
    ALTER COLUMN address_id SET NOT NULL;

ALTER TABLE landlord
    ALTER COLUMN email SET NOT NULL;

ALTER TABLE landlord
    ALTER COLUMN is_active SET NOT NULL;

ALTER TABLE landlord
    ALTER COLUMN name SET NOT NULL;

ALTER TABLE landlord
    ALTER COLUMN phone_number SET NOT NULL;

ALTER TABLE landlord
    ALTER COLUMN subject_identifier SET NOT NULL;

ALTER TABLE landlord
    ADD CONSTRAINT uc_landlord_subject_identifier UNIQUE (subject_identifier);

ALTER TABLE landlord
    ADD CONSTRAINT FK_LANDLORD_1L_USER FOREIGN KEY (subject_identifier) REFERENCES one_login_user (id);

ALTER TABLE landlord
    ADD CONSTRAINT FK_LANDLORD_ADDRESS FOREIGN KEY (address_id) REFERENCES address (id);